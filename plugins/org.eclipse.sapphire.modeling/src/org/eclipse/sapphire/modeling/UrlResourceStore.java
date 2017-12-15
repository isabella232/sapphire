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

package org.eclipse.sapphire.modeling;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.localization.StandardLocalizationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class UrlResourceStore

    extends ByteArrayResourceStore
    
{
    private final URL url;
    
    /**
     * A URI object for the URL associated with this model store. The URI is used for comparison
     * operations since URL can block for domain name resolution to equate host names if they 
     * resolve to the same IP address.
     */
    
    private final URI uri;
    
    public UrlResourceStore( final URL url )
    
        throws ResourceStoreException
        
    {
        URI uri = null;
        
        try
        {
            uri = url.toURI();
        }
        catch( URISyntaxException e )
        {
            // Intentionally ignoring. This class has fail-over behavior if there is no URI.
        }
        
        this.url = url;
        this.uri = uri;
        
        try
        {
            final InputStream in = this.url.openStream();
            
            try
            {
                setContents( in );
            }
            finally
            {
                try
                {
                    in.close();
                }
                catch( IOException e ) {}
            }
        }
        catch( IOException e )
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
    public boolean equals( final Object obj )
    {
        if( obj instanceof UrlResourceStore )
        {
            final UrlResourceStore ums = ( (UrlResourceStore) obj );
            
            if( this.uri == null || ums.uri == null )
            {
                return this.url.toString().equals( ums.url.toString() );
            }
            else
            {
                return this.uri.equals( ums.uri );
            }
        }
        
        return false;
    }

    @Override
    public int hashCode()
    {
        return ( this.uri != null ? this.uri.hashCode() : this.url.toString().hashCode() );
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
                    
                    try
                    {
                        final InputStream stream = resFileUrl.openStream();
                        
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
                    catch( IOException e )
                    {
                        return false;
                    }
                }
                
                return false;
            }
        };
    }
   
}
