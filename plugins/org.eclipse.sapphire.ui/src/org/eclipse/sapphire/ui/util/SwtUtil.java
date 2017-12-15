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

package org.eclipse.sapphire.ui.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.sapphire.ui.renderers.swt.ColumnSortComparator;
import org.eclipse.sapphire.ui.util.internal.MutableReference;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SwtUtil
{
    public static final GridLayout glayout( final int columns )
    {
        return new GridLayout( columns, false );
    }

    public static final GridLayout glayout( final int columns,
                                            final int marginWidth,
                                            final int marginHeight )
    {
        final GridLayout layout = new GridLayout( columns, false );

        layout.marginWidth = marginWidth;
        layout.marginHeight = marginHeight;
        
        return layout;
    }

    public static final GridLayout glayout( final int columns,
                                            final int leftMargin,
                                            final int rightMargin,
                                            final int topMargin,
                                            final int bottomMargin )
    {
        final GridLayout layout = new GridLayout( columns, false );
        
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.marginLeft = leftMargin;
        layout.marginRight = rightMargin;
        layout.marginTop = topMargin;
        layout.marginBottom = bottomMargin;
        
        return layout;
    }
    
    public static final GridLayout glspacing( final GridLayout layout,
                                              final int spacing )
    {
        layout.horizontalSpacing = spacing;
        layout.verticalSpacing = spacing;
        
        return layout;
    }
    
    public static final GridData gd()
    {
        return new GridData();
    }
    
    public static final GridData gdfill()
    {
        return new GridData( SWT.FILL, SWT.FILL, true, true );
    }
    
    public static final GridData gdhfill()
    {
        return new GridData( GridData.FILL_HORIZONTAL );
    }

    public static final GridData gdvfill()
    {
        return new GridData( GridData.FILL_VERTICAL );
    }
    
    public static final GridData gdhhint( final GridData gd,
                                          final int heightHint )
    {
        gd.heightHint = heightHint;
        return gd;
    }
    
    public static final GridData gdwhint( final GridData gd,
                                          final int widthHint )
    {
        gd.widthHint = widthHint;
        return gd;
    }
    
    public static final GridData gdhindent( final GridData gd,
                                            final int horizontalIndent )
    {
        gd.horizontalIndent = horizontalIndent;
        return gd;
    }

    public static final GridData gdvindent( final GridData gd,
                                            final int verticalIndent )
    {
        gd.verticalIndent = verticalIndent;
        return gd;
    }
    
    public static final GridData hspan( final GridData gd,
                                        final int span )
    {
        gd.horizontalSpan = span;
        return gd;
    }

    public static final GridData vspan( final GridData gd,
                                        final int span )
    {
        gd.verticalSpan = span;
        return gd;
    }
    
    public static final GridData halign( final GridData gd,
                                         final int alignment )
    {
        gd.horizontalAlignment = alignment;
        return gd;
    }
    
    public static final GridData valign( final GridData gd,
                                         final int alignment )
    {
        gd.verticalAlignment = alignment;
        return gd;
    }
    
    public static Color color( final int id )
    {
        return Display.getCurrent().getSystemColor( id );       
    }
    
    public static final int getPreferredWidth( final Control control )
    {
        return control.computeSize( SWT.DEFAULT, SWT.DEFAULT ).x;
    }
    
    /**
     * Gets the top-level workbench active shell no matter which thread it
     * is called from.
     *  
     * @return the shell
     */
    
    public static Shell getActiveShell()
    {
        if( PlatformUI.isWorkbenchRunning() )
        {
            final IWorkbench workbench = PlatformUI.getWorkbench();
            final Display display = workbench.getDisplay();
            
            final Shell[] result = new Shell[ 1 ];
            
            display.syncExec
            (
                new Runnable()
                {
                    public void run() 
                    {
                        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                        if ( window != null ) {
                           result[ 0 ] = window.getShell();
                        } else {
                           result[ 0 ] = null;
                        }
                    }
                }
            );
            
            return result[ 0 ];
        }
        else
        {
            return null;
        }
    }
    
    public static boolean hasModalShell( final Display display )
    {
        if( display.isDisposed() )
        {
            return false;
        }
        else
        {
            final MutableReference<Boolean> result = new MutableReference<Boolean>();
            
            display.syncExec
            (
                new Runnable()
                {
                    public void run()
                    {
                        result.set( Boolean.FALSE );
                        
                        for( Shell shell : display.getShells() )
                        {
                            if( ! shell.isDisposed() && shell.isVisible() &&
                                ( shell.getStyle() & SWT.APPLICATION_MODAL ) != 0 )
                            {
                                result.set( Boolean.TRUE );
                                break;
                            }
                        }
                    }
                }
            );
            
            return result.get().booleanValue();
        }
    }
    
    public static void waitOnModalShells()
    {
        final Display display = PlatformUI.getWorkbench().getDisplay();
        
        while( hasModalShell( display ) )
        {
            try
            {
                Thread.sleep( 1000 );
            }
            catch( InterruptedException e ) {}
        }
    }
    
    public static void setEnabledOnChildren( final Composite composite,
                                             final boolean enabled )
    {
        for( Control child : composite.getChildren() )
        {
            child.setEnabled( enabled );
        }
    }
    
    public static void runOnDisplayThread( final Display display,
                                           final Runnable runnable )
    {
        if( display.getThread() == Thread.currentThread() )
        {
            runnable.run();
        }
        else
        {
            display.asyncExec( runnable );
        }
    }
    
    public static TreeItem getTreeItem( final Tree tree,
                                        final int x,
                                        final int y )
    {
        return getTreeItem( tree.getItems(), x, y );
    }
    
    private static TreeItem getTreeItem( final TreeItem[] items,
                                         final int x,
                                         final int y )
    {
        for( TreeItem item : items )
        {
            final Rectangle bounds = item.getBounds();
            bounds.add( new Rectangle( bounds.x - 25, bounds.y, 25, bounds.height ) );
            
            if( bounds.contains( x, y ) )
            {
                return item;
            }
            
            final TreeItem res = getTreeItem( item.getItems(), x, y );
            
            if( res != null )
            {
                return res;
            }
        }
        
        return null;
    }
    
    public static void makeTableSortable( final TableViewer tableViewer )
    {
        final Map<TableColumn,Comparator<Object>> comparators = Collections.emptyMap();
        makeTableSortable( tableViewer, comparators, tableViewer.getTable().getColumn( 0 ), SWT.DOWN );
    }
    
    public static void makeTableSortable( final TableViewer tableViewer,
                                          final Map<TableColumn,Comparator<Object>> comparators )
    {
        makeTableSortable( tableViewer, comparators, tableViewer.getTable().getColumn( 0 ), SWT.DOWN );
    }
    
    public static void makeTableSortable( final TableViewer tableViewer,
                                          final Map<TableColumn,Comparator<Object>> comparators,
                                          final TableColumn initialSortColumn,
                                          final int initialSortDirection )
    {
        final Table table = tableViewer.getTable();
        
        sortByTableColumn( tableViewer, initialSortColumn, initialSortDirection, comparators.get( initialSortColumn ) );
        
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
    
}
