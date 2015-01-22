/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.modeling.util.MiscUtil.normalizeToEmptyString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.sapphire.Disposable;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.ui.def.ActionContextRef;
import org.eclipse.sapphire.ui.def.ActionContextsHostDef;
import org.eclipse.sapphire.ui.def.ActionDef;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.def.ActionHandlerFactoryDef;
import org.eclipse.sapphire.ui.def.ActionHandlerFilterDef;
import org.eclipse.sapphire.ui.def.ISapphireConditionHostDef;
import org.eclipse.sapphire.ui.def.PartDef;
import org.eclipse.sapphire.ui.util.TopologicalSorter;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SapphireActionGroup implements Disposable
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
        
        final PartDef partDef = this.part.definition();
        
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
        // Sort actions in two passes. First pass buckets actions by groups.
        
        final Map<String,List<SapphireAction>> buckets = new LinkedHashMap<String,List<SapphireAction>>();
        
        for( SapphireAction action : this.actions )
        {
            final String group = normalizeToEmptyString( action.getGroup() );
            
            List<SapphireAction> bucket = buckets.get( group );
            
            if( bucket == null )
            {
                bucket = new ArrayList<SapphireAction>();
                buckets.put( group, bucket );
            }
            
            bucket.add( action );
        }
        
        // Second pass performs a topological sort using before and after location hints.
        
        final TopologicalSorter<SapphireAction> sorter = new TopologicalSorter<SapphireAction>();
        
        for( List<SapphireAction> bucket : buckets.values() )
        {
            for( SapphireAction action : bucket )
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
    
    @Override
    public void dispose()
    {
        for( SapphireAction action : this.actions )
        {
            action.dispose();
        }
    }
    
    private void createActions( final List<ActionDef> defs )
    {
        for( ActionDef def : defs )
        {
            if( isForContext( def ) && checkCondition( def ) )
            {
                final SapphireAction action = new SapphireAction();
                action.init( this, def );
                addAction( action );
            }
        }
    }
    
    private void createActionHandlers( final List<ActionHandlerDef> defs )
    {
        for( ActionHandlerDef def : defs )
        {
            final SapphireAction action = getAction( def.getAction().content() );
            
            if( action != null && isForContext( def ) && checkCondition( def ) )
            {
                try
                {
                    final JavaType implType = def.getImplClass().target();
                    
                    if( implType != null )
                    {
                        final Class<?> implClass = (Class<?>) implType.artifact();
                        
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
                    Sapphire.service( LoggingService.class ).log( e );
                }
            }
        }
    }

    private void createActionHandlersFromFactories( final List<ActionHandlerFactoryDef> defs )
    {
        for( ActionHandlerFactoryDef def : defs )
        {
            final SapphireAction action = getAction( def.getAction().content() );
            
            if( action != null && isForContext( def ) && checkCondition( def ) )
            {
                try
                {
                    final JavaType implType = def.getImplClass().target();
                    
                    if( implType != null )
                    {
                        final Class<?> implClass = (Class<?>) implType.artifact();
                        
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
                    Sapphire.service( LoggingService.class ).log( e );
                }
            }
        }
    }
    
    private void createActionHandlerFilters( final List<ActionHandlerFilterDef> defs )
    {
        for( ActionHandlerFilterDef def : defs )
        {
            if( isForContext( def ) )
            {
                try
                {
                    final JavaType implClass = def.getImplClass().target();
                    
                    if( implClass != null )
                    {
                        final SapphireActionHandlerFilter filter = (SapphireActionHandlerFilter) ( (Class<?>) implClass.artifact() ).newInstance();
                        addFilter( filter );
                    }
                }
                catch( Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                }
            }
        }
    }

    private boolean checkCondition( final ISapphireConditionHostDef def )
    {
        try
        {
            final JavaType conditionType = def.getConditionClass().target();
            
            if( conditionType != null )
            {
                final Class<?> conditionClass = (Class<?>) conditionType.artifact();
                
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
            Sapphire.service( LoggingService.class ).log( e );
            return false;
        }
    }

    private boolean isForContext( final ActionContextsHostDef def )
    {
        if( ! def.getContexts().isEmpty() )
        {
            for( ActionContextRef ctxt : def.getContexts() )
            {
                if( this.context.equalsIgnoreCase( ctxt.getContext().content() ) )
                {
                    return true;
                }
            }
            
            return false;
        }
        
        return true;
    }
    
}