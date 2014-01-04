/******************************************************************************
 * Copyright (c) 2014 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [342771] Support "image+label" hint for when actions are presented in a toolbar
 *    Gregory Amerson - [374622] Add ability to specify action tooltips
 *    Shenxue Zhou - display a default image for actions without images
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt;

import static org.eclipse.sapphire.modeling.util.MiscUtil.equal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.ui.DefaultActionImage;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionSystemPart.CheckedStateChangedEvent;
import org.eclipse.sapphire.ui.SapphireActionSystemPart.EnablementChangedEvent;
import org.eclipse.sapphire.ui.SapphireActionSystemPart.LabelChangedEvent;
import org.eclipse.sapphire.ui.SapphireActionSystemPart.ToolTipChangedEvent;
import org.eclipse.sapphire.ui.def.ActionDef;
import org.eclipse.sapphire.ui.def.PartDef;
import org.eclipse.sapphire.ui.def.SapphireActionType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public final class SapphireToolBarActionPresentation extends SapphireHotSpotsActionPresentation
{
    private ToolBar toolbar;
    private Map<SapphireAction,Listener> listeners;
    
    public SapphireToolBarActionPresentation( final SapphireActionPresentationManager manager )
    {
        super( manager );
    }
    
    public ToolBar getToolBar()
    {
        return this.toolbar;
    }
    
    public void setToolBar( final ToolBar toolbar )
    {
        this.toolbar = toolbar;
    }
    
    public void render()
    {
        final SwtPresentation context = (SwtPresentation) getManager().context();
        
        boolean first = true;
        String lastGroup = null;
        
        this.listeners = new HashMap<SapphireAction,Listener>();
        
        for( final SapphireAction action : getActions() )
        {
            final String group = action.getGroup();
            
            if( ! first && ! equal( lastGroup, group ) )
            {
                new ToolItem( this.toolbar, SWT.SEPARATOR );
            }
            
            first = false;
            lastGroup = group;
            
            final ToolItem toolItem;
            final SelectionListener toolItemListener;
            
            if( action.getType() == SapphireActionType.PUSH )
            {
                toolItem = new ToolItem( this.toolbar, SWT.PUSH );
                
                registerHotSpot( action, new ToolItemHotSpot( toolItem ) );
                
                toolItemListener = new SelectionAdapter()
                {
                    @Override
                    public void widgetSelected( final SelectionEvent event )
                    {
                        final List<SapphireActionHandler> handlers = action.getActiveHandlers();
                        
                        if( handlers.size() == 1 )
                        {
                            handlers.get( 0 ).execute( context );
                        }
                        else
                        {
                            displayActionHandlerChoice( action );
                        }
                    }
                };
            }
            else if( action.getType() == SapphireActionType.TOGGLE )
            {
                toolItem = new ToolItem( this.toolbar, SWT.CHECK );
                
                toolItemListener = new SelectionAdapter()
                {
                    @Override
                    public void widgetSelected( final SelectionEvent event )
                    {
                        action.getActiveHandlers().get( 0 ).execute( context );
                    }
                };
            }
            else
            {
                throw new IllegalStateException();
            }
            
            final String hint = action.getRenderingHint( PartDef.HINT_STYLE, ActionDef.HINT_VALUE_STYLE_IMAGE );
            
            if( ActionDef.HINT_VALUE_STYLE_IMAGE.equals( hint ) || 
                ActionDef.HINT_VALUE_STYLE_IMAGE_TEXT.equals( hint ) )
            {
            	if (action.getImage( 16 ) != null)
            	{
            		toolItem.setImage( context.resources().image( action.getImage( 16 ) ) );
            	}
            	else
            	{
            		toolItem.setImage( DefaultActionImage.getDefaultActionImage() );
            	}
            }
            
            toolItem.setData( action );
            toolItem.addSelectionListener( toolItemListener );
            
            final Runnable updateActionLabelOp = new Runnable()
            {
                public void run()
                {
                    if( Display.getCurrent() == null )
                    {
                        Display.getDefault().asyncExec( this );
                        return;
                    }
                    
                    if( ! toolItem.isDisposed() )
                    {
                        if( ActionDef.HINT_VALUE_STYLE_IMAGE_TEXT.equals( hint ) ||
                            ActionDef.HINT_VALUE_STYLE_TEXT.equals( hint ) )
                        {
                            toolItem.setText( LabelTransformer.transform( action.getLabel(), CapitalizationType.TITLE_STYLE, true ) );
                        }
                    }
                }
            };
            
            final Runnable updateActionToolTipOp = new Runnable()
            {
                public void run()
                {
                    if( Display.getCurrent() == null )
                    {
                        Display.getDefault().asyncExec( this );
                        return;
                    }
                    
                    if( ! toolItem.isDisposed() )
                    {
                        toolItem.setToolTipText( LabelTransformer.transform( action.getToolTip(), CapitalizationType.TITLE_STYLE, false ) );
                    }
                }
            };
            
            final Runnable updateActionEnablementStateOp = new Runnable()
            {
                public void run()
                {
                    if( Display.getCurrent() == null )
                    {
                        Display.getDefault().asyncExec( this );
                        return;
                    }
                    
                    if( ! toolItem.isDisposed() )
                    {
                        toolItem.setEnabled( action.isEnabled() );
                    }
                }
            };
            
            final Runnable updateActionCheckedStateOp = new Runnable()
            {
                public void run()
                {
                    if( Display.getCurrent() == null )
                    {
                        Display.getDefault().asyncExec( this );
                        return;
                    }
                    
                    if( ! toolItem.isDisposed() )
                    {
                        toolItem.setSelection( action.isChecked() );
                    }
                }
            };
            
            final Listener listener = new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof LabelChangedEvent )
                    {
                        updateActionLabelOp.run();
                    }
                    else if( event instanceof ToolTipChangedEvent )
                    {
                        updateActionToolTipOp.run();
                    }
                    else if( event instanceof EnablementChangedEvent )
                    {
                        updateActionEnablementStateOp.run();
                    }
                    else if( event instanceof CheckedStateChangedEvent )
                    {
                        updateActionCheckedStateOp.run();
                    }
                }
            };

            action.attach( listener );
            this.listeners.put( action, listener );
            
            updateActionLabelOp.run();
            updateActionToolTipOp.run();
            updateActionEnablementStateOp.run();
            updateActionCheckedStateOp.run();
        }
        
        this.toolbar.getAccessible().addAccessibleListener
        (
            new AccessibleAdapter()
            {
                @Override
                public void getName( final AccessibleEvent event )
                {
                    final int childId = event.childID;
                    
                    if( childId == -1 )
                    {
                        event.result = getManager().getLabel();
                    }
                    else if( childId < SapphireToolBarActionPresentation.this.toolbar.getItemCount() )
                    {
                        final ToolItem item = SapphireToolBarActionPresentation.this.toolbar.getItem( childId );
                        final SapphireAction action = (SapphireAction) item.getData();
                        event.result = LabelTransformer.transform( action.getLabel(), CapitalizationType.TITLE_STYLE, false );
                    }
                }
            }
        );
    }
    
    @Override
    public void dispose()
    {
        if( this.listeners != null )
        {
            for( Map.Entry<SapphireAction,Listener> entry : this.listeners.entrySet() )
            {
                entry.getKey().detach( entry.getValue() );
            }
        }
        
        super.dispose();
    }

    private static final class ToolItemHotSpot extends HotSpot
    {
        private final ToolItem item;
        
        public ToolItemHotSpot( final ToolItem item )
        {
            this.item = item;
        }

        @Override
        public Rectangle getBounds()
        {
            return toDisplay( this.item.getParent(), this.item.getBounds() );
        }
    }
    
}
