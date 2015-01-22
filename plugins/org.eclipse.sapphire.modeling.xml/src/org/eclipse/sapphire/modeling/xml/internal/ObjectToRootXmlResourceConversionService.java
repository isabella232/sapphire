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

package org.eclipse.sapphire.modeling.xml.internal;

import java.io.File;

import org.eclipse.sapphire.ConversionService;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.MasterConversionService;
import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlRootBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * ConversionService implementation for Object to RootXmlResource conversions. Conversion is only performed if the 
 * object can be converted to ByteArrayResourceStore and if the resource store corresponds to a file with "xml" 
 * extension or if the context element type has XML binding annotations.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ObjectToRootXmlResourceConversionService extends ConversionService<Object,RootXmlResource>
{
    public ObjectToRootXmlResourceConversionService()
    {
        super( Object.class, RootXmlResource.class );
    }

    @Override
    public RootXmlResource convert( final Object object )
    {
        final ByteArrayResourceStore store = service( MasterConversionService.class ).convert( object, ByteArrayResourceStore.class );
        
        if( compatible( store ) )
        {
            return new RootXmlResource( new XmlResourceStore( store ) );
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
            
            final ElementType type = context( ElementType.class );
            
            if( type != null && ( type.hasAnnotation( XmlBinding.class ) || type.hasAnnotation( CustomXmlRootBinding.class ) ) )
            {
                return true;
            }
        }
        
        return false;
    }
    
}
