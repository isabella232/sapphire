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

package org.eclipse.sapphire;

import org.eclipse.sapphire.services.Service;

/**
 * Converts an object to the specified type.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class ConversionService<S,T> extends Service
{
    private final Class<S> source;
    private final Class<T> target;
    
    public ConversionService( final Class<S> source,
                              final Class<T> target )
    {
        if( source == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( target == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.source = source;
        this.target = target;
    }
    
    /**
     * Returns the conversion's source type
     * 
     * @return the conversion's source type
     */
    
    public final Class<S> source()
    {
        return this.source;
    }
    
    /**
     * Return the conversion's target type.
     * 
     * @return the conversion's target type
     */
    
    public final Class<T> target()
    {
        return this.target;
    }
    
    /**
     * Converts an object of source type to target type.
     * 
     * @param object the object to convert
     * @return the converted object or null if this implementation cannot convert, but it is ok to try other implementations
     * @throws ConversionException if the object cannot be converted and no other implementation should be tried
     */
    
    public abstract T convert( S object ) throws ConversionException;
    
}
