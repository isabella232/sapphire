/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.ui.def.ISapphireActionContext;
import org.eclipse.sapphire.ui.def.ISapphireActionContextsHostDef;
import org.eclipse.sapphire.ui.def.ISapphireActionDef;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerFactoryDef;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerFilterDef;
import org.eclipse.sapphire.ui.def.ISapphireConditionHostDef;
import org.eclipse.sapphire.ui.def.ISapphirePartDef;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.sapphire.ui.util.TopologicalSorter;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireActionGroup
{
    private final ISapphirePart part;
    private final String context;
    private final List<SapphireAction> actions;
    
    public SapphireActionGroup( final ISapphirePart part,
                                final String context )
    {
        this.part = part;
        this.context = context;
        
        this.actions = new CopyOnWriteArrayList<SapphireAction>();
        
        final ISapphirePartDef partDef = this.part.getDefinition();
        
        createActions( SapphireExtensionSystem.getActions() );
        
        if( partDef != null )
        {
            createActions( partDef.getActions() );
        }
        
        createActionHandlers( SapphireExtensionSystem.getActionHandlers() );
        createActionHandlersFromFactories( SapphireExtensionSystem.getActionHandlerFactories() );
        
        if( partDef != null )
        {
            createActionHandlers( partDef.getActionHandlers() );
            createActionHandlersFromFactories( partDef.getActionHandlerFactories() );
            createActionHandlerFilters( partDef.getActionHandlerFilters() );
        }
    }
    
    public ISapphirePart getPart()
    {
        return this.part;
    }
    
    public String getContext()
    {
        return this.context;
    }
    
    public List<SapphireAction> getActiveActions()
    {
        final TopologicalSorter<SapphireAction> sorter = new TopologicalSorter<SapphireAction>();
        
        for( SapphireAction action : this.actions )
        {
            if( action.hasActiveHandlers() )
            {
                final TopologicalSorter.Entity actionEntity = sorter.entity( action.getId(), action );
                
                for( SapphireActionLocationHint locationHint : action.getLocationHints() )
                {
                    actionEntity.constraint( locationHint.toString() );
                }
            }
        }
        
        return Collections.unmodifiableList( sorter.sort() );
    }
    
    public List<SapphireAction> getActions()
    {
        final TopologicalSorter<SapphireAction> sorter = new TopologicalSorter<SapphireAction>();
        
        for( SapphireAction action : this.actions )
        {
            final TopologicalSorter.Entity actionEntity = sorter.entity( action.getId(), action );
            
            for( SapphireActionLocationHint locationHint : action.getLocationHints() )
            {
                actionEntity.constraint( locationHint.toString() );
            }
        }
        
        return Collections.unmodifiableList( sorter.sort() );
    }
    
    public void addAction( final SapphireAction action )
    {
        this.actions.add( action );
    }
    
    public void removeAction( final SapphireAction action )
    {
        this.actions.remove( action );
    }
    
    public SapphireAction getAction( final String id )
    {
        for( SapphireAction action : this.actions )
        {
            if( id.equalsIgnoreCase( action.getId() ) )
            {
                return action;
            }
        }
        
        return null;
    }
    
    public boolean isEmpty()
    {
        for( SapphireAction action : this.actions )
        {
            if( action.hasActiveHandlers() )
            {
                return false;
            }
        }
        
        return true;
    }

    public void addFilter( final SapphireActionHandlerFilter filter )
    {
        for( SapphireAction action : this.actions )
        {
            action.addFilter( filter );
        }
    }
    
    public void removeFilter( final SapphireActionHandlerFilter filter )
    {
        for( SapphireAction action : this.actions )
        {
            action.removeFilter( filter );
        }
    }
    
    public void dispose()
    {
        for( SapphireAction action : this.actions )
        {
            action.dispose();
        }
    }
    
    private void createActions( final List<ISapphireActionDef> defs )
    {
        for( ISapphireActionDef def : defs )
        {
            if( isForContext( def ) && checkCondition( def ) )
            {
                final SapphireAction action = new SapphireAction();
                action.init( this, def );
                addAction( action );
            }
        }
    }
    
    private void createActionHandlers( final List<ISapphireActionHandlerDef> defs )
    {
        for( ISapphireActionHandlerDef def : defs )
        {
            final SapphireAction action = getAction( def.getAction().getContent() );
            
            if( action != null && isForContext( def ) && checkCondition( def ) )
            {
                try
                {
                    final JavaType implType = def.getImplClass().resolve();
                    
                    if( implType != null )
                    {
                        final Class<?> implClass = implType.artifact();
                        
                        if( implClass != null )
                        {
                            final SapphireActionHandler handler = (SapphireActionHandler) implClass.newInstance();
                            handler.init( action, def );
                            action.addHandler( handler );
                        }
                    }
                }
                catch( Exception e )
                {
                    SapphireUiFrameworkPlugin.log( e );
                }
            }
        }
    }

    private void createActionHandlersFromFactories( final List<ISapphireActionHandlerFactoryDef> defs )
    {
        for( ISapphireActionHandlerFactoryDef def : defs )
        {
            final SapphireAction action = getAction( def.getAction().getContent() );
            
            if( action != null && isForContext( def ) && checkCondition( def ) )
            {
                try
                {
                    final JavaType implType = def.getImplClass().resolve();
                    
                    if( implType != null )
                    {
                        final Class<?> implClass = implType.artifact();
                        
                        if( implClass != null )
                        {
                            final SapphireActionHandlerFactory factory = (SapphireActionHandlerFactory) implClass.newInstance();
                            factory.init( action, def );
                            action.addHandlerFactory( factory );
                        }
                    }
                }
                catch( Exception e )
                {
                    SapphireUiFrameworkPlugin.log( e );
                }
            }
        }
    }
    
    private void createActionHandlerFilters( final List<ISapphireActionHandlerFilterDef> defs )
    {
        for( ISapphireActionHandlerFilterDef def : defs )
        {
            if( isForContext( def ) )
            {
                try
                {
                    final JavaType implClass = def.getImplClass().resolve();
                    
                    if( implClass != null )
                    {
                        final SapphireActionHandlerFilter filter = (SapphireActionHandlerFilter) implClass.artifact().newInstance();
                        addFilter( filter );
                    }
                }
                catch( Exception e )
                {
                    SapphireUiFrameworkPlugin.log( e );
                }
            }
        }
    }

    private boolean checkCondition( final ISapphireConditionHostDef def )
    {
        try
        {
            final JavaType conditionType = def.getConditionClass().resolve();
            
            if( conditionType != null )
            {
                final Class<?> conditionClass = conditionType.artifact();
                
                if( conditionClass != null )
                {
                    final SapphireCondition condition = SapphireCondition.create( this.part, conditionClass, null );
                    
                    if( condition != null )
                    {
                        try
                        {
                            return condition.getConditionState();
                        }
                        finally
                        {
                            condition.dispose();
                        }
                    }
                }
            }
            
            return true;
        }
        catch( Exception e )
        {
            SapphireUiFrameworkPlugin.log( e );
            return false;
        }
    }

    private boolean isForContext( final ISapphireActionContextsHostDef def )
    {
        if( ! def.getContexts().isEmpty() )
        {
            for( ISapphireActionContext ctxt : def.getContexts() )
            {
                if( this.context.equalsIgnoreCase( ctxt.getContext().getContent() ) )
                {
                    return true;
                }
            }
            
            return false;
        }
        
        return true;
    }
    
}