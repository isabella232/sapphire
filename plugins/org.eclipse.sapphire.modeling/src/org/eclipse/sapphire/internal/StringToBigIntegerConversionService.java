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

package org.eclipse.sapphire.internal;

import java.math.BigInteger;

import org.eclipse.sapphire.ConversionService;

/**
 * ConversionService implementation for String to BigInteger conversions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StringToBigIntegerConversionService extends ConversionService<String,BigInteger>
{
    public StringToBigIntegerConversionService()
    {
        super( String.class, BigInteger.class );
    }

    @Override
    public BigInteger convert( final String string )
    {
        BigInteger result = null;
        
        try
        {
            result = new BigInteger( string );
        }
        catch( NumberFormatException e )
        {
            // Intentionally ignored.
        }
        
        return result;
    }

}
