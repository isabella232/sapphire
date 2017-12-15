/******************************************************************************
 * Copyright (c) 2013 Oracle
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.sapphire.util.IdentityHashSet;

/**
 * Sorts objects in dependencies-first order.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class DependencySorter<K,T>
{
    private final Map<K,T> objectByKey = new HashMap<K,T>();
    private final Map<T,Set<K>> objectToDependencies = new IdentityHashMap<T,Set<K>>();
    
    public void add( final K key,
                     final T object )
    {
        if( key == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( object == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( this.objectByKey.containsKey( key ) )
        {
            if( this.objectByKey.get( key ) != object )
            {
                throw new IllegalArgumentException();
            }
        }
        else
        {
            this.objectByKey.put( key, object );
            this.objectToDependencies.put( object, new HashSet<K>() );
        }
    }
    
    public void dependency( final T from,
                            final K toKey )
    {
        if( ! this.objectToDependencies.containsKey( from ) )
        {
            throw new IllegalArgumentException();
        }
        
        this.objectToDependencies.get( from ).add( toKey );
    }
    
    public List<T> sort()
    {
        // Return early if no objects defined.
        
        if( this.objectToDependencies.isEmpty() )
        {
            return Collections.emptyList();
        }
        
        // Find all objects with no incoming dependencies.
        
        final List<T> roots = new ArrayList<T>();
        
        for( T x : this.objectToDependencies.keySet() )
        {
            boolean found = false;
            
            for( Set<K> dependencies : this.objectToDependencies.values() )
            {
                for( K dependency : dependencies )
                {
                    if( this.objectByKey.get( dependency ) == x )
                    {
                        found = true;
                        break;
                    }
                }
                
                if( found )
                {
                    break;
                }
            }
            
            if( ! found )
            {
                roots.add( x );
            }
        }
        
        // Start with the roots and try to visit all objects. The ones not visited
        // are involved in detached loops. Pick one of the not visited objects to
        // be another root and visit what can be reached from that object. Repeat
        // until all objects have been visited.
        
        final Set<T> visited = new IdentityHashSet<T>();
        
        for( T root : roots )
        {
            visit( root, visited );
        }
        
        while( visited.size() != this.objectToDependencies.size() )
        {
            for( T object : this.objectToDependencies.keySet() )
            {
                if( ! visited.contains( object ) )
                {
                    roots.add( object );
                    visit( object, visited );
                    break;
                }
            }
        }
        
        // Traverse the dependency trees of each root and add objects in depth-first
        // order. The visited set tracks objects already traversed so that we can
        // break cycles.
        
        final List<T> list = new ArrayList<T>();
        visited.clear();
        
        for( T root : roots )
        {
            traverse( root, list, visited );
        }
        
        // Return the sorted list.
        
        return list;
    }
    
    private void visit( final T object,
                        final Set<T> visited )
    {
        if( visited.contains( object ) )
        {
            return;
        }
        
        visited.add( object );
        
        for( K key : this.objectToDependencies.get( object ) )
        {
            final T x = this.objectByKey.get( key );
            
            if( x != null )
            {
                visit( x, visited );
            }
        }
    }

    private void traverse( final T object,
                           final List<T> list,
                           final Set<T> visited )
    {
        if( visited.contains( object ) )
        {
            return;
        }
        
        visited.add( object );
        
        for( K key : this.objectToDependencies.get( object ) )
        {
            final T x = this.objectByKey.get( key );
            
            if( x != null )
            {
                traverse( x, list, visited );
            }
        }
        
        list.add( object );
    }
    
}
