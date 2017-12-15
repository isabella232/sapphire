/******************************************************************************
 * Copyright (c) 2013 Oracle and Liferay
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

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
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

}
