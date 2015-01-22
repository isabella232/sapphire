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

package org.eclipse.sapphire.ui.forms.swt.internal;

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdwhint;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * Implements Ctrl+Click navigation in a standard SWT table widget. 
 * 
 * @author <a href="konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class HyperlinkTable
{
    public static abstract class Controller
    {
        public boolean isHyperlinkEnabled( final TableItem item,
                                           final int column )
        {
            return true;
        }
        
        public abstract void handleHyperlinkEvent( final TableItem item,
                                                   final int column );
    }
    
    @Text( "Select Jump Destination" )
    private static LocalizableText jumpDialogTitle;
    
    @Text( "Where do you want to jump to?" )
    private static LocalizableText jumpDialogPrompt;

    static
    {
        LocalizableText.init( HyperlinkTable.class );
    }

    private static final Point IMAGE_OFFSET_PRIMARY_COLUMN;
    private static final Point IMAGE_OFFSET_SECONDARY_COLUMN;
    private static final Point TEXT_OFFSET_PRIMARY_COLUMN;
    private static final Point TEXT_OFFSET_SECONDARY_COLUMN;
    
    static
    {
        final String os = Platform.getOS();
        final String osName = System.getProperties().getProperty( "os.name" );
        
        if( os.equals( Platform.OS_WIN32 ) )
        {
            if( osName.equals( "Windows XP" ) )
            {
                IMAGE_OFFSET_PRIMARY_COLUMN = new Point( 0, 0 );
                IMAGE_OFFSET_SECONDARY_COLUMN = new Point( 0, 0 );
                TEXT_OFFSET_PRIMARY_COLUMN = new Point( 0, 2 );
                TEXT_OFFSET_SECONDARY_COLUMN = new Point( -1, 2 );
            }
            else
            {
                IMAGE_OFFSET_PRIMARY_COLUMN = new Point( 0, 1 );
                IMAGE_OFFSET_SECONDARY_COLUMN = new Point( 0, 1 );
                TEXT_OFFSET_PRIMARY_COLUMN = new Point( 0, 2 );
                TEXT_OFFSET_SECONDARY_COLUMN = new Point( 0, 2 );
            }
        }
        else
        {
            // This number has been derived on openSUSE 11.0, but we will use it
            // for all non-windows systems for now.
            
            IMAGE_OFFSET_PRIMARY_COLUMN = new Point( 1, 3 );
            IMAGE_OFFSET_SECONDARY_COLUMN = new Point( 1, 3 );
            TEXT_OFFSET_PRIMARY_COLUMN = new Point( 1, 3 );
            TEXT_OFFSET_SECONDARY_COLUMN = new Point( 1, 3 );
        }
    }
    
    private boolean controlKeyActive;
    private final Table table;
    private TableItem mouseOverTableItem;
    private int mouseOverColumn;
    private Controller controller;
    
    public HyperlinkTable( final Table table,
                           final SapphireActionGroup actions )
    {
        this.table = table;
        this.controlKeyActive = false;
        this.mouseOverTableItem = null;
        this.mouseOverColumn = -1;
        
        final Listener keyListener = new Listener() 
        {
            public void handleEvent( final Event event ) 
            {
                handleKeyEvent( event );
            }
        };
        
        final Display display = this.table.getDisplay();
        
        display.addFilter( SWT.KeyDown, keyListener );
        display.addFilter( SWT.KeyUp, keyListener );
        
        this.table.addListener
        (
            SWT.EraseItem, 
            new Listener() 
            {
                public void handleEvent( final Event event ) 
                {
                    handleEraseItem( event );
                }
            }
        );

        this.table.addListener
        (
            SWT.PaintItem, 
            new Listener() 
            {
                public void handleEvent( final Event event ) 
                {
                    handlePaintItem( event );
                }
            }
        );

        this.table.addListener
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

        this.table.addListener
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

        this.table.addListener
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
        
        final SapphireActionHandler jumpActionHandler = new SapphireActionHandler()
        {
            @Override
            protected Object run( final Presentation context )
            {
                handleJumpCommand();
                return null;
            }
        };
        
        final SapphireAction jumpAction = actions.getAction( SapphireActionSystem.ACTION_JUMP );
        jumpActionHandler.init( jumpAction, null );
        jumpAction.addHandler( jumpActionHandler );
        
        this.table.addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    display.removeFilter( SWT.KeyDown, keyListener );
                    display.removeFilter( SWT.KeyUp, keyListener );
                    jumpAction.removeHandler( jumpActionHandler );
                }
            }
        );
    }
    
    public void setController( final Controller controller )
    {
        this.controller = controller;
    }
    
    public Table getTable()
    {
        return this.table;
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
        this.mouseOverTableItem = null;
        this.mouseOverColumn = -1;
        
        for( int i = this.table.getTopIndex(), n = this.table.getItemCount(); i < n; i++ )
        {
            final TableItem item = this.table.getItem( i );
            
            for( int j = 0, m = getColumnCount( this.table ); j < m; j++ )
            {
                final Rectangle bounds = item.getTextBounds( j );
                
                if( bounds.contains( event.x, event.y ) )
                {
                    final GC gc = new GC( this.table );
                    final Point textExtent = gc.textExtent( item.getText( j ) );
                    gc.dispose();
                    
                    bounds.width = textExtent.x;
                    bounds.height = textExtent.y;
                    
                    if( bounds.contains( event.x, event.y ) && this.controller.isHyperlinkEnabled( item, j ) )
                    {
                        this.mouseOverTableItem = item;
                        this.mouseOverColumn = j;
                    }

                    break;
                }
            }
        }
        
        update();
    }
    
    private void handleMouseExit( final Event event )
    {
        this.mouseOverTableItem = null;
        this.mouseOverColumn = -1;

        update();
    }
    
    private void handleMouseDown( final Event event )
    {
        if( this.controlKeyActive && this.mouseOverTableItem != null )
        {
            final TableItem item = this.mouseOverTableItem;
            
            this.table.setCursor( null );
            
            // Ideally, it would be best to prevent table selection from taking place. Haven't found
            // a way to do that yet. At the very least, the following makes sure that Ctrl+Click hyperlink
            // action doesn't also have a multi-select behavior.
            
            this.table.setSelection( item );

            handleJumpCommand( this.mouseOverTableItem, this.mouseOverColumn );
        }
    }
    
    private void handleJumpCommand()
    {
        final TableItem[] items = HyperlinkTable.this.table.getSelection();
        
        if( items.length == 1 )
        {
            final TableItem item = items[ 0 ];
            final List<Integer> columnsWithHyperlinks = new ArrayList<Integer>();
            
            for( int i = 0, n = getColumnCount( HyperlinkTable.this.table ); i < n; i++ )
            {
                if( this.controller.isHyperlinkEnabled( item, i ) )
                {
                    columnsWithHyperlinks.add( i );
                }
            }
            
            if( columnsWithHyperlinks.size() == 1 )
            {
                handleJumpCommand( item, columnsWithHyperlinks.get( 0 ) );
            }
            else if( ! columnsWithHyperlinks.isEmpty() )
            {
                final Dialog dialog = new Dialog( this.table.getShell() )
                {
                    private int choice = columnsWithHyperlinks.get( 0 );
                    
                    @Override
                    protected Control createDialogArea( final Composite parent )
                    {
                        getShell().setText( jumpDialogTitle.text() );
                        
                        final Composite composite = (Composite) super.createDialogArea( parent );
                        
                        final Label prompt = new Label( composite, SWT.WRAP );
                        prompt.setLayoutData( gdwhint( gdhfill(), 300 ) );
                        prompt.setText( jumpDialogPrompt.text() );
                        
                        final SelectionListener listener = new SelectionAdapter()
                        {
                            public void widgetSelected( final SelectionEvent event )
                            {
                                setChoice( (Integer) event.widget.getData() );
                            }
                        };
                        
                        boolean first = true;
                        
                        for( Integer col : columnsWithHyperlinks )
                        {
                            final Button button = new Button( composite, SWT.RADIO | SWT.WRAP );
                            button.setLayoutData( gdhindent( gd(), 10 ) );
                            button.setText( item.getText( col ) );
                            button.setData( col );
                            
                            if( first )
                            {
                                button.setSelection( true );
                                first = false;
                            }
                            
                            button.addSelectionListener( listener );
                        }
                        
                        return composite;
                    }

                    @Override
                    protected void okPressed()
                    {
                        super.okPressed();
                        handleJumpCommand( item, this.choice );
                    }
                    
                    private void setChoice( final int choice )
                    {
                        this.choice = choice;
                    }
                };
                
                dialog.open();
            }
        }
    }
    
    private void handleJumpCommand( final TableItem item,
                                    final int column )
    {
        final Runnable op = new Runnable()
        {
            public void run()
            {
                HyperlinkTable.this.controller.handleHyperlinkEvent( item, column );
            }
        };
        
        BusyIndicator.showWhile( this.table.getDisplay(), op );
    }
    
    private void handleEraseItem( final Event event ) 
    {
        final TableItem item = (TableItem) event.item;
        
        if( this.controlKeyActive && this.mouseOverTableItem == item )
        {
            event.detail &= ~SWT.FOREGROUND;
        }
    }
    
    private void handlePaintItem( final Event event ) 
    {
        final TableItem item = (TableItem) event.item;
        
        if( this.controlKeyActive && this.mouseOverTableItem == item )
        {
            for( int i = 0, n = getColumnCount( this.table ); i < n; i++ )
            {
                final Display display = this.table.getDisplay();
                final String text = item.getText( i );

                final Font font = item.getFont( i );
                final TextStyle style = new TextStyle( font, null, null );
                
                if( this.mouseOverColumn == i )
                {
                    final Color hyperlinkColor = JFaceColors.getActiveHyperlinkText( display );
                    style.underline = true;
                    style.foreground = hyperlinkColor;
                    style.underlineColor = hyperlinkColor;
                }
                
                final Image image = item.getImage( i );
                
                if( image != null )
                {
                    final Rectangle bounds = item.getBounds( i );
                    final Point offset = ( i == 0 ? IMAGE_OFFSET_PRIMARY_COLUMN : IMAGE_OFFSET_SECONDARY_COLUMN );
                    event.gc.drawImage( image, bounds.x + offset.x, bounds.y + offset.y );
                }
                
                final TextLayout layout = new TextLayout( display );
                layout.setText( text );
                layout.setStyle( style, 0, text.length() - 1 );
                
                final Point offset = ( i == 0 ? TEXT_OFFSET_PRIMARY_COLUMN : TEXT_OFFSET_SECONDARY_COLUMN );
                
                final Rectangle clientArea = item.getTextBounds( i );
                layout.setWidth( clientArea.width );
                layout.draw( event.gc, clientArea.x + offset.x, clientArea.y + offset.y );
            }
        }
    }
    
    private void update()
    {
        if( this.controlKeyActive && this.mouseOverTableItem != null )
        {
            this.table.setCursor( this.table.getDisplay().getSystemCursor( SWT.CURSOR_HAND ) );
        }
        else
        {
            this.table.setCursor( null );
        }
        
        this.table.redraw();
    }
    
    private static int getColumnCount( final Table table )
    {
        final int count = table.getColumnCount();
        return ( count == 0 ? 1 : count );
    }
    
}
