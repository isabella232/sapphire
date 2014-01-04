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

package org.eclipse.sapphire.modeling.localization;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ClassLocalizationService extends StandardLocalizationService
{
    private final Class<?> cl;
    
    public ClassLocalizationService( final Class<?> cl,
                                     final Locale locale )
    {
        super( locale );

        this.cl = cl;
    }

    @Override
    protected boolean load( final Locale locale,
                            final Map<String,String> keyToText )
    {
        final String path = this.cl.getName().replace( '.', '/' );
        
        String resPath = path;
        final String localeString = locale.toString();
        
        if( localeString.length() > 0 )
        {
            resPath = resPath + "_" + localeString;
        }
        
        resPath = resPath + ".properties";
        
        ClassLoader loader = this.cl.getClassLoader();
        
        if( loader == null )
        {
            // Some JVM implementations, in certain circumstances (perhaps related to running with different
            // locales) will return null from Class.getClassLoader() call to signify that the class was
            // loaded by the bootstrap class loader. Try to recover.
            
            loader = ClassLoader.getSystemClassLoader();
        }
        
        if( loader != null )
        {
            final InputStream stream = loader.getResourceAsStream( resPath );
            
            if( stream != null )
            {
                try
                {
                    return parse( stream, keyToText );
                }
                finally
                {
                    try
                    {
                        stream.close();
                    }
                    catch( IOException e ) {}
                }
            }
        }
        
        return false;
    }

}
