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

package org.eclipse.sapphire.ui.forms.swt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.sapphire.Disposable;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionHandlerFilter;
import org.eclipse.sapphire.ui.SapphireActionSystemPart.EnablementChangedEvent;
import org.eclipse.sapphire.ui.SapphireActionSystemPart.ImagesChangedEvent;
import org.eclipse.sapphire.ui.SapphireActionSystemPart.LabelChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Actions presentation is responsible for presenting available actions to the user. A presentation
 * is attached to an actions context and handles a subset of actions available in that context. 
 * Presentation can take a variety of forms such as buttons, toolbars, menus or hyperlinks.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class SapphireActionPresentation implements Disposable
{
    private final SapphireActionPresentationManager manager;
    private final List<SapphireActionHandlerFilter> filters = new CopyOnWriteArrayList<SapphireActionHandlerFilter>();
    
    public SapphireActionPresentation( final SapphireActionPresentationManager manager )
    {
        this.manager = manager;
        manager.addPresentation( this );
    }
    
    public final SapphireActionPresentationManager getManager()
    {
        return this.manager;
    }
    
    public final List<SapphireAction> getActions()
    {
        final List<SapphireAction> actions = new ArrayList<SapphireAction>();
        
        for( final SapphireAction action : getManager().getActions() )
        {
            if( hasActionHandlers( action ) )
            {
                actions.add( action );
            }
        }
        
        return Collections.unmodifiableList( actions );
    }
    
    public final boolean hasActions()
    {
        for( final SapphireAction action : getManager().getActions() )
        {
            if( hasActionHandlers( action ) )
            {
                return true;
            }
        }
        
        return false;
    }
    
    public final List<SapphireActionHandler> getActionHandlers( final SapphireAction action )
    {
        final List<SapphireActionHandler> handlers = new ArrayList<SapphireActionHandler>();
        
        for( final SapphireActionHandler handler : action.getActiveHandlers() )
        {
            boolean ok = true;
            
            List<SapphireActionHandlerFilter> failedFilters = null;
            
            for( final SapphireActionHandlerFilter filter : this.filters )
            {
                try
                {
                    ok = filter.check( handler );
                }
                catch( Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                    
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
                handlers.add( handler );
            }
        }
        
        return Collections.unmodifiableList( handlers );
    }
    
    public final boolean hasActionHandlers( final SapphireAction action )
    {
        for( final SapphireActionHandler handler : action.getActiveHandlers() )
        {
            boolean ok = true;
            
            List<SapphireActionHandlerFilter> failedFilters = null;
            
            for( final SapphireActionHandlerFilter filter : this.filters )
            {
                try
                {
                    ok = filter.check( handler );
                }
                catch( Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                    
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
                return true;
            }
        }
        
        return false;
    }
    
    public final void addFilter( final SapphireActionHandlerFilter filter )
    {
        this.filters.add( filter );
    }
    
    public final void removeFilter( final SapphireActionHandlerFilter filter )
    {
        this.filters.remove( filter );
    }
    
    public abstract void render();
    
    public abstract boolean displayActionHandlerChoice( SapphireAction action );
    
    protected final MenuItem renderMenuItem( final Menu menu,
                                             final SapphireActionHandler handler )
    {
        final MenuItem menuItem = new MenuItem( menu, SWT.PUSH );
        
        menuItem.setEnabled( handler.isEnabled() );
        menuItem.setText( LabelTransformer.transform( handler.getLabel(), CapitalizationType.TITLE_STYLE, false ) );
        setMenuItemImage( menuItem, handler );
            
        menuItem.addSelectionListener
        (
            new SelectionAdapter()
            {
                @Override
                public void widgetSelected( final SelectionEvent event )
                {
                    handler.execute( getManager().context() );
                }
            }
        );
            
        final Listener handlerListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                final Runnable op = new Runnable()
                {
                    public void run()
                    {
                        if( Display.getCurrent() == null )
                        {
                            Display.getDefault().asyncExec( this );
                            return;
                        }
                        
                        if( event instanceof LabelChangedEvent )
                        {
                            menuItem.setText( LabelTransformer.transform( handler.getLabel(), CapitalizationType.TITLE_STYLE, false ) );
                        }
                        else if( event instanceof ImagesChangedEvent )
                        {
                            setMenuItemImage( menuItem, handler );
                        }
                        else if( event instanceof EnablementChangedEvent )
                        {
                            menuItem.setEnabled( handler.isEnabled() );
                        }
                    }
                };
                
                op.run();
            }
        };
        
        handler.attach( handlerListener );
        
        menuItem.addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event ) 
                {
                    handler.detach( handlerListener );
                }
            }
        );
        
        return menuItem;
    }

    protected void setMenuItemImage( final MenuItem menuItem,
                                     final SapphireActionHandler handler )
    {
        Image image = null;
    
        final ImageData imageData = handler.getImage( 16 );
        
        if( imageData != null )
        {
            image = ( (SwtPresentation) getManager().context() ).resources().image( imageData );
        }
        
        menuItem.setImage( image );
    }
    
    @Override
    public void dispose()
    {
    }
    
}
