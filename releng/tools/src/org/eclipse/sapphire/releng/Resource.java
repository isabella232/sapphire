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

package org.eclipse.sapphire.releng;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;

/**
 * @author <a href="konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class Resource
{
    private final URL url;
    private final String name;
    
    public Resource( final URL url, final String name )
    {
        if( url == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( name == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.url = url;
        this.name = name;
    }
    
    public String text()
    {
        Reader reader = null;
        
        try
        {
            reader = new InputStreamReader( this.url.openStream(), "UTF-8" );
            
            final StringBuilder content = new StringBuilder();
            
            char[] buffer = new char[ 1024 ];
            int count = 0;
            
            while( ( count = reader.read( buffer ) ) != -1 )
            {
                content.append( buffer, 0, count );
            }
            
            return content.toString();
        }
        catch( final Exception e )
        {
            throw new RuntimeException( e );
        }
        finally
        {
            if( reader != null )
            {
                try
                {
                    reader.close();
                }
                catch( final IOException e ) {}
            }
        }
    }
    
    public void copy( final File folder )
    {
        if( folder == null || ! folder.isDirectory() )
        {
            throw new IllegalArgumentException();
        }
        
        InputStream in = null;
        
        try
        {
            in = this.url.openStream();
            
            final OutputStream out = new FileOutputStream( new File( folder, this.name ) );
            
            try
            {
                byte[] buffer = new byte[ 4 * 1024 ];
                int count = 0;
                
                while( ( count = in.read( buffer ) ) != -1 )
                {
                    out.write( buffer, 0, count );
                }
            }
            finally
            {
                if( out != null )
                {
                    try
                    {
                        out.close();
                    }
                    catch( final IOException e ) {}
                }
            }
        }
        catch( final IOException e )
        {
            throw new RuntimeException( e );
        }
        finally
        {
            if( in != null )
            {
                try
                {
                    in.close();
                }
                catch( final IOException e ) {}
            }
        }
    }
    
}