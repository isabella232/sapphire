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

package org.eclipse.sapphire.modeling.util.internal;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class FileUtil
{
    public static IFile getWorkspaceFile( final File f )
    {
        final IWorkspace ws = ResourcesPlugin.getWorkspace();
        final IWorkspaceRoot wsroot = ws.getRoot();
        
        final IFile[] wsFiles = wsroot.findFilesForLocationURI( f.toURI() );
        
        if( wsFiles.length > 0 )
        {
            return wsFiles[ 0 ];
        }
        
        return null;
    }

    public static IContainer getWorkspaceContainer( final File f )
    {
        final IWorkspace ws = ResourcesPlugin.getWorkspace();
        final IWorkspaceRoot wsroot = ws.getRoot();
        
        final IContainer[] wsContainers = wsroot.findContainersForLocationURI( f.toURI() );
        
        if( wsContainers.length > 0 )
        {
            return wsContainers[ 0 ];
        }
        
        return null;
    }
    
    public static void mkdirs( final File f )
    
        throws CoreException
        
    {
        final IContainer wsContainer = getWorkspaceContainer( f );
        
        if( f.exists() )
        {
            if( f.isFile() )
            {
                final String msg
                    = NLS.bind( Resources.locationIsFile, 
                                f.getAbsolutePath() );
                
                throw new CoreException( SapphireModelingFrameworkPlugin.createErrorStatus( msg ) );
            }
            else
            {
                // Make sure that the the folder is in the workspace.
                
                if( wsContainer != null )
                {
                    wsContainer.refreshLocal( IResource.DEPTH_ZERO, null );
                }
            }
        }
        else
        {
            mkdirs( f.getParentFile() );
            
            if( wsContainer != null )
            {
                // Should be a folder...
                
                final IFolder iFolder = (IFolder) wsContainer;
                iFolder.create( true, true, null );
            }
            else
            {
                final boolean isSuccessful = f.mkdir();
                
                if( ! isSuccessful )
                {
                    final String msg
                        = NLS.bind( Resources.failedToCreateDirectory, 
                                    f.getAbsolutePath() );
                    
                    throw new CoreException( SapphireModelingFrameworkPlugin.createErrorStatus( msg ) );
                }
            }
        }
    }
    
    private static final class Resources
    
        extends NLS
        
    {
        public static String failedToCreateDirectory;
        public static String locationIsFile;
        
        static
        {
            initializeMessages( FileUtil.class.getName(), 
                                Resources.class );
        }
    }

}
