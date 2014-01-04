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
 * ConversionService implementation for String to Long conversions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StringToLongConversionService extends ConversionService<String,Long>
{
    public StringToLongConversionService()
    {
        super( String.class, Long.class );
    }

    @Override
    public Long convert( final String string )
    {
        Long result = null;
        
        try
        {
            result = Long.valueOf( string );
        }
        catch( NumberFormatException e )
        {
            // Intentionally ignored.
        }
        
        return result;
    }

}
