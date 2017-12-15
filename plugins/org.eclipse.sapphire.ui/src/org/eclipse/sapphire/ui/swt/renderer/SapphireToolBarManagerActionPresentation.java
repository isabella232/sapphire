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

import static org.eclipse.sapphire.modeling.util.internal.MiscUtil.equal;

import java.util.List;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.SubContributionItem;
import org.eclipse.jface.action.SubToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.SapphireActionType;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireToolBarManagerActionPresentation

    extends SapphireHotSpotsActionPresentation
    
{
    private IToolBarManager toolbar;
    
    public SapphireToolBarManagerActionPresentation( final SapphireActionPresentationManager manager )
    {
        super( manager );
    }

    public SapphireToolBarManagerActionPresentation( final ISapphirePart part,
                                                     final Shell shell,
                                                     final SapphireActionGroup actions )
    {
        this( new SapphireActionPresentationManager( new SapphireRenderingContext( part, shell ), actions ) );
    }
    
    public IToolBarManager getToolBarManager()
    {
        return this.toolbar;
    }
    
    public void setToolBarManager( final IToolBarManager toolbar )
    {
        this.toolbar = toolbar;
    }
    
    public void render()
    {
        setCursor( this.toolbar, null );
        
        final SapphireRenderingContext context = getManager().getContext();
        
        boolean first = true;
        String lastGroup = null;
        
        for( final SapphireAction action : getActions() )
        {
            final String group = action.getGroup();
            
            if( ! first && ! equal( lastGroup, group ) )
            {
                this.toolbar.add( new Separator() );
            }
            
            first = false;
            lastGroup = group;
            
            final List<SapphireActionHandler> handlers = action.getActiveHandlers();
            final org.eclipse.jface.action.Action a;
            
            if( action.getType() == SapphireActionType.PUSH )
            {
                a = new org.eclipse.jface.action.Action( null, org.eclipse.jface.action.Action.AS_PUSH_BUTTON )
                {
                    @Override
                    public void run()
                    {
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
                
                registerHotSpot( action, new JFaceActionHotSpot( a ) );
            }
            else if( action.getType() == SapphireActionType.TOGGLE )
            {
                a = new org.eclipse.jface.action.Action( null, org.eclipse.jface.action.Action.AS_CHECK_BOX )
                {
                    @Override
                    public void run()
                    {
                        handlers.get( 0 ).execute( context );
                    }
                };
            }
            else
            {
                throw new IllegalStateException();
            }
            
            a.setImageDescriptor( action.getImage( 16 ) );
            a.setToolTipText( LabelTransformer.transform( action.getLabel(), CapitalizationType.TITLE_STYLE, false ) );
            
            this.toolbar.add( a );
            
            final Runnable updateActionEnablementStateOp = new Runnable()
            {
                public void run()
                {
                    a.setEnabled( action.isEnabled() );
                }
            };
            
            final Runnable updateActionCheckedStateOp = new Runnable()
            {
                public void run()
                {
                    a.setChecked( action.isChecked() );
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
                        
                        if( type.equals( SapphireAction.EVENT_ENABLEMENT_STATE_CHANGED ) )
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
            
            updateActionEnablementStateOp.run();
            updateActionCheckedStateOp.run();
        }
        
        this.toolbar.update( true );
    }
    
    private static void setCursor( final IToolBarManager toolBarManager,
                                   final Cursor cursor )
    {
        if( toolBarManager instanceof ToolBarManager )
        {
            ( (ToolBarManager) toolBarManager ).getControl().setCursor( null );
        }
        else if( toolBarManager instanceof SubToolBarManager )
        {
            setCursor( (IToolBarManager) ( (SubToolBarManager) toolBarManager ).getParent(), cursor );
        }
    }
    
    private final class JFaceActionHotSpot
    
        extends HotSpot
        
    {
        private final org.eclipse.jface.action.Action action;
        
        public JFaceActionHotSpot( final org.eclipse.jface.action.Action item )
        {
            this.action = item;
        }
    
        @Override
        public Rectangle getBounds()
        {
            ToolItem actionToolItem = null;
            
            for( ToolItem toolItem : getToolBar( getToolBarManager() ).getItems() )
            {
                final Object data = toolItem.getData();
                
                if( data instanceof IContributionItem && getJFaceAction( ( (IContributionItem) data ) ) == this.action )
                {
                    actionToolItem = toolItem;
                    break;
                }
            }
            
            return toDisplay( actionToolItem.getParent(), actionToolItem.getBounds() );
        }
        
        private IAction getJFaceAction( final IContributionItem item )
        {
            if( item instanceof ActionContributionItem )
            {
                return ( (ActionContributionItem) item ).getAction();
            }
            else if( item instanceof SubContributionItem )
            {
                return getJFaceAction( ( (SubContributionItem) item ).getInnerItem() );
            }
            else
            {
                return null;
            }
        }
        
        private ToolBar getToolBar( final IToolBarManager manager )
        {
            if( manager instanceof ToolBarManager )
            {
                return ( (ToolBarManager) manager ).getControl();
            }
            else if( manager instanceof SubToolBarManager )
            {
                return getToolBar( (IToolBarManager) ( (SubToolBarManager) manager ).getParent() );
            }
            else
            {
                throw new IllegalStateException( manager.getClass().getName() );
            }
        }
    }
    
}
