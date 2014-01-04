/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.workspace;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ExecutableElement;
import org.eclipse.sapphire.FileName;
import org.eclipse.sapphire.ReferenceValue;
import org.eclipse.sapphire.Transient;
import org.eclipse.sapphire.TransientProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.ProgressMonitor;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Services;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;
import org.eclipse.sapphire.workspace.internal.CreateWorkspaceFileOpServices.FileReferenceService;
import org.eclipse.sapphire.workspace.internal.CreateWorkspaceFileOpServices.FileValidationService;
import org.eclipse.sapphire.workspace.internal.CreateWorkspaceFileOpServices.FolderInitialValueService;
import org.eclipse.sapphire.workspace.internal.CreateWorkspaceFileOpServices.FolderReferenceService;
import org.eclipse.sapphire.workspace.internal.CreateWorkspaceFileOpServices.FolderValidationService;
import org.eclipse.sapphire.workspace.internal.CreateWorkspaceFileOpServices.RootReferenceService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface CreateWorkspaceFileOp extends ExecutableElement
{
    ElementType TYPE = new ElementType( CreateWorkspaceFileOp.class );
    
    static final String PROBLEM_FILE_EXISTS = "Sapphire.Workspace.CreateFileOp.FileExists";
    
    // *** Context ***
    
    @Type( base = IResource.class )

    TransientProperty PROP_CONTEXT = new TransientProperty( TYPE, "Context" );
    
    Transient<IResource> getContext();
    void setContext( IResource value );
    
    // *** Root ***
    
    @Type( base = Path.class )
    @Reference( target = IContainer.class )
    @Label( standard = "root" )
    @WorkspaceRelativePath
    @ValidFileSystemResourceType( FileSystemResourceType.FOLDER )
    @Service( impl = RootReferenceService.class )
    
    ValueProperty PROP_ROOT = new ValueProperty( TYPE, "Root" );
    
    ReferenceValue<Path,IContainer> getRoot();
    void setRoot( String value );
    void setRoot( Path value );
    
    // *** Folder ***
    
    @Type( base = Path.class )
    @Reference( target = IContainer.class )
    @Label( standard = "fol&der" )
    @ValidFileSystemResourceType( FileSystemResourceType.FOLDER )
    
    @Services
    (
        {
            @Service( impl = FolderReferenceService.class ),
            @Service( impl = FolderValidationService.class ),
            @Service( impl = FolderInitialValueService.class )
        }
    )
    
    ValueProperty PROP_FOLDER = new ValueProperty( TYPE, "Folder" );
    
    ReferenceValue<Path,IContainer> getFolder();
    void setFolder( String value );
    void setFolder( Path value );
    
    // *** File ***

    @Type( base = FileName.class )
    @Reference( target = IFile.class )
    @Label( standard = "file na&me" )
    @Required
    
    @Services
    (
        {
            @Service( impl = FileReferenceService.class ),
            @Service( impl = FileValidationService.class )
        }
    )
    
    ValueProperty PROP_FILE = new ValueProperty( TYPE, "File" );
    
    ReferenceValue<FileName,IFile> getFile();
    void setFile( String value );
    void setFile( FileName value );
    
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
