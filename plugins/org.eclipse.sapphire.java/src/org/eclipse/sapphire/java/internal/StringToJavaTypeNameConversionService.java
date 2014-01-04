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
import org.eclipse.sapphire.java.JavaTypeName;

/**
 * ConversionService implementation for String to JavaTypeName conversions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StringToJavaTypeNameConversionService extends ConversionService<String,JavaTypeName>
{
    public StringToJavaTypeNameConversionService()
    {
        super( String.class, JavaTypeName.class );
    }

    @Override
    public JavaTypeName convert( final String string )
    {
        JavaTypeName result = null;
        
        try
        {
            result = new JavaTypeName( string );
        }
        catch( IllegalArgumentException e )
        {
            // Intentionally ignored.
        }
        
        return result;
    }

}
