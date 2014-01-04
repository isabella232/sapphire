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

/**
 * A wrapper for a function result that allows the function caller to select between
 * getting a null or an exception if no data is returned.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class Result<T>
{
    private final T object;
    private final RuntimeException exception;
    
    private Result( final T object )
    {
        this.object = object;
        this.exception = null;
    }
    
    private Result( final RuntimeException exception )
    {
        this.object = null;
        this.exception = exception;
    }
    
    public static <T> Result<T> success( final T object )
    {
        return new Result<T>( object );
    }
    
    public static <T> Result<T> failure( final RuntimeException exception )
    {
        return new Result<T>( exception );
    }
    
    public T required()
    {
        if( this.exception != null )
        {
            throw this.exception;
        }
        
        return this.object;
    }
    
    public T optional()
    {
        return this.object;
    }
}
