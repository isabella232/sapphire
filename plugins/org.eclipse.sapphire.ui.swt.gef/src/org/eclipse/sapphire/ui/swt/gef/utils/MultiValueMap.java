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

package org.eclipse.sapphire.ui.swt.gef.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.sapphire.util.SetFactory;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class MultiValueMap<K,V>
{
    private final Map<K,Object> map = new HashMap<K,Object>();
    
    @SuppressWarnings( "unchecked" )
    
    public Set<V> get( final K key )
    {
        if( key == null )
        {
            throw new IllegalArgumentException();
        }
        
        final Object value = this.map.get( key );
        
        if( value == null )
        {
            return null;
        }
        
        final SetFactory<V> valueSetFactory = SetFactory.start();
        
        if( value instanceof Set )
        {
            for( final Object obj : (Set<?>) value )
            {
                valueSetFactory.add( (V) obj );
            }
        }
        else
        {
            valueSetFactory.add( (V) value );
        }
        
        return valueSetFactory.result();
    }
    
    @SuppressWarnings( "unchecked" )
    
    public void put( final K key, final V value )
    {
        if( key == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( value != null )
        {
            final Object existing = this.map.get( key );
            
            if( existing == null )
            {
                this.map.put( key, value );
            }
            else if( existing instanceof Set )
            {
                ( (Set<V>) existing ).add( value );
            }
            else if( ! existing.equals( value ) )
            {
                final Set<V> set = new HashSet<V>( 2 );
                set.add( (V) existing );
                set.add( value );
                this.map.put( key, set );
            }
        }
    }
    
    public void remove( final K key, final V value )
    {
        if( key == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( value == null )
        {
            throw new IllegalArgumentException();
        }
        
        final Object existing = this.map.get( key );
        
        if( existing != null )
        {
            if( existing instanceof Set )
            {
                final Set<?> set = (Set<?>) existing;
                
                set.remove( value );
                
                if( set.isEmpty() )
                {
                    this.map.remove( key );
                }
            }
            else if( existing.equals( value ) )
            {
                this.map.remove( key );
            }
        }
    }
    
    public boolean containsKey( final K key )
    {
        return this.map.containsKey( key );
    }
    
    public int size()
    {
        return this.map.size();
    }
    
    public void clear()
    {
        this.map.clear();
    }
}
