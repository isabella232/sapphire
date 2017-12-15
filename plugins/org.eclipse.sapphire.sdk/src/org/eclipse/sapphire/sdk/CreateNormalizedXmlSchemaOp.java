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

package org.eclipse.sapphire.sdk;

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.ProgressMonitor;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.FileExtensions;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Listeners;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;
import org.eclipse.sapphire.sdk.internal.CreateNormalizedXmlSchemaOpMethods;
import org.eclipse.sapphire.sdk.internal.CreateNormalizedXmlSchemaOpServices.FolderInitialValueService;
import org.eclipse.sapphire.sdk.internal.CreateNormalizedXmlSchemaOpServices.SourceFileInitialValueService;
import org.eclipse.sapphire.sdk.internal.CreateNormalizedXmlSchemaOpServices.SourceFileListener;
import org.eclipse.sapphire.workspace.CreateWorkspaceFileOp;
import org.eclipse.sapphire.workspace.WorkspaceRelativePath;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface CreateNormalizedXmlSchemaOp extends CreateWorkspaceFileOp
{
    ModelElementType TYPE = new ModelElementType( CreateNormalizedXmlSchemaOp.class );
    
    // *** Folder ***
    
    @Service( impl = FolderInitialValueService.class )

    ValueProperty PROP_FOLDER = new ValueProperty( TYPE, CreateWorkspaceFileOp.PROP_FOLDER );
    
    // *** FileName ***

    @FileExtensions( expr = "xsd" )

    ValueProperty PROP_FILE_NAME = new ValueProperty( TYPE, CreateWorkspaceFileOp.PROP_FILE_NAME );
    
    // *** SourceFile ***
    
    @Type( base = Path.class )
    @Label( standard = "source file" )
    @ValidFileSystemResourceType( FileSystemResourceType.FILE )
    @FileExtensions( expr = "xsd" )
    @WorkspaceRelativePath
    @MustExist
    @Required
    @Service( impl = SourceFileInitialValueService.class )
    @Listeners( SourceFileListener.class )
    
    ValueProperty PROP_SOURCE_FILE = new ValueProperty( TYPE, "SourceFile" );
    
    Value<Path> getSourceFile();
    void setSourceFile( String value );
    void setSourceFile( Path value );
    
    // *** Method: execute ***
    
    @DelegateImplementation( CreateNormalizedXmlSchemaOpMethods.class )
    
    Status execute( ProgressMonitor monitor );

}
