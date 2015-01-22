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
import java.util.List;

import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.def.KeyBindingBehavior;
import org.eclipse.sapphire.ui.def.SapphireKeySequence;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Widget;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SapphireKeyboardActionPresentation extends SapphireHotSpotsActionPresentation
{
    private final List<Widget> attachedControls = new ArrayList<Widget>();
    private Listener keyListener;
    private Listener traverseListener;

    public SapphireKeyboardActionPresentation( final SapphireActionPresentationManager manager )
    {
        super( manager );
    }
    
    public void attach( final Control control )
    {
        // Ignore plain composites. Attaching a key listener to a control forces it to become a tab stop,
        // which is undesirable for plain composites. See discussion in KeyEvent and TraverseEvent. 

        if( control.getClass() != Composite.class && ! ( control instanceof RadioButtonsGroup ) )
        {
            this.attachedControls.add( control );
            
            control.addDisposeListener
            (
                new DisposeListener()
                {
                    public void widgetDisposed( final DisposeEvent event )
                    {
                        detach( control );
                    }
                }
            );
        }
    }
    
    public void detach( final Control control )
    {
        this.attachedControls.remove( control );
    }
    
    public void render()
    {
        this.keyListener = new Listener()
        {
            public void handleEvent( final Event event )
            {
                handleKeyEvent( event );
            }
        };
        
        this.traverseListener = new Listener()
        {
            public void handleEvent( Event event )
            {
                if( event.widget instanceof ToolBar )
                {
                    event.doit = true;
                }
            }
        };
        
        for( Widget control : this.attachedControls )
        {
            if( control.getClass() != Composite.class )
            {
                control.addListener( SWT.KeyDown, this.keyListener );
                control.addListener( SWT.Traverse, this.traverseListener );
            }
        }
    }
    
    private void handleKeyEvent( final Event event )
    {
        final SapphireActionGroup localGroupOfActions = getManager().getActionGroup();

        if( handleKeyEvent( event, localGroupOfActions, false ) )
        {
            return;
        }
        
        // Account for propagated key bindings. Start the search with this part's 
        // main action context and continue up the parts hierarchy.
        
        ISapphirePart part = localGroupOfActions.getPart();
        
        while( part != null )
        {
            final String mainActionContext = part.getMainActionContext();
            
            if( mainActionContext != null )
            {
                final SapphireActionGroup groupOfActions = part.getActions( mainActionContext );
                
                if( handleKeyEvent( event, groupOfActions, true ) )
                {
                    return;
                }
            }
            
            part = part.parent();
        }
    }
    
    private boolean handleKeyEvent( final Event event,
                                    final SapphireActionGroup groupOfActions,
                                    final boolean onlyPropagatedKeyBindings )
    {
        for( SapphireAction action : groupOfActions.getActions() )
        {
            if( action.hasActiveHandlers() && ( ! onlyPropagatedKeyBindings || action.getKeyBindingBehavior() == KeyBindingBehavior.PROPAGATED ) )
            {
                final SapphireKeySequence keySequence = action.getKeyBinding();
                
                if( keySequence != null )
                {
                    int expectedStateMask = 0;
                    
                    for( SapphireKeySequence.Modifier modifier : keySequence.getModifiers() )
                    {
                        if( modifier == SapphireKeySequence.Modifier.SHIFT )
                        {
                            expectedStateMask = expectedStateMask | SWT.SHIFT;
                        }
                        else if( modifier == SapphireKeySequence.Modifier.ALT )
                        {
                            expectedStateMask = expectedStateMask | SWT.ALT;
                        }
                        else if( modifier == SapphireKeySequence.Modifier.CONTROL )
                        {
                            expectedStateMask = expectedStateMask | SWT.CONTROL;
                        }
                    }
                    
                    if( event.stateMask == expectedStateMask && event.keyCode == keySequence.getKeyCode() )
                    {
                        final List<SapphireActionHandler> handlers = action.getEnabledHandlers();
                        
                        if( handlers.size() == 1 )
                        {
                            final SapphireActionHandler handler = handlers.get( 0 );
                            
                            final Runnable runnable = new Runnable()
                            {
                                public void run()
                                {
                                    handler.execute( getManager().context() );
                                }
                            };
                            
                            ( (SwtPresentation) getManager().context() ).display().asyncExec( runnable );
                        }
                        else
                        {
                            for( SapphireActionPresentation presentation : getManager().getPresentations() )
                            {
                                if( presentation.displayActionHandlerChoice( action ) )
                                {
                                    break;
                                }
                            }
                        }

                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        for( Widget widget : this.attachedControls )
        {
            if( ! widget.isDisposed() )
            {
                widget.removeListener( SWT.KeyDown, this.keyListener );
                widget.removeListener( SWT.Traverse, this.traverseListener );
            }
        }
        
        this.attachedControls.clear();
    }
    
}
