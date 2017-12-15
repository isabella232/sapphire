/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.osgi;

import java.net.URL;

import org.eclipse.sapphire.modeling.ImageData;
import org.osgi.framework.Bundle;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class BundleImageDataFactory
{
    private BundleImageDataFactory()
    {
        // This class is not meant to be instantiated.
    }
    
    public static ImageData readFromBundle( final String bundleSymbolicName,
                                            final String path )
    {
        final Bundle bundle = BundleLocator.find( bundleSymbolicName );
        
        if( bundle != null )
        {
            return readFromBundle( bundle, path );
        }
        
        return null;
    }
    
    public static ImageData readFromBundle( final Bundle bundle,
                                            final String path )
    {
        final URL url = bundle.getResource( path );
        
        if( url != null )
        {
            return ImageData.readFromUrl( url );
        }
        
        return null;
    }
    
    public static ImageData createFromBundle( final String bundleSymbolicName,
                                              final String path )
    {
        final Bundle bundle = BundleLocator.find( bundleSymbolicName );
        
        if( bundle == null )
        {
            throw new IllegalArgumentException();
        }

        return readFromBundle( bundle, path );
    }
    
    public static ImageData createFromBundle( final Bundle bundle,
                                              final String path )
    {
        final URL url = bundle.getResource( path );
        
        if( url == null )
        {
            throw new IllegalArgumentException();
        }
        
        return ImageData.createFromUrl( url );
    }
    
}
