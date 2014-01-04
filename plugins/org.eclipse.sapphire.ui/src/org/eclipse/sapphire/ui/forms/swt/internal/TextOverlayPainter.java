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

package org.eclipse.sapphire.ui.forms.swt.internal;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * Implements Ctrl+Click navigation in a standard SWT Text widget. 
 * 
 * @author <a href="konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TextOverlayPainter
{
    public static abstract class Controller
    {
        public boolean isHyperlinkEnabled()
        {
            return false;
        }
        
        public void handleHyperlinkEvent()
        {
            throw new UnsupportedOperationException();
        }
        
        public String overlay()
        {
            return null;
        }
    }
    
    private static final Point TEXT_OFFSET;
    
    static
    {
        final String os = Platform.getOS();
        
        if( os.equals( Platform.OS_WIN32 ) )
        {
            TEXT_OFFSET = new Point( 4, 1 );
        }
        else
        {
            // This number has been derived on openSUSE 11.0, but we will use it
            // for all non-windows systems for now.
            
            TEXT_OFFSET = new Point( 2, 2 );
        }
    }
    
    private final Display display;
    private final Text textControl;
    private final Controller controller;
    private boolean controlKeyActive;
    private Point textExtent;
    private boolean hyperlinkActive;
    private boolean mouseOverText;
    
    private TextOverlayPainter( final Text textControl,
                                final Controller controller )
    {
        this.display = textControl.getDisplay();
        this.textControl = textControl;
        this.controller = controller;
        this.controlKeyActive = false;
        this.hyperlinkActive = false;
        this.mouseOverText = false;
        
        final Listener keyListener = new Listener() 
        {
            public void handleEvent( final Event event ) 
            {
                handleKeyEvent( event );
            }
        };
        
        this.display.addFilter( SWT.KeyDown, keyListener );
        this.display.addFilter( SWT.KeyUp, keyListener );
        
        this.textControl.addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    TextOverlayPainter.this.display.removeFilter( SWT.KeyDown, keyListener );
                    TextOverlayPainter.this.display.removeFilter( SWT.KeyUp, keyListener );
                }
            }
        );
        
        this.textControl.addModifyListener
        (
             new ModifyListener()
             {
                public void modifyText( final ModifyEvent event )
                {
                    updateTextExtent();
                }
             }
        );
        
        updateTextExtent();

        this.textControl.addListener
        (
            SWT.Paint, 
            new Listener() 
            {
                public void handleEvent( final Event event ) 
                {
                    handlePaint( event );
                }
            }
        );

        this.textControl.addListener
        (
            SWT.MouseMove,
            new Listener() 
            {
                public void handleEvent( final Event event ) 
                {
                    handleMouseMove( event );
                }
            }
        );

        this.textControl.addListener
        (
            SWT.MouseExit,
            new Listener() 
            {
                public void handleEvent( final Event event ) 
                {
                    handleMouseExit( event );
                }
            }
        );

        this.textControl.addListener
        (
            SWT.MouseDown,
            new Listener() 
            {
                public void handleEvent( final Event event ) 
                {
                    handleMouseDown( event );
                }
            }
        );
    }
    
    public static void install( final Text textControl,
                                final Controller controller )
    {
        new TextOverlayPainter( textControl, controller );
    }
    
    private void handleKeyEvent( final Event event )
    {
        if( event.keyCode == SWT.CONTROL )
        {
            this.controlKeyActive = ( event.type == SWT.KeyDown );
            
            // Only force update when user releases the control key. We want the hyperlink
            // to show only after user starts moving the mouse after holding down the
            // control key.
            
            if( ! this.controlKeyActive )
            {
                update();
            }
        }
    }
    
    private void handleMouseMove( final Event event )
    {
        if( event.x <= this.textExtent.x && event.y <= this.textExtent.y )
        {
            this.mouseOverText = true;
        }
        else
        {
            this.mouseOverText = false;
        }
        
        update();
    }
    
    private void handleMouseExit( final Event event )
    {
        this.mouseOverText = false;
        update();
    }
    
    private void handleMouseDown( final Event event )
    {
        if( this.hyperlinkActive )
        {
            this.textControl.setCursor( null );
            handleJumpCommand();
        }
    }
    
    private void handleJumpCommand()
    {
        final Runnable op = new Runnable()
        {
            public void run()
            {
                TextOverlayPainter.this.controller.handleHyperlinkEvent();
            }
        };
        
        BusyIndicator.showWhile( this.display, op );
    }
    
    private void update()
    {
        final boolean shouldHyperlinkBeActive = ( this.controlKeyActive && this.mouseOverText && this.controller.isHyperlinkEnabled() );
        
        if( this.hyperlinkActive != shouldHyperlinkBeActive )
        {
            this.hyperlinkActive = shouldHyperlinkBeActive;
            this.textControl.setCursor( shouldHyperlinkBeActive ? this.display.getSystemCursor( SWT.CURSOR_HAND ) : null );
            this.textControl.redraw();
        }
    }
    
    private void updateTextExtent()
    {
        final GC gc = new GC( this.textControl );
        this.textExtent = gc.textExtent( getTextWithOverlay() );
        gc.dispose();
    }
    
    private void handlePaint( final Event event ) 
    {
        if( this.textControl.isEnabled() )
        {
            if( this.hyperlinkActive )
            {
                final TextStyle style = new TextStyle( this.textControl.getFont(), null, null );
                style.underline = true;
                style.foreground = JFaceColors.getActiveHyperlinkText( this.display );
                style.underlineColor = style.foreground;
                
                paintTextOverlay( event.gc, style, getTextWithOverlay() );
            }
            else if( ! this.textControl.isFocusControl() && this.textControl.getText().length() == 0 )
            {
                final String overlay = this.controller.overlay();
                
                if( overlay != null && overlay.length() > 0 )
                {
                    final TextStyle style = new TextStyle( this.textControl.getFont(), null, null );
                    style.foreground = this.display.getSystemColor( SWT.COLOR_GRAY );
                    
                    paintTextOverlay( event.gc, style, overlay );
                }
            }
        }
    }
    
    private void paintTextOverlay( final GC gc,
                                   final TextStyle style,
                                   final String text )
    {
        final TextLayout layout = new TextLayout( this.display );
        layout.setText( text );
        layout.setStyle( style, 0, text.length() - 1 );
        
        final Rectangle clientArea = this.textControl.getClientArea();
        gc.fillRectangle( clientArea );

        layout.setWidth( clientArea.width - TEXT_OFFSET.x * 2 );
        layout.draw( gc, TEXT_OFFSET.x, TEXT_OFFSET.y );
    }
    
    private String getTextWithOverlay()
    {
        String text = this.textControl.getText();
        
        if( text.length() == 0 )
        {
            text = this.controller.overlay();
            
            if( text == null )
            {
                text = "";
            }
        }

        return text;
    }
    
}
