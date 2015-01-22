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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.sapphire.ConversionService;
import org.eclipse.sapphire.modeling.ResourceStore;

/**
 * ConversionService implementation for ResourceStore to IContainer conversions via IFile.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ResourceStoreToIContainerConversionService extends ConversionService<ResourceStore,IContainer>
{
    public ResourceStoreToIContainerConversionService()
    {
        super( ResourceStore.class, IContainer.class );
    }

    @Override
    public IContainer convert( final ResourceStore store )
    {
        final IFile file = store.adapt( IFile.class );
        
        if( file != null )
        {
            return file.getParent();
        }
        
        return null;
    }

}
