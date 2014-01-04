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

package org.eclipse.sapphire.util;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class IdentityCache<K,V>
{
    private Map<K,V> map = new IdentityHashMap<K,V>();
    private Map<K,V> next = null;
    
    public V get( final K key )
    {
        final V value = this.map.get( key );
        
        if( this.next != null && value != null )
        {
            this.next.put( key, value );
        }
        
        return value;
    }
    
    public void put( final K key,
                     final V value )
    {
        if( value == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( this.next != null )
        {
            this.next.put( key, value );
        }
        
        this.map.put( key, value );
    }
    
    public void track()
    {
        this.next = new IdentityHashMap<K,V>();
    }
    
    public void purge()
    {
        this.map = this.next;
        this.next = null;
    }
    
}
