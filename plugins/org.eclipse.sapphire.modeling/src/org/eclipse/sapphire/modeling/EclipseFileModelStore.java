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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.sapphire.modeling.util.internal.FileUtil;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class EclipseFileModelStore

    extends ByteArrayModelStore
    implements IEclipseFileModelStore
    
{
    private final IFile file;
    private long modStamp;
    
    public EclipseFileModelStore( final IFile file )
    {
        this.file = file;
        this.modStamp = -1;
    }
    
    public IFile getEclipseFile()
    {
        return this.file;
    }
    
    public File getFile()
    {
        return this.file.getLocation().toFile();
    }

    @Override
    public void open() throws IOException
    {
        if( this.file.exists() )
        {
            InputStream in = null;
            
            try
            {
                this.modStamp = this.file.getModificationStamp();
                in = this.file.getContents();
                setContents( in );
            }
            catch( CoreException e )
            {
                throw new IOException( e.getMessage() );
            }
            finally
            {
                if( in != null )
                {
                    try
                    {
                        in.close();
                    }
                    catch( IOException e ) {}
                }
            }
        }
        else
        {
            setContents( EMPTY_BYTE_ARRAY );
            this.modStamp = -1;
        }
    }

    @Override
    public void save()
    
        throws IOException
        
    {
        if( validateEdit() )
        {
            try
            {
            	final byte[] content = getContents();
                final InputStream stream = new ByteArrayInputStream( content ); 
                
                if( this.file.exists() )
                {
                    this.file.setContents( stream, true, false, null );
                }
                else
                {
                	if( content.length > 0 )
                	{
	                    FileUtil.mkdirs( this.file.getParent().getLocation().toFile() );
	                    this.file.create( stream, true, null );
                	}
                }
                
                this.modStamp = this.file.getModificationStamp();
            }
            catch( CoreException e )
            {
                throw new IOException( e.getMessage() );
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
        final IWorkspace ws = ResourcesPlugin.getWorkspace();
        return ws.validateEdit( new IFile[] { this.file }, IWorkspace.VALIDATE_PROMPT ).isOK();
    }

    @Override
    public boolean isOutOfDate()
    {
        return ( this.modStamp != this.file.getModificationStamp() );
    }

    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof EclipseFileModelStore )
        {
            return this.file.getLocation().equals( ( (EclipseFileModelStore) obj ).getEclipseFile().getLocation() );
        }
        
        return false;
    }

    @Override
    public int hashCode()
    {
        return this.file.hashCode();
    }

}
