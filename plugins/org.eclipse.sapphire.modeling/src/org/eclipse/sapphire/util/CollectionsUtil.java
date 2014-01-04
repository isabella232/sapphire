/******************************************************************************
 * Copyright (c) 2014 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [358295] Need access to selection in list property editor
 ******************************************************************************/

package org.eclipse.sapphire.util;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class CollectionsUtil
{
    private CollectionsUtil() {}

    public static <E> E findPrecedingItem( final List<E> list,
                                           final E item )
    {
        E lastSeenItem = null;

        for( E x : list )
        {
            if( x == item )
            {
                return lastSeenItem;
            }

            lastSeenItem = x;
        }

        return null;
    }

    public static <E> E findTrailingItem( final List<E> list,
                                          final E item )
    {
        boolean takeNextItem = false;

        for( E x : list )
        {
            if( takeNextItem )
            {
                return x;
            }
            else if( x == item )
            {
                takeNextItem = true;
            }
        }

        return null;
    }
    
    public static <V> V findIgnoringCase( final Map<?,V> map,
                                          final String key )
    {
        V value = map.get( key );
        
        if( value == null )
        {
            for( Map.Entry<?,V> entry : map.entrySet() )
            {
                final Object entryKey = entry.getKey();
                
                if( entryKey instanceof String )
                {
                    if( ( (String) entryKey ).equalsIgnoreCase( key ) )
                    {
                        value = entry.getValue();
                        break;
                    }
                }
            }
        }
        
        return value;
    }

    public static <T> boolean equalsBasedOnEntryIdentity( final List<T> a, 
                                                          final List<T> b )
    {
        if( a == b )
        {
            return true;
        }

        if( a == null || b == null )
        {
            return false;
        }

        final int aSize = a.size();
        final int bSize = b.size();

        if( aSize == bSize )
        {
            for( int i = 0; i < aSize; i++ )
            {
                if( a.get( i ) != b.get( i ) )
                {
                    return false;
                }
            }

            return true;
        }

        return false;
    }
    
    public static boolean containsBasedOnEntryIdentity( final List<?> list, final Object object )
    {
        for( Object obj : list )
        {
            if( obj == object )
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Computes the list of items in A that aren't in B.
     */
    
    public static <T> List<T> removedBasedOnEntryIdentity( final List<? extends T> a, final List<? extends T> b )
    {
        final ListFactory<T> result = ListFactory.start();
        
        for( T obj : a )
        {
            if( ! containsBasedOnEntryIdentity( b, obj ) )
            {
                result.add( obj );
            }
        }
     
        return result.result();
    }

}
