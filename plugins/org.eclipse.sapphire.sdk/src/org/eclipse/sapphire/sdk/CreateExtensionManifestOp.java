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

package org.eclipse.sapphire.sdk;

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ProgressMonitor;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.sdk.extensibility.SapphireExtensionDef;
import org.eclipse.sapphire.sdk.internal.CreateExtensionManifestOpFileNameValidationService;
import org.eclipse.sapphire.sdk.internal.CreateExtensionManifestOpFolderValidationService;
import org.eclipse.sapphire.sdk.internal.CreateExtensionManifestOpMethods;
import org.eclipse.sapphire.workspace.CreateWorkspaceFileOp;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface CreateExtensionManifestOp extends CreateWorkspaceFileOp
{
    ModelElementType TYPE = new ModelElementType( CreateExtensionManifestOp.class );
    
    // *** Folder ***
    
    @Service( impl = CreateExtensionManifestOpFolderValidationService.class )
    
    ValueProperty PROP_FOLDER = new ValueProperty( TYPE, CreateWorkspaceFileOp.PROP_FOLDER );
    
    // *** FileName ***
    
    @DefaultValue( text = SapphireExtensionDef.FILE_NAME )
    @Service( impl = CreateExtensionManifestOpFileNameValidationService.class )

    ValueProperty PROP_FILE_NAME = new ValueProperty( TYPE, CreateWorkspaceFileOp.PROP_FILE_NAME );
    
    // *** Method: execute ***
    
    @DelegateImplementation( CreateExtensionManifestOpMethods.class )
    
    Status execute( ProgressMonitor monitor );
    
}
