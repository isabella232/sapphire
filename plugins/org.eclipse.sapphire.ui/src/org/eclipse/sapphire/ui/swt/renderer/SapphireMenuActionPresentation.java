/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.renderer;

import static org.eclipse.sapphire.modeling.util.MiscUtil.equal;

import java.util.List;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.SapphireActionType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireMenuActionPresentation extends SapphireHotSpotsActionPresentation
{
    private Menu menu;
    
    public SapphireMenuActionPresentation( final SapphireActionPresentationManager manager )
    {
        super( manager );
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
        final SapphireRenderingContext context = getManager().getContext();
        
        boolean first = true;
        String lastGroup = null;
        
        for( final SapphireAction action : getActions() )
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
                        menuItem.setImage( context.getImageCache().getImage( action.getImage( 16 ) ) );
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
            
            action.addListener
            (
                new SapphireAction.Listener()
                {
                    @Override
                    public void handleEvent( final SapphireAction.Event event )
                    {
                        final String type = event.getType();
                        
                        if( type.equals( SapphireAction.EVENT_LABEL_CHANGED ) )
                        {
                            updateActionLabelOp.run();
                        }
                        else if( type.equals( SapphireAction.EVENT_IMAGES_CHANGED ) )
                        {
                            updateActionLabelOp.run();
                        }
                        else if( type.equals( SapphireAction.EVENT_ENABLEMENT_STATE_CHANGED ) )
                        {
                            updateActionEnablementStateOp.run();
                        }
                        else if( type.equals( SapphireAction.EVENT_CHECKED_STATE_CHANGED ) )
                        {
                            updateActionCheckedStateOp.run();
                        }
                    }
                }
            );
            
            updateActionLabelOp.run();
            updateActionImageOp.run();
            updateActionEnablementStateOp.run();
            updateActionCheckedStateOp.run();
        }
    }
    
}
