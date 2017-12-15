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

package org.eclipse.sapphire.workspace;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.annotations.FileExtensions;
import org.eclipse.sapphire.platform.PathBridge;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class CreateWorkspaceFileOpMethods
{
	public static IFile getFileHandle( final CreateWorkspaceFileOp operation )
	{
        final Path folderPath = operation.getFolder().getContent();
        String fileName = operation.getFileName().getContent();
        
        if( folderPath == null || fileName == null )
        {
            return null;
        }
        
        fileName = fileName.trim();
        
        if( fileName.indexOf( '.' ) == -1 )
        {
            final ModelProperty property = CreateWorkspaceFileOp.PROP_FILE_NAME.refine( operation );
            final FileExtensions fileExtensionsAnnotation = property.getAnnotation( FileExtensions.class );
            
            if( fileExtensionsAnnotation != null )
            {
                final String extension = fileExtensionsAnnotation.expr();
                
                if( extension.length() > 0 )
                {
                    fileName = fileName + "." + extension;
                }
            }
        }
        
        final Path newFilePath = folderPath.append( fileName );
        
        return ResourcesPlugin.getWorkspace().getRoot().getFile( PathBridge.create( newFilePath ) );
	}
    
}
