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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.sapphire.ConversionService;

/**
 * ConversionService implementation for String to URL conversions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StringToUrlConversionService extends ConversionService<String,URL>
{
    public StringToUrlConversionService()
    {
        super( String.class, URL.class );
    }

    @Override
    public URL convert( final String string )
    {
        URL result = null;
        
        try
        {
            result = new URL( string );
        }
        catch( MalformedURLException e )
        {
            // Intentionally ignored.
        }
        
        return result;
    }

}
