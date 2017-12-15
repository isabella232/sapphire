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

package org.eclipse.sapphire.tests.modeling.misc.t0002;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.util.DependencySorter;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests the DependencySorter class.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestMisc0002

    extends SapphireTestCase
    
{
    private TestMisc0002( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "Misc0002" );

        suite.addTest( new TestMisc0002( "testNoItems" ) );
        suite.addTest( new TestMisc0002( "testOneItem" ) );
        suite.addTest( new TestMisc0002( "testMultipleUnrelatedItems" ) );
        suite.addTest( new TestMisc0002( "testSimpleDependency" ) );
        suite.addTest( new TestMisc0002( "testMultiLevelDependency" ) );
        suite.addTest( new TestMisc0002( "testCycleBreaking1" ) );
        suite.addTest( new TestMisc0002( "testCycleBreaking2" ) );
        suite.addTest( new TestMisc0002( "testCycleBreaking3" ) );
        
        return suite;
    }
    
    public void testNoItems() throws Exception
    {
        final DependencySorter<String,Object> sorter = new DependencySorter<String,Object>();
        final List<Object> sorted = sorter.sort();
        
        assertEquals( 0, sorted.size() );
    }

    public void testOneItem() throws Exception
    {
        final DependencySorter<String,Object> sorter = new DependencySorter<String,Object>();
        
        final Object a = new Object();
        sorter.add( "a", a );
        
        final List<Object> sorted = sorter.sort();
        
        assertEquals( 1, sorted.size() );
        assertTrue( sorted.contains( a ) );
    }

    public void testMultipleUnrelatedItems() throws Exception
    {
        final DependencySorter<String,Object> sorter = new DependencySorter<String,Object>();
        
        final Object a = new Object();
        sorter.add( "a", a );

        final Object b = new Object();
        sorter.add( "b", b );
        
        final Object c = new Object();
        sorter.add( "c", c );
        
        final List<Object> sorted = sorter.sort();
        
        assertEquals( 3, sorted.size() );
        assertTrue( sorted.contains( a ) );
        assertTrue( sorted.contains( b ) );
        assertTrue( sorted.contains( c ) );
    }

    public void testSimpleDependency() throws Exception
    {
        final DependencySorter<String,Object> sorter = new DependencySorter<String,Object>();
        
        final Object a = new Object();
        sorter.add( "a", a );
        sorter.dependency( a, "c" );

        final Object b = new Object();
        sorter.add( "b", b );
        
        final Object c = new Object();
        sorter.add( "c", c );
        
        final List<Object> sorted = sorter.sort();
        
        assertEquals( 3, sorted.size() );
        assertTrue( sorted.contains( a ) );
        assertTrue( sorted.contains( b ) );
        assertTrue( sorted.contains( c ) );
        assertTrue( sorted.indexOf( c ) < sorted.indexOf( a ) );
    }

    public void testMultiLevelDependency() throws Exception
    {
        final DependencySorter<String,Object> sorter = new DependencySorter<String,Object>();
        
        final Object a = new Object();
        sorter.add( "a", a );
        sorter.dependency( a, "b" );

        final Object b = new Object();
        sorter.add( "b", b );
        sorter.dependency( b, "c" );
        
        final Object c = new Object();
        sorter.add( "c", c );
        
        final List<Object> sorted = sorter.sort();
        
        assertEquals( 3, sorted.size() );
        assertTrue( sorted.contains( a ) );
        assertTrue( sorted.contains( b ) );
        assertTrue( sorted.contains( c ) );
        assertTrue( sorted.indexOf( c ) < sorted.indexOf( b ) );
        assertTrue( sorted.indexOf( b ) < sorted.indexOf( a ) );
    }

    public void testCycleBreaking1() throws Exception
    {
        /*
         *    a ---> b ---> c
         *             <---
         */
        
        final DependencySorter<String,Object> sorter = new DependencySorter<String,Object>();
        
        final Object a = new Object();
        sorter.add( "a", a );
        sorter.dependency( a, "b" );

        final Object b = new Object();
        sorter.add( "b", b );
        sorter.dependency( b, "c" );
        
        final Object c = new Object();
        sorter.add( "c", c );
        sorter.dependency( c, "b" );
        
        final List<Object> sorted = sorter.sort();
        
        assertEquals( 3, sorted.size() );
        assertTrue( sorted.contains( a ) );
        assertTrue( sorted.contains( b ) );
        assertTrue( sorted.contains( c ) );
    }

    public void testCycleBreaking2() throws Exception
    {
        /*
         *    a ---> b ---> c
         *      <----------
         */
        
        final DependencySorter<String,Object> sorter = new DependencySorter<String,Object>();
        
        final Object a = new Object();
        sorter.add( "a", a );
        sorter.dependency( a, "b" );

        final Object b = new Object();
        sorter.add( "b", b );
        sorter.dependency( b, "c" );
        
        final Object c = new Object();
        sorter.add( "c", c );
        sorter.dependency( c, "a" );
        
        final List<Object> sorted = sorter.sort();
        
        assertEquals( 3, sorted.size() );
        assertTrue( sorted.contains( a ) );
        assertTrue( sorted.contains( b ) );
        assertTrue( sorted.contains( c ) );
    }

    public void testCycleBreaking3() throws Exception
    {
        /*
         *    a ---> b ---> c
         *      <----------
         *      
         *    d
         */
        
        final DependencySorter<String,Object> sorter = new DependencySorter<String,Object>();
        
        final Object a = new Object();
        sorter.add( "a", a );
        sorter.dependency( a, "b" );

        final Object b = new Object();
        sorter.add( "b", b );
        sorter.dependency( b, "c" );
        
        final Object c = new Object();
        sorter.add( "c", c );
        sorter.dependency( c, "a" );
        
        final Object d = new Object();
        sorter.add( "d", d );
        
        final List<Object> sorted = sorter.sort();
        
        assertEquals( 4, sorted.size() );
        assertTrue( sorted.contains( a ) );
        assertTrue( sorted.contains( b ) );
        assertTrue( sorted.contains( c ) );
        assertTrue( sorted.contains( d ) );
    }

}
