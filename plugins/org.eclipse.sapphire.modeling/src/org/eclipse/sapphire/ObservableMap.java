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

package org.eclipse.sapphire;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of {@link Map} that is {@link Observable} using listeners.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ObservableMap<K,V> extends AbstractMap<K,V> implements Observable
{
    private final Map<K,V> base;
    private Set<Map.Entry<K,V>> entries;
    private ListenerContext listeners;
    
    public ObservableMap()
    {
        this( null );
    }
    
    public ObservableMap( final Map<K,V> base )
    {
        this.base = ( base == null ? new HashMap<K,V>() : base );
    }

    @Override
    public void attach( final Listener listener )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( this )
        {
            if( this.listeners == null )
            {
                this.listeners = new ListenerContext();
            }
            
            this.listeners.attach( listener );
        }
    }

    @Override
    public void detach( final Listener listener )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( this )
        {
            if( this.listeners != null )
            {
                this.listeners.detach( listener );
            }
        }
    }
    
    private void broadcast()
    {
        final ListenerContext listeners;
        
        synchronized( this )
        {
            listeners = this.listeners;
        }
        
        if( listeners != null )
        {
            listeners.broadcast( new Event() );
        }
    }

    @Override
    public int size()
    {
        synchronized( this )
        {
            return this.base.size();
        }
    }

    @Override
    public boolean isEmpty()
    {
        synchronized( this )
        {
            return this.base.isEmpty();
        }
    }

    @Override
    public boolean containsKey( final Object key )
    {
        synchronized( this )
        {
            return this.base.containsKey( key );
        }
    }

    @Override
    public boolean containsValue( final Object value )
    {
        synchronized( this )
        {
            return this.base.containsValue( value );
        }
    }

    @Override
    public V get( final Object key )
    {
        synchronized( this )
        {
            return this.base.get( key );
        }
    }

    @Override
    public V put( final K key, final V value )
    {
        final V previous;
        boolean broadcast = false;
        
        synchronized( this )
        {
            if( this.base.containsKey( key ) )
            {
                previous = this.base.put( key, value );
                
                if( previous != value )
                {
                    broadcast = true;
                }
            }
            else
            {
                this.base.put( key, value );
                previous = null;
                broadcast = true;
            }
        }
        
        if( broadcast )
        {
            broadcast();
        }
        
        return previous;
    }

    @Override
    public void putAll( final Map<? extends K,? extends V> m )
    {
        boolean broadcast = false;
        
        synchronized( this )
        {
            for( final Map.Entry<? extends K,? extends V> entry : m.entrySet() )
            {
                final K key = entry.getKey();
                final V value = entry.getValue();
                
                if( this.base.containsKey( key ) )
                {
                    final V previous = this.base.put( key, value );
                    
                    if( value != previous )
                    {
                        broadcast = true;
                    }
                }
                else
                {
                    this.base.put( key, value );
                    broadcast = true;
                }
            }
        }
        
        if( broadcast )
        {
            broadcast();
        }
    }

    @Override
    public V remove( final Object key )
    {
        final V removed;
        boolean broadcast = false;
        
        synchronized( this )
        {
            if( this.base.containsKey( key ) )
            {
                broadcast = true;
            }
            
            removed = this.base.remove( key );
        }
        
        if( broadcast )
        {
            broadcast();
        }
        
        return removed;
    }

    @Override
    public void clear()
    {
        boolean broadcast = false;
        
        synchronized( this )
        {
            if( ! this.base.isEmpty() )
            {
                broadcast = true;
            }
            
            this.base.clear();
        }
        
        if( broadcast )
        {
            broadcast();
        }
    }

    @Override
    public Set<Map.Entry<K,V>> entrySet()
    {
        synchronized( this )
        {
            if( this.entries == null )
            {
                this.entries = new AbstractSet<Map.Entry<K,V>>()
                {
                    public Iterator<Map.Entry<K,V>> iterator()
                    {
                        synchronized( ObservableMap.this )
                        {
                            return new Iterator<Map.Entry<K,V>>()
                            {
                                private final Iterator<Entry<K,V>> base = ObservableMap.this.base.entrySet().iterator();
                
                                public boolean hasNext()
                                {
                                    synchronized( ObservableMap.this )
                                    {
                                        return this.base.hasNext();
                                    }
                                }
                
                                public Map.Entry<K,V> next()
                                {
                                    synchronized( ObservableMap.this )
                                    {
                                        return this.base.next();
                                    }
                                }
                
                                public void remove()
                                {
                                    synchronized( ObservableMap.this )
                                    {
                                        this.base.remove();
                                        broadcast();
                                    }
                                }
                            };
                        }
                    }
        
                    public int size()
                    {
                        return ObservableMap.this.size();
                    }
        
                    public boolean contains( final Object obj )
                    {
                        synchronized( ObservableMap.this )
                        {
                            return ObservableMap.this.base.entrySet().contains( obj );
                        }
                    }
                };
            }
            
            return this.entries;
        }
    }

}
