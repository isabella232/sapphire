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

package org.eclipse.sapphire.java.internal;

import org.eclipse.sapphire.ConversionService;
import org.eclipse.sapphire.java.JavaIdentifier;

/**
 * ConversionService implementation for String to JavaIdentifier conversions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StringToJavaIdentifierConversionService extends ConversionService<String,JavaIdentifier>
{
    public StringToJavaIdentifierConversionService()
    {
        super( String.class, JavaIdentifier.class );
    }

    @Override
    public JavaIdentifier convert( final String string )
    {
        JavaIdentifier result = null;
        
        try
        {
            result = new JavaIdentifier( string );
        }
        catch( IllegalArgumentException e )
        {
            // Intentionally ignored.
        }
        
        return result;
    }

}
