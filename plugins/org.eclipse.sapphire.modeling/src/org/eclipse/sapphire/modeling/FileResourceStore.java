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

package org.eclipse.sapphire.modeling;

import static org.eclipse.sapphire.FileUtil.mkdirs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class FileResourceStore extends ByteArrayResourceStore
{
    private final File file;
    
    public FileResourceStore( final File file ) throws ResourceStoreException
    {
        this.file = file;

        if( this.file.exists() )
        {
            try
            {
                final InputStream in = new FileInputStream( this.file );
                
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
            catch( final IOException e )
            {
                throw new ResourceStoreException( e );
            }
        }
    }
    
    public File getFile()
    {
        return this.file;
    }

    @Override
    public void save() throws ResourceStoreException
    {
        validateSave();

        try
        {
            mkdirs( this.file.getParentFile() );
        }
        catch( final IOException e )
        {
            throw new ResourceStoreException( e );
        }
        
        try
        {
            final OutputStream out = new FileOutputStream( this.file );
            
            try
            {
                out.write( getContents() );
                out.flush();
            }
            finally
            {
                try
                {
                    out.close();
                }
                catch( IOException e ) {}
            }
        }
        catch( final IOException e )
        {
            throw new ResourceStoreException( e );
        }
    }

    @Override
    public void validateSave()
    {
        if( this.file.exists() )
        {
            if( ! this.file.canWrite() )
            {
                // TODO: Add conditional call to Java 6 specific setWritable API.

                //if( ! this.file.setWritable( true ) )
                //{
                //    throw new ValidateEditException();
                //}

                throw new ValidateEditException();
            }
        }
    }

    @Override
    public <A> A adapt( final Class<A> adapterType )
    {
        A result = null;
        
        if( adapterType == File.class )
        {
            result = adapterType.cast( this.file );
        }
        else
        {
            result = super.adapt( adapterType );
        }
        
        return result;
    }
    
}
