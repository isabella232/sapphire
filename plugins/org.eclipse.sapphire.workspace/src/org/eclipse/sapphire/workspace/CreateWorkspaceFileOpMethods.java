/******************************************************************************
 * Copyright (c) 2013 Oracle
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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.sapphire.FileName;
import org.eclipse.sapphire.MasterConversionService;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.ProgressMonitor;
import org.eclipse.sapphire.modeling.Resource;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.platform.PathBridge;
import org.eclipse.sapphire.platform.StatusBridge;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class CreateWorkspaceFileOpMethods
{
    public static IFile getFileHandle( final CreateWorkspaceFileOp operation )
    {
        final Path folderPath = operation.getFolder().getContent();
        final FileName fileName = operation.getFileName().getContent();
        
        if( folderPath == null || fileName == null )
        {
            return null;
        }
        
        final Path newFilePath = folderPath.append( fileName.toString() );
        
        return ResourcesPlugin.getWorkspace().getRoot().getFile( PathBridge.create( newFilePath ) );
    }
    
    public static Status execute( final CreateWorkspaceFileOp operation,
                                  ProgressMonitor monitor )
    {
        if( monitor == null )
        {
            monitor = new ProgressMonitor();
        }
        
        monitor.beginTask( Resources.executeTaskName, 2 );
        
        try
        {
            final IFile newFileHandle = operation.getFileHandle();
            
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
                final ModelElementType type = ModelElementType.read( fileTypeAnnotation.value() );
                final Resource resource = type.service( MasterConversionService.class ).convert( newFileHandle, Resource.class );
                
                if( resource != null )
                {
                    try
                    {
                        final IModelElement element = type.instantiate( resource );
                        
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
    
    private static final class Resources extends NLS
    {
        public static String executeTaskName;
        
        static
        {
            initializeMessages( CreateWorkspaceFileOpMethods.class.getName(), Resources.class );
        }
    }
    
}
