/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt;

import static org.eclipse.sapphire.modeling.util.MiscUtil.equal;

import java.util.List;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireAction.HandlersChangedEvent;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionSystemPart.CheckedStateChangedEvent;
import org.eclipse.sapphire.ui.SapphireActionSystemPart.EnablementChangedEvent;
import org.eclipse.sapphire.ui.SapphireActionSystemPart.ImagesChangedEvent;
import org.eclipse.sapphire.ui.SapphireActionSystemPart.LabelChangedEvent;
import org.eclipse.sapphire.ui.SapphireActionSystemPart.VisibilityEvent;
import org.eclipse.sapphire.ui.def.SapphireActionType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SapphireMenuActionPresentation extends SapphireHotSpotsActionPresentation
{
    private Menu menu;
    private final Listener actionVisibilityListener;
    
    public SapphireMenuActionPresentation( final SapphireActionPresentationManager manager )
    {
        super( manager );
        
        this.actionVisibilityListener = new FilteredListener<VisibilityEvent>()
        {
            @Override
            protected void handleTypedEvent( final VisibilityEvent event )
            {
                render();
            }
        };
        
        for( SapphireAction action : manager.getActions() )
        {
            action.attach( this.actionVisibilityListener );
        }
    }
    
    public Menu getMenu()
    {
        return this.menu;
    }
    
    public void setMenu( final Menu menu )
    {
        this.menu = menu;
    }
    
    public void render()
    {
        if( this.menu == null )
        {
            return;
        }
        
        final SwtPresentation context = (SwtPresentation) getManager().context();
        
        for( MenuItem item : this.menu.getItems() )
        {
            item.dispose();
        }
        
        boolean first = true;
        String lastGroup = null;
        
        for( final SapphireAction action : getActions() )
        {
            if( action.isVisible() )
            {
                final String group = action.getGroup();
                
                if( ! first && ! equal( lastGroup, group ) )
                {
                    new MenuItem( this.menu, SWT.SEPARATOR );
                }
                
                first = false;
                lastGroup = group;
                
                final List<SapphireActionHandler> handlers = action.getActiveHandlers();
                final MenuItem menuItem;
    
                if( action.getType() == SapphireActionType.PUSH )
                {
                    if( handlers.size() == 1 )
                    {
                        menuItem = new MenuItem( this.menu, SWT.PUSH );
                        
                        menuItem.addSelectionListener
                        (
                            new SelectionAdapter()
                            {
                                @Override
                                public void widgetSelected( final SelectionEvent event )
                                {
                                    handlers.get( 0 ).execute( context );
                                }
                            }
                        );
                    }
                    else
                    {
                        final Menu childMenu = new Menu( this.menu );
                        
                        menuItem = new MenuItem( this.menu, SWT.CASCADE );
                        menuItem.setMenu( childMenu );
                        
                        for( SapphireActionHandler handler : action.getActiveHandlers() )
                        {
                            renderMenuItem( childMenu, handler );
                        }
                    }
                }
                else if( action.getType() == SapphireActionType.TOGGLE )
                {
                    menuItem = new MenuItem( this.menu, SWT.CHECK );
                    
                    menuItem.addSelectionListener
                    (
                        new SelectionAdapter()
                        {
                            @Override
                            public void widgetSelected( final SelectionEvent event )
                            {
                                handlers.get( 0 ).execute( context );
                            }
                        }
                    );
                }
                else
                {
                    throw new IllegalStateException();
                }
                
                final Runnable updateActionLabelOp = new Runnable()
                {
                    public void run()
                    {
                        if( ! menuItem.isDisposed() )
                        {
                            menuItem.setText( LabelTransformer.transform( action.getLabel(), CapitalizationType.TITLE_STYLE, false ) );
                        }
                    }
                };
                
                final Runnable updateActionImageOp = new Runnable()
                {
                    public void run()
                    {
                        if( ! menuItem.isDisposed() )
                        {
                            menuItem.setImage( context.resources().image( action.getImage( 16 ) ) );
                        }
                    }
                };
                
                final Runnable updateActionEnablementStateOp = new Runnable()
                {
                    public void run()
                    {
                        if( ! menuItem.isDisposed() )
                        {
                            menuItem.setEnabled( action.isEnabled() );
                        }
                    }
                };
                
                final Runnable updateActionCheckedStateOp = new Runnable()
                {
                    public void run()
                    {
                        if( ! menuItem.isDisposed() )
                        {
                            menuItem.setSelection( action.isChecked() );
                        }
                    }
                };
                
                updateActionLabelOp.run();
                updateActionImageOp.run();
                updateActionEnablementStateOp.run();
                updateActionCheckedStateOp.run();
    
                final Listener listener = new Listener()
                {
                    @Override
                    public void handle( final Event event )
                    {
                        if( event instanceof LabelChangedEvent )
                        {
                            updateActionLabelOp.run();
                        }
                        else if( event instanceof ImagesChangedEvent )
                        {
                            updateActionImageOp.run();
                        }
                        else if( event instanceof EnablementChangedEvent )
                        {
                            updateActionEnablementStateOp.run();
                        }
                        else if( event instanceof CheckedStateChangedEvent )
                        {
                            updateActionCheckedStateOp.run();
                        }
                        else if( event instanceof HandlersChangedEvent )
                        {
                            render();
                        }
                    }
                };
    
                action.attach( listener );
                
                menuItem.addDisposeListener
                (
                    new DisposeListener()
                    {
                        public void widgetDisposed( final DisposeEvent event )
                        {
                            action.detach( listener );
                        }
                    }
                );
            }
        }
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        for( SapphireAction action : getManager().getActions() )
        {
            action.detach( this.actionVisibilityListener );
        }
    }
    
}
