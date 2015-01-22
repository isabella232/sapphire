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

package org.eclipse.sapphire.services.internal;

import static org.eclipse.sapphire.ImageData.readFromClassLoader;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.MasterConversionService;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.EnumValueType;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ValueImageService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class EnumValueImageService extends ValueImageService
{
    private EnumValueType enumType;
    private final Map<Enum<?>,ImageData> images = new HashMap<Enum<?>,ImageData>();
    
    @Override
    protected void init()
    {
        super.init();
        
        this.enumType = new EnumValueType( context( ValueProperty.class ).getTypeClass() );
    }
    
    @Override
    public ImageData provide( final String value )
    {
        final MasterConversionService masterConversionService = context( ValueProperty.class ).service( MasterConversionService.class );
        final Enum<?> item = masterConversionService.convert( value, this.enumType.getEnumTypeClass() );
        
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
                    image = readFromClassLoader( this.enumType.getEnumTypeClass(), imageAnnotation.path() ).optional();
                    
                    if( image != null )
                    {
                        this.images.put( item, image );
                    }
                }
            }
            
            return image;
        }
    }

    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            return ( property != null && Enum.class.isAssignableFrom( property.getTypeClass() ) );
        }
    }
    
}
