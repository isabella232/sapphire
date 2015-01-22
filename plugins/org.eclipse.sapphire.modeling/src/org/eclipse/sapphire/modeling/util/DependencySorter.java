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

package org.eclipse.sapphire.modeling.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.sapphire.util.ListFactory;

/**
 * Sorts objects in dependencies-first order.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DependencySorter<K,T>
{
    private final Map<K,T> keyToObject = new LinkedHashMap<K,T>();
    private final Map<T,K> objectToKey = new LinkedHashMap<T,K>();
    private final Map<K,Set<K>> dependencies = new LinkedHashMap<K,Set<K>>();
    
    public void add( final K key, final T object )
    {
        if( key == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( object == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( this.keyToObject.containsKey( key ) )
        {
            if( this.keyToObject.get( key ) != object )
            {
                throw new IllegalArgumentException();
            }
        }
        else
        {
            this.keyToObject.put( key, object );
            this.objectToKey.put( object, key );
            
            if( this.dependencies.get( key ) == null )
            {
                this.dependencies.put( key, new LinkedHashSet<K>() );
            }
        }
    }
    
    /**
     * Determines if the sorter contains an object with the specified key.
     * 
     * @param key the object key
     * @return true if and only if the sorter contains an object with the specified key
     */
    
    public boolean contains( final K key )
    {
        return ( this.keyToObject.get( key ) != null );
    }
    
    public void dependency( final K from, final K to )
    {
        if( from == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( to == null )
        {
            throw new IllegalArgumentException();
        }
        
        Set<K> set = this.dependencies.get( from );
        
        if( set == null )
        {
            set = new LinkedHashSet<K>();
            this.dependencies.put( from, set );
        }
        
        set.add( to );
    }
    
    /**
     * Returns the sorted list. The returned list is not modifiable.
     * 
     * @return the sorted list
     */
    
    public List<T> sort()
    {
        // Return early if no objects defined.
        
        if( this.keyToObject.isEmpty() )
        {
            return Collections.emptyList();
        }
        
        // Find all objects with no incoming dependencies.
        
        final List<T> roots = new ArrayList<T>();
        
        for( final K key : this.dependencies.keySet() )
        {
            boolean found = false;
            
            for( final Set<K> dependencies : this.dependencies.values() )
            {
                if( dependencies.contains( key ) )
                {
                    found = true;
                    break;
                }
            }
            
            if( ! found )
            {
                final T object = this.keyToObject.get( key );
                
                if( object != null )
                {
                    roots.add( object );
                }
            }
        }
        
        // Start with the roots and try to visit all objects. The ones not visited
        // are involved in detached loops. Pick one of the not visited objects to
        // be another root and visit what can be reached from that object. Repeat
        // until all objects have been visited.
        
        final Set<T> visited = new HashSet<T>();
        
        for( T root : roots )
        {
            visit( root, visited );
        }
        
        while( visited.size() != this.keyToObject.size() )
        {
            for( T object : this.keyToObject.values() )
            {
                if( ! visited.contains( object ) )
                {
                    roots.add( object );
                    visit( object, visited );
                    break;
                }
            }
        }
        
        // Finally, traverse the dependency trees of each root and add objects in depth-first order. The visited set
        // tracks objects already traversed so that we can break cycles.
        
        final ListFactory<T> result = ListFactory.start();
        visited.clear();
        
        for( T root : roots )
        {
            traverse( root, visited, result );
        }
        
        return result.result();
    }
    
    private void visit( final T object,
                        final Set<T> visited )
    {
        if( visited.contains( object ) )
        {
            return;
        }
        
        visited.add( object );
        
        for( K key : this.dependencies.get( this.objectToKey.get( object ) ) )
        {
            final T x = this.keyToObject.get( key );
            
            if( x != null )
            {
                visit( x, visited );
            }
        }
    }

    private void traverse( final T object,
                           final Set<T> visited,
                           final ListFactory<T> result )
    {
        if( visited.contains( object ) )
        {
            return;
        }
        
        visited.add( object );
        
        for( K key : this.dependencies.get( this.objectToKey.get( object ) ) )
        {
            final T x = this.keyToObject.get( key );
            
            if( x != null )
            {
                traverse( x, visited, result );
            }
        }
        
        result.add( object );
    }
    
}
