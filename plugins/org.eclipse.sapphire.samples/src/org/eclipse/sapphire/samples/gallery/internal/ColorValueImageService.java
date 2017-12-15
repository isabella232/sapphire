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

package org.eclipse.sapphire.samples.gallery.internal;

import static org.eclipse.sapphire.modeling.ImageData.readFromClassLoader;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.services.ValueImageService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ColorValueImageService extends ValueImageService
{
    private final Map<String,ImageData> images = new HashMap<String,ImageData>();

    @Override
    public ImageData provide( final String value )
    {
        ImageData image = this.images.get( value );
        
        if( image == null )
        {
            String imageResourceName = null;
            
            if( value != null )
            {
                if( value.equals( "red" ) )
                {
                    imageResourceName = "SquareRed.png";
                }
                else if( value.equals( "orange" ) )
                {
                    imageResourceName = "SquareOrange.png";
                }
                else if( value.equals( "yellow" ) )
                {
                    imageResourceName = "SquareYellow.png";
                }
                else if( value.equals( "green" ) )
                {
                    imageResourceName = "SquareGreen.png";
                }
                else if( value.equals( "blue" ) )
                {
                    imageResourceName = "SquareBlue.png";
                }
                else if( value.equals( "violet" ) )
                {
                    imageResourceName = "SquareViolet.png";
                }
            }
            
            if( imageResourceName != null )
            {
                final String imageResourcePath = "org/eclipse/sapphire/samples/" + imageResourceName;
                image = readFromClassLoader( ColorValueImageService.class, imageResourcePath );
            }
        }
        
        return image;
    }
    
}
