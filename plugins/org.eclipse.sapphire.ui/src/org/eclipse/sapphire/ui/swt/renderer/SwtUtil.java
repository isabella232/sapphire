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

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.sapphire.ui.renderers.swt.ColumnSortComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
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
    
}
