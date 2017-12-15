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

package org.eclipse.sapphire.util;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class HashCodeFactory
{
    private final int result;
    
    private HashCodeFactory( final int result )
    {
        this.result = result;
    }
    
    public static HashCodeFactory start()
    {
        return new HashCodeFactory( 1 );
    }
    
    public HashCodeFactory add( final Object object )
    {
        if( object == null )
        {
            return this;
        }
        else
        {
            return new HashCodeFactory( this.result * object.hashCode() );
        }
    }
    
    public int result()
    {
        return this.result;
    }
    
}
