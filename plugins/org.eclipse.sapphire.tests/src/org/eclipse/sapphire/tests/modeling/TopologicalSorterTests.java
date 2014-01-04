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

package org.eclipse.sapphire.tests.modeling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.tests.SapphireTestCase;
import org.eclipse.sapphire.ui.util.TopologicalSorter;
import org.eclipse.sapphire.ui.util.TopologicalSorter.Entity;
import org.junit.Test;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@SuppressWarnings( "unchecked" )
public final class TopologicalSorterTests extends SapphireTestCase
{
    @Test
    
    public void test_1()
    {
        final TopologicalSorter<String> sorter = new TopologicalSorter<String>();
        
        final TopologicalSorter.Entity a = sorter.entity( "A" );
        final TopologicalSorter.Entity b = sorter.entity( "B" );
        final TopologicalSorter.Entity c = sorter.entity( "C" );
        
        b.before( a );
        a.after( c );
        
        testWithCycleBreaking( sorter, list( b, c, a ) );
    }
    
    @Test
    
    public void test_2()
    {
        final TopologicalSorter<String> sorter = new TopologicalSorter<String>();
        
        final TopologicalSorter.Entity a = sorter.entity( "A" );
        final TopologicalSorter.Entity b = sorter.entity( "B" );
        final TopologicalSorter.Entity c = sorter.entity( "C" );
        
        b.before( a );
        a.after( c );
        c.before( b );
        
        testWithCycleBreaking( sorter, list( c, b, a ) );
    }
    
    @Test
    
    public void test_3()
    {
        final TopologicalSorter<String> sorter = new TopologicalSorter<String>();
        
        final TopologicalSorter.Entity b = sorter.entity( "B" );
        final TopologicalSorter.Entity a = sorter.entity( "A" );
        final TopologicalSorter.Entity c = sorter.entity( "C" );
        
        b.before( a );
        a.after( c );
        c.before( b );
        
        testWithCycleBreaking( sorter, list( c, b, a ) );
    }
    
    @Test
    
    public void test_4()
    {
        // One cycle in a graph that has a leaf.
        //
        //       ----- D
        //       |     ^
        //       v     |
        // A --> B --> C
        
        final TopologicalSorter<String> sorter = new TopologicalSorter<String>();
        
        final TopologicalSorter.Entity a = sorter.entity( "A" );
        final TopologicalSorter.Entity b = sorter.entity( "B" );
        final TopologicalSorter.Entity c = sorter.entity( "C" );
        final TopologicalSorter.Entity d = sorter.entity( "D" );
        
        a.after( b );
        b.after( c );
        c.after( d );
        d.after( b );
        
        testWithCycleBreaking( sorter, list( d, c, b, a ), list( list( b, c, d ) ) );
    }
    
    @Test
    
    public void test_5()
    {
        // Two cycles in a graph that has a leaf.
        //
        //       ----- D     ----- G
        //       |     ^     |     ^
        //       v     |     v     |
        // A --> B --> C --> E --> F --> H
        
        final TopologicalSorter<String> sorter = new TopologicalSorter<String>();
        
        final TopologicalSorter.Entity a = sorter.entity( "A" );
        final TopologicalSorter.Entity b = sorter.entity( "B" );
        final TopologicalSorter.Entity c = sorter.entity( "C" );
        final TopologicalSorter.Entity d = sorter.entity( "D" );
        final TopologicalSorter.Entity e = sorter.entity( "E" );
        final TopologicalSorter.Entity f = sorter.entity( "F" );
        final TopologicalSorter.Entity g = sorter.entity( "G" );
        final TopologicalSorter.Entity h = sorter.entity( "H" );
        
        a.after( b );
        b.after( c );
        c.after( d );
        d.after( b );
        c.after( e );
        e.after( f );
        f.after( g );
        g.after( e );
        f.after( h );
        
        testWithCycleBreaking( sorter, list( d, g, h, f, e, c, b, a ), list( list( b, c, d ), list( e, f, g ) ) );
    }
    
    @Test
    
    public void test_6()
    {
        // One cycle in a graph with no leaves.
        //
        // ----- C
        // |     ^
        // v     |
        // A --> B
        
        final TopologicalSorter<String> sorter = new TopologicalSorter<String>();
        
        final TopologicalSorter.Entity a = sorter.entity( "A" );
        final TopologicalSorter.Entity b = sorter.entity( "B" );
        final TopologicalSorter.Entity c = sorter.entity( "C" );
        
        a.after( b );
        b.after( c );
        c.after( a );
        
        testWithCycleBreaking( sorter, list( c, b, a ), list( list( a, b, c ) ) );
    }
    
    @Test
    
    public void test_7()
    {
        // Cycle with another cycle in a graph that has a leaf.
        //
        //             G --> F
        //             ^     |
        //             |     v
        //       I <-- H <-- E
        //       |           ^
        //       v           |
        // A --> B --> C --> D
        
        final TopologicalSorter<String> sorter = new TopologicalSorter<String>();
        
        final TopologicalSorter.Entity a = sorter.entity( "A" );
        final TopologicalSorter.Entity b = sorter.entity( "B" );
        final TopologicalSorter.Entity c = sorter.entity( "C" );
        final TopologicalSorter.Entity d = sorter.entity( "D" );
        final TopologicalSorter.Entity e = sorter.entity( "E" );
        final TopologicalSorter.Entity f = sorter.entity( "F" );
        final TopologicalSorter.Entity g = sorter.entity( "G" );
        final TopologicalSorter.Entity h = sorter.entity( "H" );
        final TopologicalSorter.Entity i = sorter.entity( "I" );

        a.after( b );
        b.after( c );
        c.after( d );
        d.after( e );
        e.after( h );
        h.after( i );
        i.after( b );
        h.after( g );
        g.after( f );
        f.after( e );
        
        testWithCycleBreaking( sorter, list( i, f, g, h, e, d, c, b, a ), list( list( b, c, d, e, h, i ), list( e, h, g, f ) ) );
    }
    
    @Test
    
    public void test_8()
    {
        // Cycle with another cycle in a graph that has no leaves.
        //
        //       G --> F
        //       ^     |
        //       |     v
        // I <-- H <-- E
        // |           ^
        // v           |
        // B --> C --> D
        
        final TopologicalSorter<String> sorter = new TopologicalSorter<String>();
        
        final TopologicalSorter.Entity b = sorter.entity( "B" );
        final TopologicalSorter.Entity c = sorter.entity( "C" );
        final TopologicalSorter.Entity d = sorter.entity( "D" );
        final TopologicalSorter.Entity e = sorter.entity( "E" );
        final TopologicalSorter.Entity f = sorter.entity( "F" );
        final TopologicalSorter.Entity g = sorter.entity( "G" );
        final TopologicalSorter.Entity h = sorter.entity( "H" );
        final TopologicalSorter.Entity i = sorter.entity( "I" );

        b.after( c );
        c.after( d );
        d.after( e );
        e.after( h );
        h.after( i );
        i.after( b );
        h.after( g );
        g.after( f );
        f.after( e );
        
        testWithCycleBreaking( sorter, list( i, f, g, h, e, d, c, b ), list( list( b, c, d, e, h, i ), list( e, h, g, f ) ) );
    }
    
    @Test
    
    public void test_9()
    {
        // Graph with a self cycle.
        //
        //       -------
        //       |     |
        //       v     |
        // A --> B -----
        
        final TopologicalSorter<String> sorter = new TopologicalSorter<String>();
        
        final TopologicalSorter.Entity a = sorter.entity( "A" );
        final TopologicalSorter.Entity b = sorter.entity( "B" );

        a.after( b );
        b.after( b );
        
        testWithCycleBreaking( sorter, list( b, a ), list( list( b ) ) );
    }
    
    @Test
    
    public void test_10()
    {
        // Two detached sub-graphs. One of a sub-graphs has a cycle.
        //
        //       ----- D
        //       |     ^
        //       v     |
        // A --> B --> C
        //
        // E --> F --> G
        //       |
        //       v
        //       H
        
        final TopologicalSorter<String> sorter = new TopologicalSorter<String>();
        
        final TopologicalSorter.Entity a = sorter.entity( "A" );
        final TopologicalSorter.Entity b = sorter.entity( "B" );
        final TopologicalSorter.Entity c = sorter.entity( "C" );
        final TopologicalSorter.Entity d = sorter.entity( "D" );
        final TopologicalSorter.Entity e = sorter.entity( "E" );
        final TopologicalSorter.Entity f = sorter.entity( "F" );
        final TopologicalSorter.Entity g = sorter.entity( "G" );
        final TopologicalSorter.Entity h = sorter.entity( "H" );
        
        a.after( b );
        b.after( c );
        c.after( d );
        d.after( b );
        e.after( f );
        f.after( g );
        f.after( h );
        
        testWithCycleBreaking( sorter, list( d, c, b, a, g, h, f, e ), list( list( b, c, d ) ) );
    }
    
    private static void testWithCycleBreaking( final TopologicalSorter<String> sorter,
                                               final List<TopologicalSorter.Entity> expectedOrder )
    {
        testWithCycleBreaking( sorter, expectedOrder, Collections.<List<TopologicalSorter.Entity>>emptyList() );
    }

    private static void testWithCycleBreaking( final TopologicalSorter<String> sorter,
                                               final List<TopologicalSorter.Entity> expectedOrder,
                                               final List<List<TopologicalSorter.Entity>> expectedCycles )
    {
        final List<List<TopologicalSorter.Entity>> actualCycles = new ArrayList<List<TopologicalSorter.Entity>>();
        
        final TopologicalSorter.CycleListener listener = new TopologicalSorter.CycleListener()
        {
            @Override
            public void handleCycle( final List<Entity> cycle )
            {
                actualCycles.add( cycle );
            }
        };
        
        sorter.addCycleListener( listener );
        
        final List<String> actualOrder = sorter.sort();
        final List<String> expectedOrderAsStrings = new ArrayList<String>();
        
        for( TopologicalSorter.Entity entity : expectedOrder )
        {
            expectedOrderAsStrings.add( (String) entity.data() );
        }
        
        assertEquals( expectedOrderAsStrings, actualOrder );
        assertEquals( expectedCycles, actualCycles );
    }
    
}
