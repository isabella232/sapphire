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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.def.SapphireKeySequence;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Control;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireKeyboardActionPresentation

    extends SapphireHotSpotsActionPresentation
    
{
    private final List<Control> attachedControls = new ArrayList<Control>();
    private KeyListener keyListener;

    public SapphireKeyboardActionPresentation( final SapphireActionPresentationManager manager )
    {
        super( manager );
    }
    
    public void attach( final Control control )
    {
        this.attachedControls.add( control );
    }
    
    public void render()
    {
        this.keyListener = new KeyAdapter()
        {
            @Override
            public void keyPressed( final KeyEvent event )
            {
                handleKeyEvent( event );
            }
        };
        
        for( Control control : this.attachedControls )
        {
            control.addKeyListener( this.keyListener );
        }
    }
    
    private void handleKeyEvent( final KeyEvent event )
    {
        for( SapphireAction action : getManager().getActions() )
        {
            if( action.hasActiveHandlers() )
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
                        final List<SapphireActionHandler> handlers = action.getActiveHandlers();
                        
                        if( handlers.size() == 1 )
                        {
                            final SapphireActionHandler handler = handlers.get( 0 );
                            
                            final Runnable runnable = new Runnable()
                            {
                                public void run()
                                {
                                    handler.execute( getManager().getContext() );
                                }
                            };
                            
                            getManager().getContext().getDisplay().asyncExec( runnable );
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

                        return;
                    }
                }
            }
        }
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        for( Control control : this.attachedControls )
        {
            if( ! control.isDisposed() )
            {
                control.removeKeyListener( this.keyListener );
            }
        }
        
        this.attachedControls.clear();
    }
    
}
