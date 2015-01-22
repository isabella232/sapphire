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

package org.eclipse.sapphire.tests.observable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.sapphire.ObservableMap;
import org.eclipse.sapphire.tests.EventLog;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.eclipse.sapphire.util.MapFactory;
import org.junit.Test;

/**
 * Tests ObservableMap.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ObservableMapTests extends SapphireTestCase
{
    @Test
    
    public void Size() throws Exception
    {
        final Map<String,String> base = new HashMap<String,String>();
        final ObservableMap<String,String> observable = new ObservableMap<String,String>( base );
        
        assertEquals( 0, observable.size() );
        
        base.put( "a", "1" );
        base.put( "b", "2" );
        
        assertEquals( 2, observable.size() );
        
        base.clear();
        
        assertEquals( 0, observable.size() );
    }

    @Test
    
    public void Empty() throws Exception
    {
        final Map<String,String> base = new HashMap<String,String>();
        final ObservableMap<String,String> observable = new ObservableMap<String,String>( base );
        
        assertTrue( observable.isEmpty() );
        
        base.put( "a", "1" );
        
        assertFalse( observable.isEmpty() );
        
        base.clear();
        
        assertTrue( observable.isEmpty() );
    }

    @Test
    
    public void ContainsKey() throws Exception
    {
        final Map<String,String> base = new HashMap<String,String>();
        final ObservableMap<String,String> observable = new ObservableMap<String,String>( base );
        
        base.put( "a", "1" );
        
        assertTrue( observable.containsKey( "a" ) );
        assertFalse( observable.containsKey( "b" ) );
        assertFalse( observable.containsKey( "1" ) );
        assertFalse( observable.containsKey( new Object() ) );
    }

    @Test
    
    public void ContainsValue() throws Exception
    {
        final Map<String,String> base = new HashMap<String,String>();
        final ObservableMap<String,String> observable = new ObservableMap<String,String>( base );
        
        base.put( "a", "1" );
        
        assertTrue( observable.containsValue( "1" ) );
        assertFalse( observable.containsValue( "2" ) );
        assertFalse( observable.containsValue( "a" ) );
        assertFalse( observable.containsValue( new Object() ) );
    }

    @Test
    
    public void EntrySet() throws Exception
    {
        final Map<String,String> base = new LinkedHashMap<String,String>();
        final ObservableMap<String,String> observable = new ObservableMap<String,String>( base );
        
        base.put( "a", "1" );
        base.put( "b", "2" );
        base.put( "c", "3" );
        
        final EventLog log = new EventLog();
        observable.attach( log );
        
        final Iterator<Map.Entry<String,String>> itr = observable.entrySet().iterator();
        
        assertTrue( itr.hasNext() );
        
        final Map.Entry<String,String> a = itr.next();
        
        assertEquals( "a", a.getKey() );
        assertEquals( "1", a.getValue() );
        
        assertTrue( itr.hasNext() );
        
        final Map.Entry<String,String> b = itr.next();
        
        assertEquals( "b", b.getKey() );
        assertEquals( "2", b.getValue() );
        
        itr.remove();
        
        assertEquals( MapFactory.start().add( "a", "1" ).add( "c", "3" ).result(), base );
        assertEquals( 1, log.size() );
        
        assertTrue( itr.hasNext() );
        
        final Map.Entry<String,String> c = itr.next();
        
        assertEquals( "c", c.getKey() );
        assertEquals( "3", c.getValue() );
        
        assertFalse( itr.hasNext() );
    }

    @Test
    
    public void Get() throws Exception
    {
        final Map<String,String> base = new HashMap<String,String>();
        final ObservableMap<String,String> observable = new ObservableMap<String,String>( base );
        
        base.put( "a", "1" );
        base.put( "b", "2" );
        
        assertEquals( "1", observable.get( "a" ) );
        assertEquals( "2", observable.get( "b" ) );
        assertNull( observable.get( "c" ) );
    }

    @Test
    
    public void Put() throws Exception
    {
        final Map<String,String> base = new HashMap<String,String>();
        final ObservableMap<String,String> observable = new ObservableMap<String,String>( base );
        
        base.put( "a", "1" );
        base.put( "b", "2" );
        
        final EventLog log = new EventLog();
        observable.attach( log );
        
        observable.put( "c", "3" );

        assertEquals( MapFactory.start().add( "a", "1" ).add( "b", "2" ).add( "c", "3" ).result(), base );
        assertEquals( 1, log.size() );
        
        log.clear();
        observable.put( "b", "22" );

        assertEquals( MapFactory.start().add( "a", "1" ).add( "b", "22" ).add( "c", "3" ).result(), base );
        assertEquals( 1, log.size() );
        
        log.clear();
        observable.put( "c", "3" );

        assertEquals( MapFactory.start().add( "a", "1" ).add( "b", "22" ).add( "c", "3" ).result(), base );
        assertEquals( 0, log.size() );
    }

    @Test
    
    public void PutAll() throws Exception
    {
        final Map<String,String> base = new HashMap<String,String>();
        final ObservableMap<String,String> observable = new ObservableMap<String,String>( base );
        
        base.put( "a", "1" );
        base.put( "b", "2" );
        
        final EventLog log = new EventLog();
        observable.attach( log );
        
        observable.putAll( MapFactory.<String,String>start().add( "c", "3" ).add( "d", "4" ).result() );

        assertEquals( MapFactory.start().add( "a", "1" ).add( "b", "2" ).add( "c", "3" ).add( "d", "4" ).result(), base );
        assertEquals( 1, log.size() );

        log.clear();
        observable.putAll( MapFactory.<String,String>start().add( "a", "11" ).add( "b", "22" ).result() );

        assertEquals( MapFactory.start().add( "a", "11" ).add( "b", "22" ).add( "c", "3" ).add( "d", "4" ).result(), base );
        assertEquals( 1, log.size() );
        
        log.clear();
        observable.putAll( MapFactory.<String,String>start().add( "c", "3" ).add( "d", "4" ).result() );

        assertEquals( MapFactory.start().add( "a", "11" ).add( "b", "22" ).add( "c", "3" ).add( "d", "4" ).result(), base );
        assertEquals( 0, log.size() );
    }

    @Test
    
    public void Remove() throws Exception
    {
        final Map<String,String> base = new HashMap<String,String>();
        final ObservableMap<String,String> observable = new ObservableMap<String,String>( base );
        
        base.put( "a", "1" );
        base.put( "b", "2" );
        base.put( "c", "3" );
        
        final EventLog log = new EventLog();
        observable.attach( log );
        
        observable.remove( "b" );

        assertEquals( MapFactory.start().add( "a", "1" ).add( "c", "3" ).result(), base );
        assertEquals( 1, log.size() );

        log.clear();
        observable.remove( "d" );
        
        assertEquals( MapFactory.start().add( "a", "1" ).add( "c", "3" ).result(), base );
        assertEquals( 0, log.size() );
    }

    @Test
    
    public void Clear() throws Exception
    {
        final Map<String,String> base = new HashMap<String,String>();
        final ObservableMap<String,String> observable = new ObservableMap<String,String>( base );
        
        base.put( "a", "1" );
        base.put( "b", "2" );
        base.put( "c", "3" );
        
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
