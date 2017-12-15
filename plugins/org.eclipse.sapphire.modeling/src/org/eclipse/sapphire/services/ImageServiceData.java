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

package org.eclipse.sapphire.services;

import static org.eclipse.sapphire.modeling.util.MiscUtil.equal;

import org.eclipse.sapphire.modeling.ImageData;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ImageServiceData extends Data
{
    private final ImageData image;
    
    public ImageServiceData( final ImageData image )
    {
        this.image = image;
    }
    
    public ImageData image()
    {
        return this.image;
    }
    
    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof ImageServiceData )
        {
            final ImageServiceData data = (ImageServiceData) obj;
            return equal( this.image, data.image );
        }
        
        return false;
    }
    
    @Override
    public int hashCode()
    {
        return this.image.hashCode();
    }
    
}
