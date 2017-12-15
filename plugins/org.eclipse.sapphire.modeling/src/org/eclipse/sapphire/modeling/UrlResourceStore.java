/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.localization.StandardLocalizationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class UrlResourceStore extends ByteArrayResourceStore
{
    private final URL url;
    
    public UrlResourceStore( final URL url ) throws ResourceStoreException
    {
        this.url = url;
        
        try( InputStream in = this.url.openStream() )
        {
            setContents( in );
        }
        catch( final IOException e )
        {
            throw new ResourceStoreException( e );
        }
    }
    
    @Override
    public void validateSave()
    {
        throw new ValidateEditException();
    }

    @Override
    protected LocalizationService initLocalizationService( final Locale locale )
    {
        return new StandardLocalizationService( locale )
        {
            @Override
            protected boolean load( final Locale locale,
                                    final Map<String,String> keyToText )
            {
                final String origFileUrlString = UrlResourceStore.this.url.toString();
                final int lastDot = origFileUrlString.lastIndexOf( '.' );
                
                if( lastDot != -1 )
                {
                    String resFileUrlString = origFileUrlString.substring( 0, lastDot );
                    final String localeString = locale.toString();
                    
                    if( localeString.length() > 0 )
                    {
                        resFileUrlString = resFileUrlString + "_" + localeString;
                    }
                    
                    resFileUrlString = resFileUrlString + ".properties";
                    
                    URL resFileUrl = null;
                    
                    try
                    {
                        resFileUrl = new URL( resFileUrlString );
                    }
                    catch( MalformedURLException e )
                    {
                        return false;
                    }
                    
                    try( InputStream stream = resFileUrl.openStream() )
                    {
                        return parse( stream, keyToText );
                    }
                    catch( final IOException e )
                    {
                        return false;
                    }
                }
                
                return false;
            }
        };
    }
   
}
