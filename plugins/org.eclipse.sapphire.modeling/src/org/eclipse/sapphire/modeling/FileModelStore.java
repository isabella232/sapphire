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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.sapphire.modeling.util.internal.FileUtil;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class FileModelStore

    extends ByteArrayModelStore
    implements IFileModelStore
    
{
    private final File file;
    
    public FileModelStore( final File file )
    {
        this.file = file;
    }
    
    public File getFile()
    {
        return this.file;
    }

    @Override
    public void open() throws IOException
    {
        if( this.file.exists() )
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
        else
        {
            setContents( EMPTY_BYTE_ARRAY );
        }
    }

    @Override
    public void save() throws IOException
    {
        if( validateEdit() )
        {
            try
            {
                FileUtil.mkdirs( this.file.getParentFile() );
            }
            catch( CoreException e )
            {
                throw new IOException( e.getMessage() );
            }
            
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
        else
        {
            throw new ValidateEditException();
        }
    }

    @Override
    public boolean validateEdit()
    {
        if( this.file.exists() )
        {
            if( this.file.canWrite() )
            {
                return true;
            }
            else
            {
                // Java 6 API. Need to add smarts to access if available.

                //if( this.file.setWritable( true ) )
                //{
                //    return true;
                //}
                
                return false;
            }
        }
        else
        {
            // Don't really have a way to check if writing file to a directory will work,
            // so we are assuming here that it will.
            
            return true;
        }
    }
   
}
