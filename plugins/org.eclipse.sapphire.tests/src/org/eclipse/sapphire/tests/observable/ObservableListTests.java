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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.sapphire.ObservableList;
import org.eclipse.sapphire.tests.EventLog;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.eclipse.sapphire.util.ListFactory;
import org.junit.Test;

/**
 * Tests ObservableList.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ObservableListTests extends SapphireTestCase
{
    @Test
    
    public void Size() throws Exception
    {
        final List<String> base = new ArrayList<String>();
        final ObservableList<String> observable = new ObservableList<String>( base );
        
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
        final List<String> base = new ArrayList<String>();
        final ObservableList<String> observable = new ObservableList<String>( base );
        
        assertTrue( observable.isEmpty() );
        
        base.add( "a" );
        
        assertFalse( observable.isEmpty() );
        
        base.clear();
        
        assertTrue( observable.isEmpty() );
    }

    @Test
    
    public void Contains() throws Exception
    {
        final List<String> base = new ArrayList<String>();
        final ObservableList<String> observable = new ObservableList<String>( base );
        
        base.add( "a" );
        
        assertTrue( observable.contains( "a" ) );
        assertFalse( observable.contains( "b" ) );
        assertFalse( observable.contains( new Object() ) );
    }

    @Test
    
    public void IndexOf() throws Exception
    {
        final List<String> base = new ArrayList<String>();
        final ObservableList<String> observable = new ObservableList<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        base.add( "a" );
        base.add( "c" );
        
        assertEquals( 0, observable.indexOf( "a" ) );
        assertEquals( -1, observable.indexOf( "d" ) );
    }

    @Test
    
    public void LastIndexOf() throws Exception
    {
        final List<String> base = new ArrayList<String>();
        final ObservableList<String> observable = new ObservableList<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        base.add( "a" );
        base.add( "c" );
        
        assertEquals( 2, observable.lastIndexOf( "a" ) );
        assertEquals( -1, observable.indexOf( "d" ) );
    }

    @Test
    
    public void ContainsAll() throws Exception
    {
        final List<String> base = new ArrayList<String>();
        final ObservableList<String> observable = new ObservableList<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        base.add( "c" );
        
        assertTrue( observable.containsAll( ListFactory.unmodifiable( "a", "b" ) ) );
        assertFalse( observable.containsAll( ListFactory.unmodifiable( "a", "d" ) ) );
    }

    @Test
    
    public void Iterator() throws Exception
    {
        final List<String> base = new ArrayList<String>();
        final ObservableList<String> observable = new ObservableList<String>( base );
        
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
        
        assertEquals( ListFactory.unmodifiable( "a", "c" ), base );
        assertEquals( 1, log.size() );
        
        assertTrue( itr.hasNext() );
        assertEquals( "c", itr.next() );
        
        assertFalse( itr.hasNext() );
    }

    @Test
    
    public void ToArray1() throws Exception
    {
        final List<String> base = new ArrayList<String>();
        final ObservableList<String> observable = new ObservableList<String>( base );
        
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
        final List<String> base = new ArrayList<String>();
        final ObservableList<String> observable = new ObservableList<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        
        final String[] array = new String[ 2 ];
        final String[] result = observable.toArray( array );
        
        assertSame( array, result );
        assertEquals( "a", array[ 0 ] );
        assertEquals( "b", array[ 1 ] );
    }

    @Test
    
    public void Get() throws Exception
    {
        final List<String> base = new ArrayList<String>();
        final ObservableList<String> observable = new ObservableList<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        
        assertEquals( "a", observable.get( 0 ) );
        assertEquals( "b", observable.get( 1 ) );
    }

    @Test
    
    public void Set() throws Exception
    {
        final List<String> base = new ArrayList<String>();
        final ObservableList<String> observable = new ObservableList<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        
        final EventLog log = new EventLog();
        observable.attach( log );
        
        observable.set( 1, "c" );

        assertEquals( ListFactory.unmodifiable( "a", "c" ), base );
        assertEquals( 1, log.size() );

        log.clear();
        observable.set( 1, "c" );
        
        assertEquals( ListFactory.unmodifiable( "a", "c" ), base );
        assertEquals( 0, log.size() );
    }

    @Test
    
    public void Add1() throws Exception
    {
        final List<String> base = new ArrayList<String>();
        final ObservableList<String> observable = new ObservableList<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        
        final EventLog log = new EventLog();
        observable.attach( log );
        
        observable.add( "c" );

        assertEquals( ListFactory.unmodifiable( "a", "b", "c" ), base );
        assertEquals( 1, log.size() );
    }

    @Test
    
    public void Add2() throws Exception
    {
        final List<String> base = new ArrayList<String>();
        final ObservableList<String> observable = new ObservableList<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        
        final EventLog log = new EventLog();
        observable.attach( log );
        
        observable.add( 1, "c" );

        assertEquals( ListFactory.unmodifiable( "a", "c", "b" ), base );
        assertEquals( 1, log.size() );
    }

    @Test
    
    public void AddAll1() throws Exception
    {
        final List<String> base = new ArrayList<String>();
        final ObservableList<String> observable = new ObservableList<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        
        final EventLog log = new EventLog();
        observable.attach( log );
        
        observable.addAll( ListFactory.unmodifiable( "a", "c", "d" ) );

        assertEquals( ListFactory.unmodifiable( "a", "b", "a", "c", "d" ), base );
        assertEquals( 1, log.size() );
    }

    @Test
    
    public void AddAll2() throws Exception
    {
        final List<String> base = new ArrayList<String>();
        final ObservableList<String> observable = new ObservableList<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        
        final EventLog log = new EventLog();
        observable.attach( log );
        
        observable.addAll( 1, ListFactory.unmodifiable( "a", "c", "d" ) );

        assertEquals( ListFactory.unmodifiable( "a", "a", "c", "d", "b" ), base );
        assertEquals( 1, log.size() );
    }

    @Test
    
    public void Remove1() throws Exception
    {
        final List<String> base = new ArrayList<String>();
        final ObservableList<String> observable = new ObservableList<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        base.add( "c" );
        
        final EventLog log = new EventLog();
        observable.attach( log );
        
        observable.remove( "b" );

        assertEquals( ListFactory.unmodifiable( "a", "c" ), base );
        assertEquals( 1, log.size() );

        log.clear();
        observable.remove( "d" );
        
        assertEquals( ListFactory.unmodifiable( "a", "c" ), base );
        assertEquals( 0, log.size() );
    }

    @Test
    
    public void Remove2() throws Exception
    {
        final List<String> base = new ArrayList<String>();
        final ObservableList<String> observable = new ObservableList<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        base.add( "c" );
        
        final EventLog log = new EventLog();
        observable.attach( log );
        
        observable.remove( 1 );

        assertEquals( ListFactory.unmodifiable( "a", "c" ), base );
        assertEquals( 1, log.size() );
    }

    @Test
    
    public void RemoveAll() throws Exception
    {
        final List<String> base = new ArrayList<String>();
        final ObservableList<String> observable = new ObservableList<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        base.add( "c" );
        
        final EventLog log = new EventLog();
        observable.attach( log );
        
        observable.removeAll( ListFactory.unmodifiable( "b", "c", "d" ) );

        assertEquals( ListFactory.unmodifiable( "a" ), base );
        assertEquals( 1, log.size() );

        log.clear();
        observable.removeAll( ListFactory.unmodifiable( "d", "e" ) );
        
        assertEquals( ListFactory.unmodifiable( "a" ), base );
        assertEquals( 0, log.size() );
    }

    @Test
    
    public void RetainAll() throws Exception
    {
        final List<String> base = new ArrayList<String>();
        final ObservableList<String> observable = new ObservableList<String>( base );
        
        base.add( "a" );
        base.add( "b" );
        base.add( "c" );
        
        final EventLog log = new EventLog();
        observable.attach( log );
        
        observable.retainAll( ListFactory.unmodifiable( "a", "c" ) );

        assertEquals( ListFactory.unmodifiable( "a", "c" ), base );
        assertEquals( 1, log.size() );

        log.clear();
        observable.retainAll( ListFactory.unmodifiable( "a", "c" ) );
        
        assertEquals( ListFactory.unmodifiable( "a", "c" ), base );
        assertEquals( 0, log.size() );
    }

    @Test
    
    public void Clear() throws Exception
    {
        final List<String> base = new ArrayList<String>();
        final ObservableList<String> observable = new ObservableList<String>( base );
        
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
