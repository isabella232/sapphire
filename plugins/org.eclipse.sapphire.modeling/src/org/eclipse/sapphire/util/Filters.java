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

package org.eclipse.sapphire.util;

import org.eclipse.sapphire.Filter;

/**
 * A collection of common filters.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class Filters
{
    private static final Filter<Object> NOT_NULL_FILTER = new Filter<Object>()
    {
        @Override
        public boolean allows( final Object item )
        {
            return ( item != null );
        }
    };
    
    private static final Filter<String> NOT_EMPTY_FILTER = new Filter<String>()
    {
        @Override
        public boolean allows( final String item )
        {
            return ( item != null && item.trim().length() > 0 );
        }
    };
    
    /**
     * This class is not intended to be instantiated.
     */
    
    private Filters() {}
    
    /**
     * Creates a filter that allows everything except for null.
     * 
     * @return the created filter
     */
    
    @SuppressWarnings( "unchecked" )
    public static <T> Filter<T> createNotNullFilter()
    {
        return (Filter<T>) NOT_NULL_FILTER;
    }
    
    /**
     * Creates a filter for strings that allows everything except for null,
     * empty strings and strings that trim to an empty string.
     * 
     * @return the created filter
     */
    
    public static Filter<String> createNotEmptyFilter()
    {
        return NOT_EMPTY_FILTER;
    }
    
}
