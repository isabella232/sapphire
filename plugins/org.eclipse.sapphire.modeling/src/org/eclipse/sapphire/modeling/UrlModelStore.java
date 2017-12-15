/******************************************************************************
 * Copyright (c) 2011 Oracle
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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class UrlModelStore

    extends ByteArrayModelStore
    
{
    private final URL url;
    
    /**
     * A URI object for the URL associated with this model store. The URI is used for comparison
     * operations since URL can block for domain name resolution to equate host names if they 
     * resolve to the same IP address.
     */
    
    private final URI uri;
    
    public UrlModelStore( final URL url )
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
    }
    
    @Override
    public void open() throws IOException
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

    @Override
    public boolean validateEdit()
    {
        return false;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof UrlModelStore )
        {
            final UrlModelStore ums = ( (UrlModelStore) obj );
            
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
   
}
