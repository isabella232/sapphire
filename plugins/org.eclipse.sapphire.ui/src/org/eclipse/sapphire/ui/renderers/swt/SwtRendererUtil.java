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

package org.eclipse.sapphire.ui.renderers.swt;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SwtRendererUtil
{
    public static ImageDescriptor toImageDescriptor( final org.eclipse.sapphire.modeling.ImageData data )
    {
        if( data != null )
        {
            final ImageData swtImageData = new ImageData( data.contents() );
            return ImageDescriptor.createFromImageData( swtImageData );
        }
        
        return null;
    }
    
    public static ImageData toImageData( final org.eclipse.sapphire.modeling.ImageData data )
    {
        if( data != null )
        {
            return new ImageData( data.contents() );
        }
        
        return null;
    }

    public static ImageData createImageData( final ClassLoader cl,
                                             final String path )
    {
        return toImageData( org.eclipse.sapphire.modeling.ImageData.createFromClassLoader( cl, path ) );
    }

    public static ImageData createImageData( final Class<?> cl,
                                             final String path )
    {
        return toImageData( org.eclipse.sapphire.modeling.ImageData.createFromClassLoader( cl, path ) );
    }

    public static ImageDescriptor createImageDescriptor( final ClassLoader cl,
                                                         final String path )
    {
        return toImageDescriptor( org.eclipse.sapphire.modeling.ImageData.createFromClassLoader( cl, path ) );
    }

    public static ImageDescriptor createImageDescriptor( final Class<?> cl,
                                                         final String path )
    {
        return toImageDescriptor( org.eclipse.sapphire.modeling.ImageData.createFromClassLoader( cl, path ) );
    }

}
