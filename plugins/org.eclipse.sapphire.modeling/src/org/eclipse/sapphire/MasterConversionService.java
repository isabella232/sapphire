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

package org.eclipse.sapphire;

import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.services.Service;

/**
 * Converts an object to the specified type by delegating to available ConversionService implementations. If object
 * is null or is already of desired type, the object is returned unchanged.
 * 
 * <p>An implementation of this service is provided with Sapphire. This service is not intended to
 * be implemented by adopters.</p>
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class MasterConversionService extends Service
{
    /**
     * Converts an object to the specified type.
     * 
     * @param object the object to convert
     * @param type the desired type of a converted object
     * @return the converted object or null if could not be converted
     * @throws IllegalArgumentException if type is null
     */
    
    public <T> T convert( final Object object, final Class<T> type )
    {
        if( type == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( object == null )
        {
            return null;
        }
        
        if( type.isInstance( object ) )
        {
            return type.cast( object );
        }
        
        for( ConversionService service : services( ConversionService.class ) )
        {
            T result = null;
            
            try
            {
                result = service.convert( object, type );
            }
            catch( Exception e )
            {
                LoggingService.log( e );
            }
            
            if( result != null )
            {
                return result;
            }
        }
        
        return null;
    }
    
}
