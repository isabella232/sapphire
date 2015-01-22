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

package org.eclipse.sapphire.workspace;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.ValidateEditException;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class WorkspaceFileResourceStore extends ByteArrayResourceStore
{
    private final IFile file;
    private long modStamp;
    
    public WorkspaceFileResourceStore( final IFile file )
    
        throws ResourceStoreException
        
    {
        this.file = file;
        this.modStamp = -1;
        
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
                throw new ResourceStoreException( e );
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
    }
    
    public IFile getWorkspaceFile()
    {
        return this.file;
    }
    
    public File getFile()
    {
        return this.file.getLocation().toFile();
    }

    @Override
    public void save()
    
        throws ResourceStoreException
        
    {
        validateSave();
        
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
                    create( this.file.getParent() );
                    this.file.create( stream, true, null );
                }
            }
            
            this.modStamp = this.file.getModificationStamp();
        }
        catch( CoreException e )
        {
            throw new ResourceStoreException( e );
        }
    }
    
    @Override
    public void validateSave()
    {
        final IWorkspace ws = ResourcesPlugin.getWorkspace();

        if( ! ws.validateEdit( new IFile[] { this.file }, IWorkspace.VALIDATE_PROMPT ).isOK() )
        {
            throw new ValidateEditException();
        }
    }

    @Override
    public boolean isOutOfDate()
    {
        return ( this.modStamp != this.file.getModificationStamp() );
    }

    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof WorkspaceFileResourceStore )
        {
            return this.file.getLocation().equals( ( (WorkspaceFileResourceStore) obj ).getWorkspaceFile().getLocation() );
        }
        
        return false;
    }

    @Override
    public int hashCode()
    {
        return this.file.hashCode();
    }
    
    public static void create( final IContainer container ) throws CoreException
    {
        if( ! container.exists() )
        {
            create( container.getParent() );
            
            final IFolder iFolder = (IFolder) container;
            iFolder.create( true, true, null );
        }
    }

}
