/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [329102] excess scroll space in editor sections
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.sapphire.ui.forms.swt.internal.ColumnSortComparator;
import org.eclipse.sapphire.ui.forms.swt.internal.text.SapphireFormText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.SharedScrolledComposite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SwtUtil
{
    public static void setEnabledOnChildren( final Composite composite,
                                             final boolean enabled )
    {
        for( Control child : composite.getChildren() )
        {
            child.setEnabled( enabled );
        }
    }
    
    public static void makeTableSortable( final TableViewer tableViewer )
    {
        final Map<TableColumn,Comparator<Object>> comparators = Collections.emptyMap();
        makeTableSortable( tableViewer, comparators );
    }
    
    public static void makeTableSortable( final TableViewer tableViewer,
                                          final Map<TableColumn,Comparator<Object>> comparators )
    {
        makeTableSortable( tableViewer, comparators, tableViewer.getTable().getColumn( 0 ) );
    }
    
    public static void makeTableSortable( final TableViewer tableViewer,
                                          final Map<TableColumn,Comparator<Object>> comparators,
                                          final TableColumn initialSortColumn )
    {
        makeTableSortable( tableViewer, comparators, initialSortColumn, SWT.DOWN );
    }
    
    public static void makeTableSortable( final TableViewer tableViewer,
                                          final Map<TableColumn,Comparator<Object>> comparators,
                                          final TableColumn initialSortColumn,
                                          final int initialSortDirection )
    {
        final Table table = tableViewer.getTable();
        
        if( initialSortColumn != null )
        {
            sortByTableColumn( tableViewer, initialSortColumn, initialSortDirection, comparators.get( initialSortColumn ) );
        }
        
        for( final TableColumn column : table.getColumns() )
        {
            final Comparator<Object> comparator = comparators.get( column );
            
            column.addSelectionListener
            (
                new SelectionAdapter()
                {
                    @Override
                    public void widgetSelected( final SelectionEvent event )
                    {
                        final TableColumn currentSortColumn = table.getSortColumn();
                        
                        if( currentSortColumn != column )
                        {
                            sortByTableColumn( tableViewer, column, SWT.DOWN, comparator );
                        }
                        else
                        {
                            final int currentSortDirection = table.getSortDirection();
                            
                            if( currentSortDirection == SWT.DOWN )
                            {
                                sortByTableColumn( tableViewer, column, SWT.UP, comparator );
                            }
                            else
                            {
                                table.setSortColumn( null );
                                tableViewer.setComparator( null );
                            }
                        }
                    }
                }
            );
        }
    }
    
    public static void sortByTableColumn( final TableViewer tableViewer,
                                          final TableColumn column,
                                          final int direction,
                                          final Comparator<Object> comparator )
    {
        final Table table = tableViewer.getTable();
        
        table.setSortColumn( column );
        table.setSortDirection( direction );
        
        final Comparator<Object> comp;
        
        if( comparator != null )
        {
            comp = comparator;
        }
        else
        {
            comp = new ColumnSortComparator();
        }
        
        tableViewer.setComparator
        (
            new ViewerComparator()
            {
                @Override
                public int compare( final Viewer viewer,
                                    final Object x,
                                    final Object y )
                {
                    int result = comp.compare( x, y );
                    
                    if( direction == SWT.UP )
                    {
                        result = result * -1;
                    }
                 
                    return result;
                }
            }
        );
    }
    
    /**
     * Suppresses the display of the rather unnecessary secondary dotted line around the selected row.
     */
    
    public static void suppressDashedTableEntryBorder( final Table table )
    {
        table.addListener
        ( 
            SWT.EraseItem, 
            new Listener() 
            {
                public void handleEvent( final Event event ) 
                {
                    event.detail &= ~SWT.FOCUSED;
                }
            }
        );
    }
    
    public static void reflowOnResize( final Control control )
    {
        final GridData gd = (GridData) control.getLayoutData();
        final int originalWidthHint = gd.widthHint;
        
        Composite parent = control.getParent();
        
        while( parent != null && ! ( parent instanceof SharedScrolledComposite || parent instanceof Shell ) ) 
        {
            parent = parent.getParent();
        }
        
        final Composite topLevelComposite = parent;
        
        control.addControlListener
        (
            new ControlAdapter() 
            {
                @Override
                public void controlResized( final ControlEvent event ) 
                {
                    final Rectangle bounds = control.getBounds();
                    
                    if( bounds.width != gd.widthHint + 20 ) 
                    {
                        if( bounds.width == gd.widthHint ) 
                        {
                            gd.widthHint = originalWidthHint;
                        }
                        else
                        {
                            gd.widthHint = bounds.width - 20;
                        }
                        
                        control.getDisplay().asyncExec
                        (
                            new Runnable() 
                            {
                                public void run() 
                                {
                                    if( topLevelComposite.isDisposed() )
                                    {
                                        return;
                                    }
                                    
                                    topLevelComposite.layout( true, true );

                                    if( topLevelComposite instanceof SharedScrolledComposite )
                                    {
                                        ( (SharedScrolledComposite) topLevelComposite ).reflow( true );
                                    }
                                }
                            }
                        );
                    }
                }
            }
        );
    }
    
    public static void changeRadioButtonSelection( final List<Button> group,
                                                   final Button buttonToSelect )
    {
        final Control focusControl = buttonToSelect.getDisplay().getFocusControl();
        boolean groupHasFocus = false;
        
        for( final Button b : group )
        {
            if( b != buttonToSelect )
            {
                b.setSelection( false );
            }
            
            if( b == focusControl )
            {
                groupHasFocus = true;
            }
        }
        
        buttonToSelect.setSelection( true );
        
        if( groupHasFocus )
        {
            buttonToSelect.setFocus();
        }
    }
    
    public static void runOnDisplayThread( final Runnable op,
                                           final Display display )
    {
        if( display.getThread() == Thread.currentThread() )
        {
            op.run();
        }
        else
        {
            display.asyncExec( op );
        }
    }

    public static void runOnDisplayThread( final Runnable op,
                                           final Control control )
    {
        runOnDisplayThread( op, control.getDisplay() );
    }
    
    public static void runOnDisplayThread( final Runnable op )
    {
        runOnDisplayThread( op, PlatformUI.getWorkbench().getDisplay() );
    }
    
    public static String describe( final Widget widget )
    {
        return describe( widget, 0 );
    }
    
    public static String describe( final Widget widget,
                                   final int ancestorLevels )
    {
        Widget start = widget;
        
        if( widget instanceof Control )
        {
            for( int i = 0; i < ancestorLevels; i++ )
            {
                final Control parent = ( (Control) start ).getParent();
                
                if( parent == null )
                {
                    break;
                }
                else
                {
                    start = parent;
                }
            }
        }
        
        final StringBuilder buf = new StringBuilder();
        describe( widget, start, buf, "" );
        return buf.toString();
    }
    
    private static void describe( final Widget target,
                                  final Widget widget,
                                  final StringBuilder buf,
                                  final String indent )
    {
        if( widget instanceof ToolBar )
        {
            buf.append( indent ).append( widget == target ? "**" : "" ).append( "ToolBar" ).append( '\n' );
            buf.append( indent ).append( '{' ).append( '\n' );
            
            final String innerIndent = indent + "    ";
            
            for( ToolItem item : ( (ToolBar) widget ).getItems() )
            {
                describe( target, item, buf, innerIndent );
            }
            
            buf.append( indent ).append( '}' ).append( '\n' );
        }
        else if( widget instanceof ToolItem )
        {
            buf.append( indent ).append( widget == target ? "**" : "" ).append( "ToolItem( " ).append( ( (ToolItem) widget ).getToolTipText() ).append( " )" ).append( '\n' );
        }
        else if( widget instanceof SapphireFormText )
        {
            buf.append( indent ).append( widget == target ? "**" : "" ).append( "SapphireFormText( " ).append( ( (SapphireFormText) widget ).getText() ).append( " )" ).append( '\n' );
        }
        else if( widget instanceof Composite )
        {
            String name = widget.getClass().getSimpleName();
            
            if( name.length() == 0 )
            {
                name = "Composite( Anonymous Subclass )";
            }
            
            buf.append( indent ).append( widget == target ? "**" : "" ).append( name ).append( '\n' );
            buf.append( indent ).append( '{' ).append( '\n' );
            
            final String innerIndent = indent + "    ";
            
            for( Control child : ( (Composite) widget ).getChildren() )
            {
                describe( target, child, buf, innerIndent );
            }
            
            buf.append( indent ).append( '}' ).append( '\n' );
        }
        else if( widget instanceof Label )
        {
            buf.append( indent ).append( widget == target ? "**" : "" ).append( "Label( " ).append( ( (Label) widget ).getText() ).append( " )" ).append( '\n' );
        }
        else if( widget instanceof Text )
        {
            buf.append( indent ).append( widget == target ? "**" : "" ).append( "Text( " ).append( ( (Text) widget ).getText() ).append( " )" ).append( '\n' );
        }
        else
        {
            String name = widget.getClass().getSimpleName();
            
            if( name.length() == 0 )
            {
                name = "Widget( Anonymous Subclass )";
            }
            
            buf.append( indent ).append( widget == target ? "**" : "" ).append( widget ).append( "()" ).append( '\n' );
        }
    }

    public static ImageDescriptor toImageDescriptor( final org.eclipse.sapphire.ImageData data )
    {
        if( data != null )
        {
            final ImageData swtImageData = new ImageData( data.contents() );
            return ImageDescriptor.createFromImageData( swtImageData );
        }
        
        return null;
    }
    
    public static ImageData toImageData( final org.eclipse.sapphire.ImageData data )
    {
        if( data != null )
        {
            return new ImageData( data.contents() );
        }
        
        return null;
    }

    public static ImageData createImageData( final ClassLoader cl,
                                             final String path )
    {
        return toImageData( org.eclipse.sapphire.ImageData.readFromClassLoader( cl, path ).required() );
    }

    public static ImageData createImageData( final Class<?> cl,
                                             final String path )
    {
        return toImageData( org.eclipse.sapphire.ImageData.readFromClassLoader( cl, path ).required() );
    }

    public static ImageDescriptor createImageDescriptor( final ClassLoader cl,
                                                         final String path )
    {
        return toImageDescriptor( org.eclipse.sapphire.ImageData.readFromClassLoader( cl, path ).required() );
    }

    public static ImageDescriptor createImageDescriptor( final Class<?> cl,
                                                         final String path )
    {
        return toImageDescriptor( org.eclipse.sapphire.ImageData.readFromClassLoader( cl, path ).required() );
    }
    
    public static int sizeOfImage( final org.eclipse.sapphire.ImageData image )
    {
        return toImageData( image ).height;
    }

}
