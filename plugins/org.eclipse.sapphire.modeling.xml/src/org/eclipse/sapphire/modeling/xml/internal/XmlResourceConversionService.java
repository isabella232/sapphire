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

package org.eclipse.sapphire.modeling.xml.internal;

import java.io.File;

import org.eclipse.sapphire.ConversionService;
import org.eclipse.sapphire.MasterConversionService;
import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Resource;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlRootBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * Implementation of ConversionService that is capable of converting a ByteArrayResourceStore to an XmlResource
 * or a Resource. Conversion is only performed if resource store corresponds to a file with "xml" extension or
 * if the context element type has XML binding annotations.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class XmlResourceConversionService extends ConversionService
{
    @Override
    public <T> T convert( final Object object, final Class<T> type )
    {
        if( type == XmlResource.class || type == Resource.class )
        {
            final ByteArrayResourceStore store = service( MasterConversionService.class ).convert( object, ByteArrayResourceStore.class );
            
            if( compatible( store ) )
            {
                return type.cast( new RootXmlResource( new XmlResourceStore( store ) ) );
            }
        }
        
        return null;
    }
    
    private boolean compatible( final ByteArrayResourceStore store )
    {
        if( store != null )
        {
            final File file = store.adapt( File.class );
            
            if( file != null && file.getName().toLowerCase().endsWith( ".xml" ) )
            {
                return true;
            }
            
            final ModelElementType type = context( ModelElementType.class );
            
            if( type != null && ( type.hasAnnotation( XmlBinding.class ) || type.hasAnnotation( CustomXmlRootBinding.class ) ) )
            {
                return true;
            }
        }
        
        return false;
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
            return new XmlResourceConversionService();
        }
    }
    
}
