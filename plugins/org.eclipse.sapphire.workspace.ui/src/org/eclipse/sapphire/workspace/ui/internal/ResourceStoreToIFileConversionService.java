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

package org.eclipse.sapphire.workspace.ui.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.sapphire.ConversionService;
import org.eclipse.sapphire.modeling.ResourceStore;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;

/**
 * ConversionService implementation for ResourceStore to IFile conversions via FileEditorInput.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ResourceStoreToIFileConversionService extends ConversionService<ResourceStore,IFile>
{
    public ResourceStoreToIFileConversionService()
    {
        super( ResourceStore.class, IFile.class );
    }

    @Override
    public IFile convert( final ResourceStore store )
    {
        final IEditorInput input = store.adapt( IEditorInput.class );
        
        if( input instanceof FileEditorInput )
        {
            return ( (FileEditorInput) input ).getFile();
        }
        
        return null;
    }

}
