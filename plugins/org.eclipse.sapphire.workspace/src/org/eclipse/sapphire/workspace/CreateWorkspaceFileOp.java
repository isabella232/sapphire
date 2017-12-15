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
import org.eclipse.sapphire.modeling.IExecutableModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.DependsOn;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;
import org.eclipse.sapphire.workspace.internal.CreateWorkspaceFileOpFileNameValidator;
import org.eclipse.sapphire.workspace.internal.CreateWorkspaceFileOpFolderValidator;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public interface CreateWorkspaceFileOp extends IExecutableModelElement
{
    ModelElementType TYPE = new ModelElementType( CreateWorkspaceFileOp.class );
    
    // *** Folder ***
    
    @Type( base = Path.class )
    @Label( standard = "fol&der" )
    @Required
    @WorkspaceRelativePath
    @ValidFileSystemResourceType( FileSystemResourceType.FOLDER )
    @Service( impl = CreateWorkspaceFileOpFolderValidator.class )
    
    ValueProperty PROP_FOLDER = new ValueProperty( TYPE, "Folder" );
    
    Value<Path> getFolder();
    void setFolder( String folder );
    void setFolder( Path folder );
    
    // *** FileName ***

    @Label( standard = "file na&me" )
    @Required
    @DependsOn( { "Folder", "OverwriteExistingFile" } )
    @Service( impl = CreateWorkspaceFileOpFileNameValidator.class )
    
    ValueProperty PROP_FILE_NAME = new ValueProperty( TYPE, "FileName" );
    
    Value<String> getFileName();
    void setFileName( String fileName );
    
    // *** Method: getFileHandle ***
    
    @DelegateImplementation( CreateWorkspaceFileOpMethods.class )
    
    IFile getFileHandle();
    
    // *** OverwriteExistingFile ***

    @Type( base = Boolean.class )
    @Label( standard = "overwrite an e&xisting file" )
    @DefaultValue( text = "false" )
    
    ValueProperty PROP_OVERWRITE_EXISTING_FILE = new ValueProperty( TYPE, "OverwriteExistingFile" );
    
    Value<Boolean> getOverwriteExistingFile();
    void setOverwriteExistingFile( String overwriteExistingFile );
    void setOverwriteExistingFile( Boolean overwriteExistingFile );

}
