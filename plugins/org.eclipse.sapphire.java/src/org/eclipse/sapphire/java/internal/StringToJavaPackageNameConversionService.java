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
import org.eclipse.sapphire.java.JavaPackageName;

/**
 * ConversionService implementation for String to JavaPackageName conversions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StringToJavaPackageNameConversionService extends ConversionService<String,JavaPackageName>
{
    public StringToJavaPackageNameConversionService()
    {
        super( String.class, JavaPackageName.class );
    }

    @Override
    public JavaPackageName convert( final String string )
    {
        JavaPackageName result = null;
        
        try
        {
            result = new JavaPackageName( string );
        }
        catch( IllegalArgumentException e )
        {
            // Intentionally ignored.
        }
        
        return result;
    }

}
