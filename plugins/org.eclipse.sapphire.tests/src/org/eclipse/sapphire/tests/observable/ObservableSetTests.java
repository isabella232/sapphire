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

package org.eclipse.sapphire.tests.observable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.sapphire.ObservableSet;
import org.eclipse.sapphire.tests.EventLog;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.eclipse.sapphire.util.SetFactory;
import org.junit.Test;

/**
 * Tests ObservableSet.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ObservableSetTests extends SapphireTestCase
{
    @Test
    
    public void Size() throws Exception
    {
        final Set<String> base = new HashSet<String>();
        final ObservableSet<String> observable = new ObservableSet<String>( base );
        
        assertEquals( 0, observable.size() );
        
        base.add( "a" );
        base.add( "b" );
        
        assertEquals( 2, observable.size() );
        
        base.clear();
        
        assertEquals( 0, observable.size() );
    }

    @Test
    
    public void Empty() throws Exception
    {
        final Set<String> base = new HashSet<String>();
        final ObservableSet<String> observable = new ObservableSet<String>( base );
        
        assertTrue( observable.isEmpty() );
        
        base.add( "a" );
        
        assertFalse( observable.isEmpty() );
        
        base.clear();
        
        assertTrue( observable.isEmpty() );
    }

    @Test
    
    public void Contains() throws Exception
    {
        final Set<String> base = new HashSet<String>();
        final ObservableSet<String> observable = new ObservableSet<String>( base );
        
        base.add( "a" );
        
        assertTrue( observable.contains( "a" ) );
        assertFalse( observable.contains( "b" ) );
        assertFalse( observable.contains( new Object() ) );
    }

    @Test
    
    public void ContainsAll() throws Exception
    {
        final Set<String> base = new HashSet<String>();
        final ObservableSet<String> observable = new ObservableSet<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        base.add( "c" );
        
        assertTrue( observable.containsAll( SetFactory.unmodifiable( "a", "b" ) ) );
        assertFalse( observable.containsAll( SetFactory.unmodifiable( "a", "d" ) ) );
    }

    @Test
    
    public void Iterator() throws Exception
    {
        final Set<String> base = new LinkedHashSet<String>();
        final ObservableSet<String> observable = new ObservableSet<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        base.add( "c" );
        
        final EventLog log = new EventLog();
        observable.attach( log );
        
        final Iterator<String> itr = observable.iterator();
        
        assertTrue( itr.hasNext() );
        assertEquals( "a", itr.next() );
        
        assertTrue( itr.hasNext() );
        assertEquals( "b", itr.next() );
        
        itr.remove();
        
        assertEquals( SetFactory.unmodifiable( "a", "c" ), base );
        assertEquals( 1, log.size() );
        
        assertTrue( itr.hasNext() );
        assertEquals( "c", itr.next() );
        
        assertFalse( itr.hasNext() );
    }

    @Test
    
    public void ToArray1() throws Exception
    {
        final Set<String> base = new LinkedHashSet<String>();
        final ObservableSet<String> observable = new ObservableSet<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        
        final Object[] array = observable.toArray();
        
        assertEquals( 2, array.length );
        assertEquals( "a", array[ 0 ] );
        assertEquals( "b", array[ 1 ] );
    }


    @Test
    
    public void ToArray2() throws Exception
    {
        final Set<String> base = new LinkedHashSet<String>();
        final ObservableSet<String> observable = new ObservableSet<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        
        final String[] array = new String[ 2 ];
        final String[] result = observable.toArray( array );
        
        assertSame( array, result );
        assertEquals( "a", array[ 0 ] );
        assertEquals( "b", array[ 1 ] );
    }

    @Test
    
    public void Add() throws Exception
    {
        final Set<String> base = new LinkedHashSet<String>();
        final ObservableSet<String> observable = new ObservableSet<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        
        final EventLog log = new EventLog();
        observable.attach( log );
        
        observable.add( "c" );

        assertEquals( SetFactory.unmodifiable( "a", "b", "c" ), base );
        assertEquals( 1, log.size() );

        log.clear();
        observable.add( "a" );
        
        assertEquals( SetFactory.unmodifiable( "a", "b", "c" ), base );
        assertEquals( 0, log.size() );
    }

    @Test
    
    public void AddAll() throws Exception
    {
        final Set<String> base = new LinkedHashSet<String>();
        final ObservableSet<String> observable = new ObservableSet<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        
        final EventLog log = new EventLog();
        observable.attach( log );
        
        observable.addAll( SetFactory.unmodifiable( "a", "c", "d" ) );

        assertEquals( SetFactory.unmodifiable( "a", "b", "c", "d" ), base );
        assertEquals( 1, log.size() );

        log.clear();
        observable.addAll( SetFactory.unmodifiable( "a", "b" ) );
        
        assertEquals( SetFactory.unmodifiable( "a", "b", "c", "d" ), base );
        assertEquals( 0, log.size() );
    }

    @Test
    
    public void Remove() throws Exception
    {
        final Set<String> base = new LinkedHashSet<String>();
        final ObservableSet<String> observable = new ObservableSet<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        base.add( "c" );
        
        final EventLog log = new EventLog();
        observable.attach( log );
        
        observable.remove( "b" );

        assertEquals( SetFactory.unmodifiable( "a", "c" ), base );
        assertEquals( 1, log.size() );

        log.clear();
        observable.remove( "d" );
        
        assertEquals( SetFactory.unmodifiable( "a", "c" ), base );
        assertEquals( 0, log.size() );
    }

    @Test
    
    public void RemoveAll() throws Exception
    {
        final Set<String> base = new LinkedHashSet<String>();
        final ObservableSet<String> observable = new ObservableSet<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        base.add( "c" );
        
        final EventLog log = new EventLog();
        observable.attach( log );
        
        observable.removeAll( SetFactory.unmodifiable( "b", "c", "d" ) );

        assertEquals( SetFactory.unmodifiable( "a" ), base );
        assertEquals( 1, log.size() );

        log.clear();
        observable.removeAll( SetFactory.unmodifiable( "d", "e" ) );
        
        assertEquals( SetFactory.unmodifiable( "a" ), base );
        assertEquals( 0, log.size() );
    }

    @Test
    
    public void RetainAll() throws Exception
    {
        final Set<String> base = new LinkedHashSet<String>();
        final ObservableSet<String> observable = new ObservableSet<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        base.add( "c" );
        
        final EventLog log = new EventLog();
        observable.attach( log );
        
        observable.retainAll( SetFactory.unmodifiable( "a", "c" ) );

        assertEquals( SetFactory.unmodifiable( "a", "c" ), base );
        assertEquals( 1, log.size() );

        log.clear();
        observable.retainAll( SetFactory.unmodifiable( "a", "c" ) );
        
        assertEquals( SetFactory.unmodifiable( "a", "c" ), base );
        assertEquals( 0, log.size() );
    }

    @Test
    
    public void Clear() throws Exception
    {
        final Set<String> base = new LinkedHashSet<String>();
        final ObservableSet<String> observable = new ObservableSet<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        base.add( "c" );
        
        final EventLog log = new EventLog();
        observable.attach( log );
        
        observable.clear();

        assertTrue( base.isEmpty() );
        assertEquals( 1, log.size() );

        log.clear();
        observable.clear();
        
        assertTrue( base.isEmpty() );
        assertEquals( 0, log.size() );
    }

}
