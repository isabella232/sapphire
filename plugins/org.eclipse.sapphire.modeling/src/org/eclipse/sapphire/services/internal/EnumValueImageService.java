/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.services.internal;

import static org.eclipse.sapphire.modeling.ImageData.readFromClassLoader;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.sapphire.modeling.EnumValueType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;
import org.eclipse.sapphire.services.ValueImageService;
import org.eclipse.sapphire.services.ValueSerializationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class EnumValueImageService extends ValueImageService
{
    private EnumValueType enumType;
    private ValueSerializationService valueSerializationService;
    private final Map<Enum<?>,ImageData> images = new HashMap<Enum<?>,ImageData>();
    
    @Override
    protected void init()
    {
        super.init();
        
        final IModelElement element = context( IModelElement.class );
        final ValueProperty property = context( ValueProperty.class );
        
        this.enumType = new EnumValueType( property.getTypeClass() );
        this.valueSerializationService = element.service( property, ValueSerializationService.class );
    }
    
    @Override
    public ImageData provide( final String value )
    {
        final Enum<?> item = (Enum<?>) this.valueSerializationService.decode( value );
        
        if( item == null )
        {
            return null;
        }
        else
        {
            ImageData image = this.images.get( item );
            
            if( image == null )
            {
                final Image imageAnnotation = this.enumType.getAnnotation( item, Image.class );
                
                if( imageAnnotation != null )
                {
                    image = readFromClassLoader( this.enumType.getEnumTypeClass(), imageAnnotation.path() );
                    
                    if( image != null )
                    {
                        this.images.put( item, image );
                    }
                }
            }
            
            return image;
        }
    }

    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            return ( property != null && Enum.class.isAssignableFrom( property.getTypeClass() ) );
        }

        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new EnumValueImageService();
        }
    }
    
}
