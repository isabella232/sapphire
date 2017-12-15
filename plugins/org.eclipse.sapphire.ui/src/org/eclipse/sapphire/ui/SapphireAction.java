/******************************************************************************
 * Copyright (c) 2012 Oracle and Other Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Greg Amerson - [342771] Support "image+label" hint for when actions are presented in a toolbar           
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.ui.def.ISapphireActionDef;
import org.eclipse.sapphire.ui.def.ISapphireHint;
import org.eclipse.sapphire.ui.def.ISapphirePartDef;
import org.eclipse.sapphire.ui.def.SapphireActionType;
import org.eclipse.sapphire.ui.def.SapphireKeySequence;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.sapphire.ui.util.TopologicalSorter;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireAction extends SapphireActionSystemPart
{
    private SapphireActionGroup parent;
    private SapphireActionType type;
    private String group;
    private SapphireKeySequence keyBinding;
    private final List<SapphireActionHandler> handlers = new CopyOnWriteArrayList<SapphireActionHandler>();
    private final Map<SapphireActionHandlerFactory,List<SapphireActionHandler>> handlerFactories = new LinkedHashMap<SapphireActionHandlerFactory,List<SapphireActionHandler>>();
    private final List<SapphireActionHandlerFilter> filters = new CopyOnWriteArrayList<SapphireActionHandlerFilter>();
    private Listener handlerListener;
    private Map<String,Object> hints;
    
    public void init( final SapphireActionGroup parent,
                      final ISapphireActionDef def )
    {
        this.parent = parent;
        
        super.init( def );
        
        this.handlerListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                if( event instanceof EnablementChangedEvent )
                {
                    refreshEnablementState();
                }
                else if( event instanceof CheckedStateChangedEvent )
                {
                    refreshCheckedState();
                }
            }
        };
        
        attach
        (
            new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof HandlersChangedEvent || event instanceof FiltersChangedEvent )
                    {
                        refreshEnablementState();
                        refreshCheckedState();
                    }
                    else if( event instanceof DisposeEvent )
                    {
                        for( SapphireActionHandler handler : SapphireAction.this.handlers )
                        {
                            try
                            {
                                handler.dispose();
                            }
                            catch( Exception e )
                            {
                                SapphireUiFrameworkPlugin.log( e );
                            }
                        }
                        
                        for( SapphireActionHandlerFactory factory : SapphireAction.this.handlerFactories.keySet() )
                        {
                            try
                            {
                                factory.dispose();
                            }
                            catch( Exception e )
                            {
                                SapphireUiFrameworkPlugin.log( e );
                            }
                        }
                    }
                }
            }
        );

        if( def != null )
        {
            this.type = def.getType().getContent();
            this.group = def.getGroup().getContent();
            this.keyBinding = def.getKeyBinding().getContent();
        }
        
        setEnabled( false );
        
        this.hints = new HashMap<String,Object>();
        
        for( ISapphireHint hint : def.getHints() )
        {
            final String name = hint.getName().getText();
            Object parsedValue = null;
            
            if( name.equals( ISapphirePartDef.HINT_STYLE ) )
            {
                parsedValue = hint.getValue().getText();
            }
           
            this.hints.put( name, parsedValue );
        }
    }
    
    @Override
    protected FunctionContext initFunctionContext()
    {
        final ISapphirePart part = getPart();
        
        return new ModelElementFunctionContext( part.getLocalModelElement(), part.getDefinition().adapt( LocalizationService.class ) )
        {
            @Override
            public FunctionResult property( final Object element,
                                            final String name )
            {
                if( element == this && name.equalsIgnoreCase( "handlers" ) )
                {
                    final Function f = new Function()
                    {
                        @Override
                        public String name()
                        {
                            return "ReadProperty";
                        }
                        
                        @Override
                        public FunctionResult evaluate( final FunctionContext context )
                        {
                            return new FunctionResult( this, context )
                            {
                                private Listener listener;
                                
                                @Override
                                protected void init()
                                {
                                    super.init();
                                    
                                    this.listener = new Listener()
                                    {
                                        @Override
                                        public void handle( final Event event )
                                        {
                                            if( event instanceof HandlersChangedEvent )
                                            {
                                                refresh();
                                            }
                                        }
                                    };
                                    
                                    SapphireAction.this.attach( this.listener );
                                }

                                @Override
                                protected Object evaluate()
                                {
                                    return getActiveHandlers();
                                }

                                @Override
                                public void dispose()
                                {
                                    super.dispose();
                                    SapphireAction.this.detach( this.listener );
                                }
                            };
                        }
                    };
                    
                    f.init();
                    
                    return f.evaluate( this );
                }
                else if( element instanceof SapphireActionHandler && name.equalsIgnoreCase( "label" ) )
                {
                    return Literal.create( ( (SapphireActionHandler) element ).getLabel() ).evaluate( this );
                }
                
                return super.property( element, name );
            }
        };
    }

    public SapphireActionGroup getActionSet()
    {
        return this.parent;
    }
    
    public final ISapphirePart getPart()
    {
        return this.parent.getPart();
    }

    public String getContext()
    {
        return this.parent.getContext();
    }
    
    @SuppressWarnings( "unchecked" )
    
    public <T> T getRenderingHint( final String name,
                                   final T defaultValue )
    {
        final Object hintValue = this.hints == null ? null : this.hints.get( name );
        return hintValue == null ? defaultValue : (T) hintValue;
    }

    public SapphireActionType getType()
    {
        synchronized( this )
        {
            return this.type;
        }
    }
    
    public void setType( final SapphireActionType type )
    {
        synchronized( this )
        {
            this.type = type;
        }
        
        broadcast( new TypeChangedEvent() );
    }
    
    public String getGroup()
    {
        synchronized( this )
        {
            return this.group;
        }
    }
    
    public void setGroup( final String group )
    {
        synchronized( this )
        {
            this.group = group;
        }
        
        broadcast( new GroupChangedEvent() );
    }

    public SapphireKeySequence getKeyBinding()
    {
        synchronized( this )
        {
            return this.keyBinding;
        }
    }
    
    public void setKeyBinding( final SapphireKeySequence keyBinding )
    {
        synchronized( this )
        {
            this.keyBinding = keyBinding;
        }
        
        broadcast( new KeyBindingChangedEvent() );
    }
    
    public void addHandler( final SapphireActionHandler handler )
    {
        handler.attach( this.handlerListener );
        this.handlers.add( handler );
        broadcast( new HandlersChangedEvent() );
    }
    
    public void removeHandler( final SapphireActionHandler handler )
    {
        handler.detach( this.handlerListener );
        this.handlers.remove( handler );
        broadcast( new HandlersChangedEvent() );
    }
    
    public void removeHandlers( final Collection<SapphireActionHandler> handlers )
    {
        for( SapphireActionHandler handler : handlers )
        {
            handler.detach( this.handlerListener );
            this.handlers.remove( handler );
        }
        
        broadcast( new HandlersChangedEvent() );
    }
    
    public void addHandlerFactory( final SapphireActionHandlerFactory factory )
    {
        synchronized( this )
        {
            if( this.handlerFactories.containsKey( factory ) )
            {
                throw new IllegalArgumentException();
            }
            
            final List<SapphireActionHandler> handlers = new ArrayList<SapphireActionHandler>();
            
            for( SapphireActionHandler handler : factory.create() )
            {
                handler.init( this, null );
                handler.attach( this.handlerListener );
                handlers.add( handler );
            }
            
            factory.attach
            (
                new Listener()
                {
                    @Override
                    public void handle( final Event event )
                    {
                        refreshHandlerFactory( factory );
                    }
                }
            );
            
            this.handlerFactories.put( factory, handlers );
        }
        
        broadcast( new HandlersChangedEvent() );
    }
    
    public void removeHandlerFactory( final SapphireActionHandlerFactory factory )
    {
        boolean changed = false;
        
        synchronized( this )
        {
            final List<SapphireActionHandler> handlers = this.handlerFactories.remove( factory );
            
            if( handlers != null )
            {
                for( SapphireActionHandler handler : handlers )
                {
                    try
                    {
                        handler.dispose();
                    }
                    catch( Exception e )
                    {
                        LoggingService.log( e );
                    }
                }
                
                changed = true;
            }
        }
        
        if( changed )
        {
            broadcast( new HandlersChangedEvent() );
        }
    }
    
    private void refreshHandlerFactory( final SapphireActionHandlerFactory factory )
    {
        synchronized( this )
        {
            final List<SapphireActionHandler> handlers = this.handlerFactories.get( factory );
            
            if( handlers == null )
            {
                throw new IllegalStateException();
            }
            
            for( SapphireActionHandler handler : handlers )
            {
                try
                {
                    handler.dispose();
                }
                catch( Exception e )
                {
                    LoggingService.log( e );
                }
            }
            
            handlers.clear();
            
            for( SapphireActionHandler handler : factory.create() )
            {
                handler.init( this, null );
                handlers.add( handler );
            }
        }
        
        broadcast( new HandlersChangedEvent() );
    }
    
    public List<SapphireActionHandler> getActiveHandlers()
    {
        synchronized( this )
        {
            final List<SapphireActionHandler> handlers = new ArrayList<SapphireActionHandler>();
            
            handlers.addAll( this.handlers );
            
            for( List<SapphireActionHandler> factoryHandlers : this.handlerFactories.values() )
            {
                handlers.addAll( factoryHandlers );
            }
            
            final TopologicalSorter<SapphireActionHandler> sorter = new TopologicalSorter<SapphireActionHandler>();
            
            for( SapphireActionHandler handler : handlers )
            {
                boolean ok = true;
                
                List<SapphireActionHandlerFilter> failedFilters = null;
                
                for( SapphireActionHandlerFilter filter : this.filters )
                {
                    try
                    {
                        ok = filter.check( handler );
                    }
                    catch( Exception e )
                    {
                        SapphireUiFrameworkPlugin.log( e );
                        
                        // Filters are booted on first failure to keep malfunctioning filters from
                        // flooding the log, etc.
                        
                        if( failedFilters == null )
                        {
                            failedFilters = new ArrayList<SapphireActionHandlerFilter>();
                        }
                        
                        failedFilters.add( filter );
                    }
                    
                    if( ! ok )
                    {
                        break;
                    }
                }
                
                if( failedFilters != null )
                {
                    this.filters.removeAll( failedFilters );
                }
                
                if( ok )
                {
                    final TopologicalSorter.Entity handlerEntity = sorter.entity( handler.getId(), handler );
                    
                    for( SapphireActionLocationHint locationHint : handler.getLocationHints() )
                    {
                        handlerEntity.constraint( locationHint.toString() );
                    }
                }
            }
            
            return Collections.unmodifiableList( sorter.sort() );
        }
    }
    
    public SapphireActionHandler getFirstActiveHandler()
    {
        final List<SapphireActionHandler> handlers = getActiveHandlers();
        return ( handlers.isEmpty() ? null : handlers.get( 0 ) );
    }
    
    public boolean hasActiveHandlers()
    {
        return ( getFirstActiveHandler() != null );
    }
    
    public void addFilter( final SapphireActionHandlerFilter filter )
    {
        this.filters.add( filter );
        broadcast( new FiltersChangedEvent() );
    }
    
    public void removeFilter( final SapphireActionHandlerFilter filter )
    {
        this.filters.remove( filter );
        broadcast( new FiltersChangedEvent() );
    }
    
    private void refreshEnablementState()
    {
        boolean enabled = false;
        
        for( SapphireActionHandler handler : getActiveHandlers() )
        {
            if( handler.isEnabled() )
            {
                enabled = true;
                break;
            }
        }
        
        setEnabled( enabled );
    }
    
    private void refreshCheckedState()
    {
        boolean checked = false;
        final SapphireActionHandler handler = getFirstActiveHandler();
        
        if( handler != null )
        {
            checked = handler.isChecked();
        }
        
        setChecked( checked );
    }
    
    public static final class TypeChangedEvent extends Event {}
    public static final class GroupChangedEvent extends Event {}
    public static final class KeyBindingChangedEvent extends Event {}
    public static final class HandlersChangedEvent extends Event {}
    public static final class FiltersChangedEvent extends Event {}
    
}