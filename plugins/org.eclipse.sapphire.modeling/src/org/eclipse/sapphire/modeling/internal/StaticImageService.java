/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.internal;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.modeling.ImageService;
import org.eclipse.sapphire.modeling.ModelElementService;
import org.eclipse.sapphire.modeling.ModelElementServiceFactory;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.Image;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StaticImageService

    extends ImageService
    
{
    private ImageData image;
    
    @Override
    public void init( final IModelElement element,
                      final String[] params )
    {
        super.init( element, params );
        
        final ModelElementType type = element.getModelElementType();
        final Image imageAnnotation = type.getAnnotation( Image.class );
        this.image = ImageData.createFromClassLoader( type.getAnnotationHostClass( imageAnnotation ), imageAnnotation.path() );
    }

    @Override
    public ImageData provide()
    {
        return this.image;
    }
    
    public static final class Factory extends ModelElementServiceFactory
    {
        @Override
        public boolean applicable( final IModelElement element,
                                   final Class<? extends ModelElementService> service )
        {
            return ( element.getModelElementType().getAnnotation( Image.class ) != null );
        }

        @Override
        public ModelElementService create( final IModelElement element,
                                           final Class<? extends ModelElementService> service )
        {
            return new StaticImageService();
        }
    }
    
}
