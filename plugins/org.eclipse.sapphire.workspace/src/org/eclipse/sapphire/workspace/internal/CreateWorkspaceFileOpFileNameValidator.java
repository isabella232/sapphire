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

package org.eclipse.sapphire.workspace.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.FileExtensions;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.PathValidationService;
import org.eclipse.sapphire.services.ValidationService;
import org.eclipse.sapphire.workspace.CreateWorkspaceFileOp;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class CreateWorkspaceFileOpFileNameValidator extends ValidationService
{
    @Override
    public Status validate()
    {
        final Value<String> value = context( IModelElement.class ).read( context( ValueProperty.class ) );
        final String fileName = value.getText();
        
        if( fileName != null )
        {
            final int lastDot = fileName.indexOf( '.' );
            
            if( lastDot >= 0 && lastDot < fileName.length() )
            {
                final FileExtensions fileExtensionsAnnotation = value.getProperty().getAnnotation( FileExtensions.class );
                final List<String> extensions = new ArrayList<String>();
                
                for( String extension : fileExtensionsAnnotation.expr().split( "," ) )
                {
                    extensions.add( extension );
                }
                
                final Status st = PathValidationService.validateExtensions( fileName, extensions );
                
                if( ! st.ok() )
                {
                    return st;
                }
            }
            
            final CreateWorkspaceFileOp operation = value.nearest( CreateWorkspaceFileOp.class );
            final IFile fileHandle = operation.getFileHandle();
            
            if( fileHandle != null && fileHandle.exists() && 
                operation.getOverwriteExistingFile().getContent() == false )
            {
                final String msg = NLS.bind( Resources.fileExists, fileName );
                return Status.createErrorStatus( msg );
            }
        }
        
        return Status.createOkStatus();
    }
    
    private static final class Resources extends NLS
    {
        public static String fileExists;
        
        static
        {
            initializeMessages( CreateWorkspaceFileOpFileNameValidator.class.getName(), Resources.class );
        }
    }
    
}
