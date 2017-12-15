/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.services;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ValueSerializationService extends Service
{
    public String encode( final Object value )
    {
        String result = null;
        
        if( value != null )
        {
            result = value.toString();
        }
        
        return result;
    }
    
    public Object decode( final String value )
    {
        if( value != null )
        {
            return decodeFromString( value.trim() );
        }
        
        return null;
    }
    
    /**
     * Decodes a string into a value object. Implementations should assume that the
     * passed in string is not null and has been trimmed. If the string cannot be decoded
     * into a value, this method should return null rather than throwing any form of
     * exception.
     * 
     * @param value the string to decode
     * @return the decoded value object or null if unable to decode
     */
    
    protected abstract Object decodeFromString( String value );
    
}
