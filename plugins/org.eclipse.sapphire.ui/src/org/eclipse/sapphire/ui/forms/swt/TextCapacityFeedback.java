/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt;

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Displays the number of characters remaining before the length limit is reached for a text field. Typically placed
 * beneath a text field.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TextCapacityFeedback extends Composite
{
    @Text( "{0} characters left" )
    private static LocalizableText underLimitMessage;
    
    @Text( "1 character left" )
    private static LocalizableText underLimitByOneMessage;
    
    @Text( "No characters left" )
    private static LocalizableText atLimitMessage;
    
    @Text( "{0} characters over the limit" )
    private static LocalizableText overLimitMessage;

    @Text( "1 character over the limit" )
    private static LocalizableText overLimitByOneMessage;
    
    static
    {
        LocalizableText.init( TextCapacityFeedback.class );
    }

    private final org.eclipse.swt.widgets.Text textField;
    private final int limit;
    private final Label label;
    private final Progress progress;
    
    public TextCapacityFeedback( final Composite parent, final org.eclipse.swt.widgets.Text textField, final int limit )
    {
        super( parent, SWT.NONE );
        
        this.textField = textField;
        this.limit = limit;
        
        setLayout( glayout( 2, 0, 0 ) );
        
        this.label = new Label( this, SWT.NONE );
        this.label.setLayoutData( gdhfill() );
        this.label.setForeground( getDisplay().getSystemColor( SWT.COLOR_DARK_GRAY ) );
        
        this.progress = new Progress( this );
        this.progress.setLayoutData( gd() );
        
        this.textField.addModifyListener
        (
            new ModifyListener()
            {
                @Override
                
                public void modifyText( final ModifyEvent event )
                {
                    refresh();
                }
            }
        );
        
        refresh();
    }
    
    private void refresh()
    {
        final int p = this.textField.getText().length();
        final int r = this.limit - p;
        final String progressLabel;
        
        if( r == 0 )
        {
            progressLabel = atLimitMessage.text();
        }
        else if( r == 1 )
        {
            progressLabel = underLimitByOneMessage.text();
        }
        else if( r == -1 )
        {
            progressLabel = overLimitByOneMessage.text();
        }
        else if( r > 1 )
        {
            progressLabel = underLimitMessage.format( r );
        }
        else
        {
            progressLabel = overLimitMessage.format( r * -1 );
        }
        
        this.label.setText( progressLabel );
        this.progress.progress( (double) p / this.limit );
        
        if( p == 0 )
        {
            this.progress.setVisible( false );;
        }
        else
        {
            this.progress.progress( (double) p / this.limit );
            this.progress.setVisible( true );
        }
    }
    
    private static final class Progress extends Canvas
    {
        private double progress;
        
        public Progress( final Composite parent )
        {
            super( parent, SWT.NONE );
            
            addPaintListener
            (
                new PaintListener()
                {
                    @Override
                    
                    public void paintControl( final PaintEvent event )
                    {
                        paint( event );
                    }
                }
            );
        }
        
        public void progress( final double progress )
        {
            this.progress = progress;
            
            redraw();
        }
        
        @Override
        
        public Point computeSize( final int wHind, final int hHint, boolean changed )
        {
            return new Point( 140, 9 );
        }
        
        private void paint( final PaintEvent event )
        {
            final GC gc = event.gc;
            
            final int p;
            final int r;
            final boolean over;
            
            if( this.progress > 1d )
            {
                p = 138;
                r = 0;
                over = true;
            }
            else
            {
                p = (int) ( this.progress * 138 );
                r = 138 - p;
                over = false;
            }
            
            gc.setForeground( getDisplay().getSystemColor( SWT.COLOR_GRAY ) );
            gc.drawRectangle( 0, 0, 139, 8 );
            
            if( p > 0 )
            {
                gc.setBackground( getDisplay().getSystemColor( over ? SWT.COLOR_RED : SWT.COLOR_GRAY ) );
                gc.fillRectangle( 1, 1, p, 7 );
            }
            
            if( r > 0 )
            {
                gc.setBackground( getDisplay().getSystemColor( SWT.COLOR_WHITE ) );
                gc.fillRectangle( p + 1, 1, r, 7 );
            }
        }
    }
    
}
