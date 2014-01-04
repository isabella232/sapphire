/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.internal;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.ImageService;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * @author <a href="konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StaticImageService extends ImageService
{
    @Text( "Failed to load image {1} referenced by {0} class." )
    private static LocalizableText failedToLoadMessage;
    
    static
    {
        LocalizableText.init( StaticImageService.class );
    }

    private ImageData image;
    
    @Override
    protected void initImageService()
    {
        final ElementType type = context( Element.class ).type();
        final Image imageAnnotation = type.getAnnotation( Image.class );
        final Class<?> imageAnnotationHostClass = type.findAnnotationHostClass( imageAnnotation );
        final String imagePath = imageAnnotation.path();
        
        this.image = ImageData.readFromClassLoader( imageAnnotationHostClass, imagePath ).optional();
        
        if( this.image == null )
        {
            final String msg = failedToLoadMessage.format( imageAnnotationHostClass.getName(), imagePath );
            Sapphire.service( LoggingService.class ).logError( msg );
        }
    }

    @Override
    protected ImageData compute()
    {
        return this.image;
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            return ( context.find( Element.class ).type().getAnnotation( Image.class ) != null );
        }
    }
    
}
