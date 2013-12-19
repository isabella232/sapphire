/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.workspace.internal;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.sapphire.ConversionService;
import org.eclipse.sapphire.modeling.ResourceStore;

/**
 * ConversionService implementation for ResourceStore to File conversions via IFile.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ResourceStoreToFileConversionService extends ConversionService<ResourceStore,File>
{
    public ResourceStoreToFileConversionService()
    {
        super( ResourceStore.class, File.class );
    }

    @Override
    public File convert( final ResourceStore store )
    {
        final IFile ifile = store.adapt( IFile.class );
        
        if( ifile != null )
        {
            return ifile.getLocation().toFile();
        }
        
        return null;
    }

}
