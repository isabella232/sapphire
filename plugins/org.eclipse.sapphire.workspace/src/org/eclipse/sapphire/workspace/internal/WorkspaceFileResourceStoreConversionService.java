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

import org.eclipse.core.resources.IFile;
import org.eclipse.sapphire.ConversionService;
import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;
import org.eclipse.sapphire.workspace.WorkspaceFileResourceStore;

/**
 * Implementation of ConversionService that is capable of converting an IFile to a WorkspaceFileResourceStore
 * or a ByteArrayResourceStore.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class WorkspaceFileResourceStoreConversionService extends ConversionService
{
    @Override
    public <T> T convert( final Object object, final Class<T> type )
    {
        if( object instanceof IFile && ( type == WorkspaceFileResourceStore.class || type == ByteArrayResourceStore.class ) )
        {
            try
            {
                return type.cast( new WorkspaceFileResourceStore( (IFile) object ) );
            }
            catch( ResourceStoreException e )
            {
                LoggingService.log( e );
            }
        }
        
        return null;
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            return true;
        }
    
        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new WorkspaceFileResourceStoreConversionService();
        }
    }
    
}
