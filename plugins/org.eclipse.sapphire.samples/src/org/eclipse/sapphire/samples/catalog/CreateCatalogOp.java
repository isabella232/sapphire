/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.samples.catalog;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.PreferDefaultValue;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.workspace.CreateWorkspaceFileOp;
import org.eclipse.sapphire.workspace.WorkspaceFileType;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@WorkspaceFileType( Catalog.class )

public interface CreateCatalogOp extends CreateWorkspaceFileOp
{
    ElementType TYPE = new ElementType( CreateCatalogOp.class );
    
    // *** File ***
    
    @DefaultValue( text = "catalog.xml" )
    @PreferDefaultValue

    ValueProperty PROP_FILE = new ValueProperty( TYPE, CreateWorkspaceFileOp.PROP_FILE );
    
}
