/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire;

import static org.eclipse.sapphire.Result.failure;
import static org.eclipse.sapphire.Result.success;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.sapphire.modeling.BinaryData;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
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
    
    public static Result<ImageData> readFromStream( final InputStream stream )
    {
        try
        {
            return success( new ImageData( stream ) );
        }
        catch( IOException e )
        {
            return failure( new IllegalArgumentException( e ) );
        }
        finally
        {
            try
            {
                stream.close();
            }
            catch( Exception e ) {}
        }
    }

    public static Result<ImageData> readFromUrl( final URL url )
    {
        try
        {
            return readFromStream( url.openStream() );
        }
        catch( IOException e )
        {
            return failure( new IllegalArgumentException( e ) );
        }
    }

    public static Result<ImageData> readFromClassLoader( final Class<?> cl, final String path )
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
                    final ImageData img = readFromClassLoader( classloader, possibleFullPath ).optional();
                    
                    if( img != null )
                    {
                        return success( img );
                    }
                }
            }
        }
        
        return readFromClassLoader( classloader, path );
    }
    
    public static Result<ImageData> readFromClassLoader( final ClassLoader cl, final String path )
    {
        final URL url = cl.getResource( path );
        
        if( url != null )
        {
            return readFromUrl( url );
        }
        
        return failure( new IllegalArgumentException() );
    }
    
}
