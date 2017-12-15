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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.sapphire.FileName;
import org.eclipse.sapphire.modeling.IExecutableModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.ProgressMonitor;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Transient;
import org.eclipse.sapphire.modeling.TransientProperty;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Services;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;
import org.eclipse.sapphire.workspace.internal.CreateWorkspaceFileOpServices.FileNameValidationService;
import org.eclipse.sapphire.workspace.internal.CreateWorkspaceFileOpServices.FolderInitialValueService;
import org.eclipse.sapphire.workspace.internal.CreateWorkspaceFileOpServices.FolderValidationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public interface CreateWorkspaceFileOp extends IExecutableModelElement
{
    ModelElementType TYPE = new ModelElementType( CreateWorkspaceFileOp.class );
    
    static final String PROBLEM_FILE_EXISTS = "Sapphire.Workspace.CreateFileOp.FileExists";
    
    // *** Context ***
    
    @Type( base = IResource.class )

    TransientProperty PROP_CONTEXT = new TransientProperty( TYPE, "Context" );
    
    Transient<IResource> getContext();
    void setContext( IResource value );
    
    // *** Folder ***
    
    @Type( base = Path.class )
    @Label( standard = "fol&der" )
    @Required
    @WorkspaceRelativePath
    @ValidFileSystemResourceType( FileSystemResourceType.FOLDER )
    @Services( { @Service( impl = FolderValidationService.class ), @Service( impl = FolderInitialValueService.class ) } )
    
    ValueProperty PROP_FOLDER = new ValueProperty( TYPE, "Folder" );
    
    Value<Path> getFolder();
    void setFolder( String value );
    void setFolder( Path value );
    
    // *** FileName ***

    @Type( base = FileName.class )
    @Label( standard = "file na&me" )
    @Required
    @Service( impl = FileNameValidationService.class )
    
    ValueProperty PROP_FILE_NAME = new ValueProperty( TYPE, "FileName" );
    
    Value<FileName> getFileName();
    void setFileName( String value );
    void setFileName( FileName value );
    
    // *** Method: getFileHandle ***
    
    @DelegateImplementation( CreateWorkspaceFileOpMethods.class )
    
    IFile getFileHandle();
    
    // *** OverwriteExistingFile ***

    @Type( base = Boolean.class )
    @Label( standard = "overwrite e&xisting file" )
    @DefaultValue( text = "false" )
    
    ValueProperty PROP_OVERWRITE_EXISTING_FILE = new ValueProperty( TYPE, "OverwriteExistingFile" );
    
    Value<Boolean> getOverwriteExistingFile();
    void setOverwriteExistingFile( String value );
    void setOverwriteExistingFile( Boolean value );
    
    // *** Method: execute ***
    
    @DelegateImplementation( CreateWorkspaceFileOpMethods.class )
    
    Status execute( ProgressMonitor monitor );

}
