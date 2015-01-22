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

package org.eclipse.sapphire.internal;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.sapphire.ConversionService;

/**
 * ConversionService implementation for String to URI conversions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StringToUriConversionService extends ConversionService<String,URI>
{
    public StringToUriConversionService()
    {
        super( String.class, URI.class );
    }

    @Override
    public URI convert( final String string )
    {
        URI result = null;
        
        try
        {
            result = new URI( string );
        }
        catch( URISyntaxException e )
        {
            // Intentionally ignored.
        }
        
        return result;
    }

}
