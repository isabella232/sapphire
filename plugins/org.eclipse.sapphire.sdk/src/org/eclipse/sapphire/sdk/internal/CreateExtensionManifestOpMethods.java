/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.sdk.internal;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.sapphire.modeling.ProgressMonitor;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.platform.StatusBridge;
import org.eclipse.sapphire.sdk.CreateExtensionManifestOp;
import org.eclipse.sapphire.sdk.extensibility.SapphireExtensionDef;
import org.eclipse.sapphire.workspace.WorkspaceFileResourceStore;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class CreateExtensionManifestOpMethods
{
    public static Status execute( final CreateExtensionManifestOp operation,
                                  ProgressMonitor monitor )
    {
        if( monitor == null )
        {
            monitor = new ProgressMonitor();
        }
        
        monitor.beginTask( Resources.executeTaskName, 3 );
        
        try
        {
            final IFile newFileHandle = operation.getFileHandle();
            
            try
            {
                newFileHandle.refreshLocal( IResource.DEPTH_ZERO, new NullProgressMonitor() );
                
                if( newFileHandle.exists() )
                {
                    newFileHandle.setContents( new ByteArrayInputStream( new byte[ 0 ] ), IFile.FORCE, new NullProgressMonitor() );
                }
            }
            catch( CoreException e )
            {
                return StatusBridge.create( e.getStatus() );
            }
            
            monitor.worked( 1 );
            
            try
            {
                final RootXmlResource resource = new RootXmlResource( new XmlResourceStore( new WorkspaceFileResourceStore( newFileHandle ) ) );
                final SapphireExtensionDef model = SapphireExtensionDef.TYPE.instantiate( resource );
                model.adapt( XmlResource.class ).getXmlElement( true );
                model.resource().save();
            }
            catch( ResourceStoreException e )
            {
                return Status.createErrorStatus( e );
            }
            
            monitor.worked( 1 );
        }
        finally
        {
            monitor.done();
        }
        
        return Status.createOkStatus();
    }
    
    private static final class Resources extends NLS
    {
        public static String executeTaskName;
        
        static
        {
            initializeMessages( CreateExtensionManifestOpMethods.class.getName(), Resources.class );
        }
    }
    
}
