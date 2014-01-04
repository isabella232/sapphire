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

import java.math.BigDecimal;

import org.eclipse.sapphire.ConversionService;

/**
 * ConversionService implementation for String to BigDecimal conversions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StringToBigDecimalConversionService extends ConversionService<String,BigDecimal>
{
    public StringToBigDecimalConversionService()
    {
        super( String.class, BigDecimal.class );
    }

    @Override
    public BigDecimal convert( final String string )
    {
        BigDecimal result = null;
        
        try
        {
            result = new BigDecimal( string );
        }
        catch( NumberFormatException e )
        {
            // Intentionally ignored.
        }
        
        return result;
    }

}
