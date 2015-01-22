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
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.workspace.WorkspaceFileResourceStore;

/**
 * ConversionService implementation for IFile to WorkspaceFileResourceStore conversions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class IFileToWorkspaceFileResourceStoreConversionService extends ConversionService<IFile,WorkspaceFileResourceStore>
{
    public IFileToWorkspaceFileResourceStoreConversionService()
    {
        super( IFile.class, WorkspaceFileResourceStore.class );
    }

    @Override
    public WorkspaceFileResourceStore convert( final IFile file )
    {
        try
        {
            return new WorkspaceFileResourceStore( file );
        }
        catch( ResourceStoreException e )
        {
            Sapphire.service( LoggingService.class ).log( e );
        }
        
        return null;
    }

}
