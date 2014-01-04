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
import org.eclipse.sapphire.modeling.Path;

/**
 * ConversionService implementation for String to Path conversions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StringToPathConversionService extends ConversionService<String,Path>
{
    public StringToPathConversionService()
    {
        super( String.class, Path.class );
    }

    @Override
    public Path convert( final String string )
    {
        Path result = null;
        
        try
        {
            result = new Path( string );
        }
        catch( IllegalArgumentException e )
        {
            // Intentionally ignored.
        }
        
        return result;
    }

}
