/******************************************************************************
 * Copyright (c) 2011 Oracle and others
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

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
    public static final String EVENT_TYPE_CHANGED = "type";
    public static final String EVENT_GROUP_CHANGED = "group";
    public static final String EVENT_KEY_BINDING_CHANGED = "key-binding";
    public static final String EVENT_HANDLERS_CHANGED = "handlers";
    public static final String EVENT_FILTERS_CHANGED = "filters";
    
    private SapphireActionGroup parent;
    private SapphireActionType type;
    private String group;
    private SapphireKeySequence keyBinding;
    private final List<SapphireActionHandler> handlers = new CopyOnWriteArrayList<SapphireActionHandler>();
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
            public void handleEvent( final Event event )
            {
                final String type = event.getType();
                
                if( type.equals( EVENT_ENABLEMENT_STATE_CHANGED ) )
                {
                    refreshEnablementState();
                }
                else if( type.equals( EVENT_CHECKED_STATE_CHANGED ) )
                {
                    refreshCheckedState();
                }
            }
        };
        
        addListener
        (
            new Listener()
            {
                @Override
                public void handleEvent( final Event event )
                {
                    final String type = event.getType();
                    
                    if( type.equals( EVENT_HANDLERS_CHANGED ) || type.equals( EVENT_FILTERS_CHANGED ) )
                    {
                        refreshEnablementState();
                        refreshCheckedState();
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
                                private SapphireAction.Listener listener;
                                
                                @Override
                                protected void init()
                                {
                                    super.init();
                                    
                                    this.listener = new SapphireAction.Listener()
                                    {
                                        @Override
                                        public void handleEvent( final Event event )
                                        {
                                            if( event.getType().equals( EVENT_HANDLERS_CHANGED ) )
                                            {
                                                refresh();
                                            }
                                        }
                                    };
                                    
                                    SapphireAction.this.addListener( this.listener );
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
                                    SapphireAction.this.removeListener( this.listener );
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
        
        notifyListeners( new Event( EVENT_TYPE_CHANGED ) );
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
        
        notifyListeners( new Event( EVENT_GROUP_CHANGED ) );
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
        
        notifyListeners( new Event( EVENT_KEY_BINDING_CHANGED ) );
    }
    
    public void addHandler( final SapphireActionHandler handler )
    {
        handler.addListener( this.handlerListener );
        this.handlers.add( handler );
        notifyListeners( new Event( EVENT_HANDLERS_CHANGED ) );
    }
    
    public void removeHandler( final SapphireActionHandler handler )
    {
        handler.removeListener( this.handlerListener );
        this.handlers.remove( handler );
        notifyListeners( new Event( EVENT_HANDLERS_CHANGED ) );
    }
    
    public List<SapphireActionHandler> getActiveHandlers()
    {
        final TopologicalSorter<SapphireActionHandler> sorter = new TopologicalSorter<SapphireActionHandler>();
        
        for( SapphireActionHandler handler : this.handlers )
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
        notifyListeners( new Event( EVENT_FILTERS_CHANGED ) );
    }
    
    public void removeFilter( final SapphireActionHandlerFilter filter )
    {
        this.filters.remove( filter );
        notifyListeners( new Event( EVENT_FILTERS_CHANGED ) );
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
    
    public void dispose()
    {
        for( SapphireActionHandler handler : this.handlers )
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
    }
    
}