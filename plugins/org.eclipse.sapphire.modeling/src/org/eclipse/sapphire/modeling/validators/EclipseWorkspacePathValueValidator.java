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

package org.eclipse.sapphire.modeling.validators;

import static org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin.PLUGIN_ID;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class EclipseWorkspacePathValueValidator

    extends PathValueValidator
    
{
    public EclipseWorkspacePathValueValidator( final ValueProperty property )
    {
        super( property );
    }
    
    @Override
    public IStatus validate( final Value<IPath> value )
    {
        final IPath path = value.getContent( false );
        
        if( path != null )
        {
            final IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember( path );
            
            if( resource != null && resource.exists() )
            {
                if( this.validResourceType == FileSystemResourceType.FILE )
                {
                    if( resource.getType() == IResource.FILE )
                    {
                        return validateExtensions( path );
                    }
                    else
                    {
                        final String message = Resources.bind( Resources.pathIsNotFile, path.toString() );
                        return new Status( Status.ERROR, PLUGIN_ID, message );
                    }
                }
                else if( this.validResourceType == FileSystemResourceType.FOLDER )
                {
                    if( resource.getType() != IResource.FOLDER && resource.getType() != IResource.PROJECT )
                    {
                        final String message = Resources.bind( Resources.pathIsNotFolder, path.toString() );
                        return new Status( Status.ERROR, PLUGIN_ID, message );
                    }
                }
            }
            else
            {
                if( this.resourceMustExist )
                {
                    if( this.validResourceType == FileSystemResourceType.FILE )
                    {
                        final String message = Resources.bind( Resources.fileMustExist, path.toString() );
                        return new Status( Status.ERROR, PLUGIN_ID, message );
                    }
                    else if( this.validResourceType == FileSystemResourceType.FOLDER )
                    {
                        final String message = Resources.bind( Resources.folderMustExist, path.toString() );
                        return new Status( Status.ERROR, PLUGIN_ID, message );
                    }
                    else
                    {
                        final String message = Resources.bind( Resources.resourceMustExist, path.toString() );
                        return new Status( Status.ERROR, PLUGIN_ID, message );
                    }
                }
            }
        }
        
        return Status.OK_STATUS;
    }
    
}
