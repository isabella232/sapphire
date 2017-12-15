/******************************************************************************
 * Copyright (c) 2012 Oracle
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
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ClassLocalizationService

    extends StandardLocalizationService
    
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
        
        final InputStream stream = this.cl.getClassLoader().getResourceAsStream( resPath );
        
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
        
        return false;
    }

}
