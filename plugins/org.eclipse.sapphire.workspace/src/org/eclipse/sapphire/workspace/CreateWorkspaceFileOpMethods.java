/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.workspace;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.MasterConversionService;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.ProgressMonitor;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.platform.StatusBridge;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class CreateWorkspaceFileOpMethods
{
    @Text( "Creating file..." )
    private static LocalizableText executeTaskName;
    
    static
    {
        LocalizableText.init( CreateWorkspaceFileOpMethods.class );
    }
    
    public static Status execute( final CreateWorkspaceFileOp operation,
                                  ProgressMonitor monitor )
    {
        if( monitor == null )
        {
            monitor = new ProgressMonitor();
        }
        
        monitor.beginTask( executeTaskName.text(), 2 );
        
        try
        {
            final IFile newFileHandle = operation.getFile().target();
            
            try
            {
                newFileHandle.refreshLocal( IResource.DEPTH_ZERO, new NullProgressMonitor() );
                
                create( newFileHandle.getParent() );
                
                if( newFileHandle.exists() )
                {
                    newFileHandle.setContents( new ByteArrayInputStream( new byte[ 0 ] ), IFile.FORCE, null );
                }
                else
                {
                    newFileHandle.create( new ByteArrayInputStream( new byte[ 0 ] ), IFile.FORCE, null );
                }
            }
            catch( CoreException e )
            {
                return StatusBridge.create( e.getStatus() );
            }
            
            monitor.worked( 1 );
            
            final WorkspaceFileType fileTypeAnnotation = operation.type().getAnnotation( WorkspaceFileType.class );
            
            if( fileTypeAnnotation != null )
            {
                final ElementType type = ElementType.read( fileTypeAnnotation.value() );
                final Resource resource = type.service( MasterConversionService.class ).convert( newFileHandle, Resource.class );
                
                if( resource != null )
                {
                    try
                    {
                        final Element element = type.instantiate( resource );
                        
                        try
                        {
                            element.initialize();
                            resource.save();
                        }
                        finally
                        {
                            element.dispose();
                        }
                    }
                    catch( ResourceStoreException e )
                    {
                        return Status.createErrorStatus( e );
                    }
                }
            }
        }
        finally
        {
            monitor.done();
        }
        
        return Status.createOkStatus();
    }
    
    private static void create( final IContainer container ) throws CoreException
    {
        if( ! container.exists() )
        {
            create( container.getParent() );
            
            final IFolder iFolder = (IFolder) container;
            iFolder.create( true, true, null );
        }
    }
    
}
