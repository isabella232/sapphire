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

package org.eclipse.sapphire.samples.po;

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.FileExtensions;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.workspace.CreateWorkspaceFileOp;
import org.eclipse.sapphire.workspace.WorkspaceFileType;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@WorkspaceFileType( PurchaseOrder.class )
@GenerateImpl

public interface CreatePurchaseOrderOp extends CreateWorkspaceFileOp
{
    ModelElementType TYPE = new ModelElementType( CreatePurchaseOrderOp.class );
    
    // *** FileName ***
    
    @DefaultValue( text = "sample.po" )
    @FileExtensions( expr = "po" )

    ValueProperty PROP_FILE_NAME = new ValueProperty( TYPE, CreateWorkspaceFileOp.PROP_FILE_NAME );
    
}
