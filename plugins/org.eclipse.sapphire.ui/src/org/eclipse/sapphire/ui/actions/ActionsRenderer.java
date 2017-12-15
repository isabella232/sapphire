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

package org.eclipse.sapphire.ui.actions;

import java.util.List;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.SubContributionItem;
import org.eclipse.jface.action.SubToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.ui.util.internal.MutableReference;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ActionsRenderer
{
    public static void fillMenu( final Menu menu,
                                 final List<ActionGroup> groups )
    {
        boolean first = true;
        
        for( ActionGroup group : groups )
        {
            if( ! group.isVisible() )
            {
                continue;
            }
            
            if( first )
            {
                first = false;
            }
            else
            {
                new MenuItem( menu, SWT.SEPARATOR );
            }
            
            for( final Action action : group.getActions() )
            {
                if( ! action.isVisible() )
                {
                    continue;
                }
                
                final MenuItem menuItem;
                final List<ActionGroup> childActionGroups = action.getChildActionGroups();
                
                if( ! childActionGroups.isEmpty() )
                {
                    final Menu childMenu = new Menu( menu );
                    
                    menuItem = new MenuItem( menu, SWT.CASCADE );
                    menuItem.setText( action.getLabel() );
                    menuItem.setImage( action.getImage() );
                    menuItem.setMenu( childMenu );
                    
                    fillMenu( childMenu, childActionGroups );
                }
                else
                {
                    menuItem = new MenuItem( menu, SWT.PUSH );
                    menuItem.setText( action.getLabel() );
                    menuItem.setImage( action.getImage() );
                    menuItem.setEnabled( action.isEnabled() );
                    
                    menuItem.addSelectionListener
                    (
                        new SelectionAdapter()
                        {
                            @Override
                            public void widgetSelected( final SelectionEvent event )
                            {
                                action.execute( menu.getShell() );
                            }
                        }
                    );
                    
                    final Action.Listener actionChangeListener = new Action.Listener()
                    {
                        @Override
                        public void handleActionChanged( final Action action )
                        {
                            menuItem.setEnabled( action.isEnabled() );
                        }
                    };
                    
                    action.addListener( actionChangeListener );
                    
                    menuItem.addDisposeListener
                    (
                        new DisposeListener()
                        {
                            public void widgetDisposed( final DisposeEvent event ) 
                            {
                                action.removeListener( actionChangeListener );
                            }
                        }
                    );
                }
                
                final String commandId = action.getCommandId();
                
                if( commandId != null )
                {
                    final IBindingService bindingService 
                        = (IBindingService) PlatformUI.getWorkbench().getService( IBindingService.class );
                    
                    final TriggerSequence[] triggerSequences = bindingService.getActiveBindingsFor( commandId );
                    
                    if( triggerSequences != null && triggerSequences.length > 0 )
                    {
                        final TriggerSequence triggerSequence = triggerSequences[ 0 ];
                        
                        if( triggerSequence instanceof KeySequence )
                        {
                            final String keySequenceString 
                                = SWTKeySupport.getKeyFormatterForPlatform().format( (KeySequence) triggerSequence );
                            
                            menuItem.setText( menuItem.getText() + "\t" + keySequenceString );
                        }
                    }
                }
            }
        }
    }
    
    public static void fillToolBar( final ToolBar toolbar,
                                    final List<ActionGroup> groups )
    {
        fillToolBar( toolbar, groups, false );
    }

    public static void fillToolBar( final ToolBar toolbar,
                                    final List<ActionGroup> groups,
                                    final boolean includeTrailingSeparator )
    {
        boolean first = true;
        
        for( ActionGroup group : groups )
        {
            if( ! group.isVisible() )
            {
                continue;
            }
            
            if( first )
            {
                first = false;
            }
            else
            {
                new ToolItem( toolbar, SWT.SEPARATOR );
            }
            
            for( final Action action : group.getActions() )
            {
                if( ! action.isVisible() )
                {
                    continue;
                }
                
                final int toolItemStyle
                    = ( action.getType() == Action.Type.TOGGLE ? SWT.CHECK : SWT.PUSH );
                    
                final ToolItem toolItem = new ToolItem( toolbar, toolItemStyle );
                
                toolItem.setImage( action.getImage( true ) );
                toolItem.setToolTipText( action.getLabel() );
                toolItem.setEnabled( action.isEnabled() );
                
                if( action.getType() == Action.Type.TOGGLE )
                {
                    toolItem.setSelection( action.isChecked() );
                }
                
                final SelectionListener toolItemListener;
                final List<ActionGroup> childActionGroups = action.getChildActionGroups();
                
                if( ! childActionGroups.isEmpty() )
                {
                    final MutableReference<Menu> popupMenuRef = new MutableReference<Menu>();
                    
                    toolItemListener = new SelectionAdapter()
                    {
                        @Override
                        public void widgetSelected( final SelectionEvent event )
                        {
                            Menu popupMenu = popupMenuRef.get();
                            
                            if( popupMenu != null )
                            {
                                popupMenu.dispose();
                            }
                            
                            popupMenu = new Menu( toolbar.getShell(), SWT.POP_UP );
                            popupMenuRef.set( popupMenu );
                            fillMenu( popupMenu, childActionGroups );
                            
                            final Rectangle rect = toolItem.getBounds();
                            Point pt = new Point( rect.x, rect.y + rect.height );
                            pt = toolItem.getParent().toDisplay( pt );
                            
                            popupMenu.setLocation( pt );
                            popupMenu.setVisible( true );
                        }
                    };
                }
                else
                {
                    toolItemListener = new SelectionAdapter()
                    {
                        @Override
                        public void widgetSelected( final SelectionEvent event )
                        {
                            if( action.getType() == Action.Type.TOGGLE )
                            {
                                action.setChecked( ! action.isChecked() );
                            }
                            
                            action.execute( toolbar.getShell() );
                        }
                    };
                }
                
                toolItem.addSelectionListener( toolItemListener );
                
                final Action.Listener actionChangeListener = new Action.Listener()
                {
                    @Override
                    public void handleActionChanged( final Action action )
                    {
                        toolItem.setImage( action.getImage( true ) );
                        toolItem.setToolTipText( action.getLabel() );
                        toolItem.setEnabled( action.isEnabled() );
                    }
                };
                
                action.addListener( actionChangeListener );
                
                toolItem.addDisposeListener
                (
                    new DisposeListener()
                    {
                        public void widgetDisposed( final DisposeEvent event ) 
                        {
                            action.removeListener( actionChangeListener );
                        }
                    }
                );
            }
        }
        
        if( ! first && includeTrailingSeparator )
        {
            new ToolItem( toolbar, SWT.SEPARATOR );
        }
        
        toolbar.getAccessible().addAccessibleListener
        (
            new AccessibleAdapter()
            {
                @Override
                public void getName( final AccessibleEvent event )
                {
                    if( event.childID != ACC.CHILDID_SELF ) 
                    {
                        ToolItem item = toolbar.getItem( event.childID );
                        
                        if( item != null ) 
                        {
                            final String toolTip = item.getToolTipText();
                            
                            if( toolTip != null ) 
                            {
                                event.result = toolTip;
                            }
                        }
                    }
                }
            }
        );
    }

    public static void fillToolBarManager( final IToolBarManager toolbarManager,
                                           final Shell shell,
                                           final List<ActionGroup> groups )
    {
        boolean first = true;
        
        for( ActionGroup group : groups )
        {
            if( ! group.isVisible() )
            {
                continue;
            }
            
            if( first )
            {
                first = false;
            }
            else
            {
                toolbarManager.add( new Separator() );
            }
            
            for( final Action action : group.getActions() )
            {
                if( ! action.isVisible() )
                {
                    continue;
                }
                
                final List<ActionGroup> childActionGroups = action.getChildActionGroups();
                final MutableReference<Menu> popupMenuRef = new MutableReference<Menu>();
                
                final int actionStyle
                    = ( action.getType() == Action.Type.TOGGLE 
                        ? org.eclipse.jface.action.Action.AS_CHECK_BOX 
                        : org.eclipse.jface.action.Action.AS_PUSH_BUTTON );
                
                final org.eclipse.jface.action.Action a = new org.eclipse.jface.action.Action( null, actionStyle )
                {
                    @Override
                    public void run()
                    {
                        if( ! childActionGroups.isEmpty() )
                        {
                            Menu popupMenu = popupMenuRef.get();
                            
                            if( popupMenu != null )
                            {
                                popupMenu.dispose();
                            }
                            
                            popupMenu = new Menu( shell, SWT.POP_UP );
                            popupMenuRef.set( popupMenu );
                            fillMenu( popupMenu, childActionGroups );
                            
                            ToolItem actionToolItem = null;
                            
                            for( ToolItem toolItem : getToolBar( toolbarManager ).getItems() )
                            {
                                final Object data = toolItem.getData();
                                
                                if( data instanceof IContributionItem && getAction( ( (IContributionItem) data ) ) == this )
                                {
                                    actionToolItem = toolItem;
                                    break;
                                }
                            }
                            
                            final Rectangle rect = actionToolItem.getBounds();
                            Point pt = new Point( rect.x, rect.y + rect.height );
                            pt = actionToolItem.getParent().toDisplay( pt );
                            
                            popupMenu.setLocation( pt );
                            popupMenu.setVisible( true );
                        }
                        else
                        {
                            if( action.getType() == Action.Type.TOGGLE )
                            {
                                action.setChecked( ! action.isChecked() );
                            }
                            
                            action.execute( shell );
                        }
                    }
                    
                    private IAction getAction( final IContributionItem item )
                    {
                        if( item instanceof ActionContributionItem )
                        {
                            return ( (ActionContributionItem) item ).getAction();
                        }
                        else if( item instanceof SubContributionItem )
                        {
                            return getAction( ( (SubContributionItem) item ).getInnerItem() );
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
                };
                
                a.setImageDescriptor( ImageDescriptor.createFromImage( action.getImage( true ) ) );
                a.setToolTipText( action.getLabel() );
                a.setEnabled( action.isEnabled() );

                if( action.getType() == Action.Type.TOGGLE )
                {
                    a.setChecked( action.isChecked() );
                }
                
                final Action.Listener actionChangeListener = new Action.Listener()
                {
                    @Override
                    public void handleActionChanged( final Action action )
                    {
                        a.setImageDescriptor( ImageDescriptor.createFromImage( action.getImage( true ) ) );
                        a.setToolTipText( action.getLabel() );
                        a.setEnabled( action.isEnabled() );
                    }
                };
                
                action.addListener( actionChangeListener );
                
                toolbarManager.add( a );
            }
        }
        
        toolbarManager.update( true );
    }
    
}
