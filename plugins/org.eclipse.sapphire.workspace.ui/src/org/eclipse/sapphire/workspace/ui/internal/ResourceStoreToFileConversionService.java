/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.workspace.ui.internal;

import java.io.File;
import java.net.URI;

import org.eclipse.sapphire.ConversionService;
import org.eclipse.sapphire.modeling.ResourceStore;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ide.FileStoreEditorInput;

/**
 * ConversionService implementation for ResourceStore to File conversions via FileStoreEditorInput. Used in the
 * case where an editor is open to a file outside of the workspace.
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
        final IEditorInput input = store.adapt( IEditorInput.class );
        
        if( input instanceof FileStoreEditorInput )
        {
            final URI uri = ( (FileStoreEditorInput) input ).getURI();
            return new File( uri );
        }
        
        return null;
    }

}
