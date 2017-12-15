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
import java.net.URL;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ImageData extends BinaryData
{
    public ImageData( final InputStream stream ) throws IOException
    {
        super( stream );
    }
    
    public ImageData( final byte[] data )
    {
        super( data );
    }
    
    public static ImageData readFromUrl( final URL url )
    {
        InputStream stream = null;
        
        try
        {
            stream = url.openStream();
            return new ImageData( stream );
        }
        catch( IOException e )
        {
            return null;
        }
        finally
        {
            if( stream != null )
            {
                try
                {
                    stream.close();
                }
                catch( Exception e ) {}
            }
        }
    }

    public static ImageData readFromClassLoader( final Class<?> cl,
                                                 final String path )
    {
        final ClassLoader classloader = cl.getClassLoader();
        
        if( ! path.contains( "/" ) )
        {
            final Package p = cl.getPackage();
            
            if( p != null )
            {
                final String pn = p.getName();
                
                if( pn != null && pn.length() > 0 )
                {
                    final String possibleFullPath = pn.replace( '.', '/' ) + "/" + path;
                    final ImageData img = readFromClassLoader( classloader, possibleFullPath );
                    
                    if( img != null )
                    {
                        return img;
                    }
                }
            }
        }
        
        return readFromClassLoader( classloader, path );
    }
    
    public static ImageData readFromClassLoader( final ClassLoader cl,
                                                 final String path )
    {
        final URL url = cl.getResource( path );
        
        if( url != null )
        {
            return readFromUrl( url );
        }
        
        return null;
    }
    
    public static ImageData createFromUrl( final URL url )
    {
        final ImageData image = readFromUrl( url );
        
        if( image == null )
        {
            throw new IllegalArgumentException();
        }
        
        return image;
    }

    public static ImageData createFromClassLoader( final Class<?> cl,
                                                   final String path )
    {
        final ImageData image = readFromClassLoader( cl, path );
        
        if( image == null )
        {
            throw new IllegalArgumentException();
        }
        
        return image;
    }
    
    public static ImageData createFromClassLoader( final ClassLoader cl,
                                                   final String path )
    {
        final ImageData image = readFromClassLoader( cl, path );
        
        if( image == null )
        {
            throw new IllegalArgumentException();
        }
        
        return image;
    }
    
}
