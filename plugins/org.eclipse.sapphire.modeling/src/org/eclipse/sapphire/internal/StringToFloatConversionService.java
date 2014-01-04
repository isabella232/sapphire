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

import org.eclipse.sapphire.ConversionService;

/**
 * ConversionService implementation for String to Float conversions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StringToFloatConversionService extends ConversionService<String,Float>
{
    public StringToFloatConversionService()
    {
        super( String.class, Float.class );
    }

    @Override
    public Float convert( final String string )
    {
        Float result = null;
        
        try
        {
            result = Float.valueOf( string );
        }
        catch( NumberFormatException e )
        {
            // Intentionally ignored.
        }
        
        return result;
    }

}
