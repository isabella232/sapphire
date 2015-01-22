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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class FilteredListener<T extends Event> extends Listener
{
    private final Class<T> type;
    
    protected FilteredListener()
    {
        this.type = findEventType( getClass() );
    }
    
    @Override
    public final void handle( final Event event )
    {
        if( this.type.isInstance( event ) )
        {
            handleTypedEvent( this.type.cast( event ) );
        }
    }
    
    protected abstract void handleTypedEvent( T event );
    
    @SuppressWarnings( "unchecked" )
    private static <E extends Event> Class<E> findEventType( final Class<?> cl )
    {
        final Type superGenericType = cl.getGenericSuperclass();
        
        if( superGenericType instanceof ParameterizedType )
        {
            final ParameterizedType superParameterizedType = (ParameterizedType) superGenericType;
            final Class<?> superRawType = (Class<?>) superParameterizedType.getRawType();
            
            if( superRawType == FilteredListener.class )
            {
                return (Class<E>) superParameterizedType.getActualTypeArguments()[ 0 ];
            }
            else
            {
                return findEventType( superRawType );
            }
        }
        else
        {
            final Class<?> superRawType = (Class<?>) superGenericType;
            
            if( superRawType == FilteredListener.class )
            {
                throw new IllegalStateException( "FilteredListener must be parameterized." );
            }
            else
            {
                return findEventType( superRawType );
            }
        }
    }
    
}
