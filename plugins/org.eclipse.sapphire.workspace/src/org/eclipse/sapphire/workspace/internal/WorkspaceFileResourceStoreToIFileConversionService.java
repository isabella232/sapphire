/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.workspace.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.sapphire.ConversionService;
import org.eclipse.sapphire.workspace.WorkspaceFileResourceStore;

/**
 * ConversionService implementation for WorkspaceFileResourceStore to IFile conversions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class WorkspaceFileResourceStoreToIFileConversionService extends ConversionService<WorkspaceFileResourceStore,IFile>
{
    public WorkspaceFileResourceStoreToIFileConversionService()
    {
        super( WorkspaceFileResourceStore.class, IFile.class );
    }

    @Override
    public IFile convert( final WorkspaceFileResourceStore store )
    {
        return store.getWorkspaceFile();
    }

}
