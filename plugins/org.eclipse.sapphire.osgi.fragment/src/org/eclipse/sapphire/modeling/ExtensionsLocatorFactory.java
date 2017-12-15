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

package org.eclipse.sapphire.modeling;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.sapphire.modeling.ExtensionsLocator.Handle;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * Locates Sapphire extensions in an OSGi system by scanning all bundles.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ExtensionsLocatorFactory extends ExtensionsLocator.Factory
{
    @Override
    public boolean applicable()
    {
        return ( FrameworkUtil.getBundle( ExtensionsLocatorFactory.class ) != null );
    }

    @Override
    public ExtensionsLocator create()
    {
        return new ExtensionsLocator()
        {
            @Override
            public List<Handle> find()
            {
                final BundleContext context = FrameworkUtil.getBundle( ExtensionsLocatorFactory.class ).getBundleContext();
                final List<Handle> handles = new ArrayList<Handle>();
                
                for( final Bundle bundle : context.getBundles() )
                {
                    final int state = bundle.getState();
                    
                    if( state == Bundle.RESOLVED || state == Bundle.STARTING || state == Bundle.ACTIVE )
                    {
                        Enumeration<URL> urls = null;
                        
                        try
                        {
                            urls = bundle.getResources( DEFAULT_PATH );
                        }
                        catch( Exception e )
                        {
                            LoggingService.log( e );
                        }
                        
                        if( urls != null )
                        {
                            while( urls.hasMoreElements() )
                            {
                                final URL url = urls.nextElement();
                                
                                if( ! contains( handles, url ) )
                                {
                                    final Handle handle = new Handle()
                                    {
                                        @Override
                                        public URL extension()
                                        {
                                            return url;
                                        }
    
                                        @Override
                                        public URL findResource( final String name )
                                        {
                                            return bundle.getResource( name );
                                        }
    
                                        @Override
                                        @SuppressWarnings( "unchecked" )
                                        
                                        public <T> Class<T> findClass( final String name )
                                        {
                                            try
                                            {
                                                return (Class<T>) bundle.loadClass( name );
                                            }
                                            catch( ClassNotFoundException e )
                                            {
                                                // Intentionally converting ClassNotFoundException to null return.
                                            }
    
                                            return null;
                                        }
                                    };
                                    
                                    handles.add( handle );
                                }
                            }
                        }
                    }
                }
                
                return Collections.unmodifiableList( handles );
            }
        };
    }
    
    private static boolean contains( final List<Handle> handles,
                                     final URL url )
    {
        for( Handle handle : handles )
        {
            if( url.toString().equals( handle.extension().toString() ) )
            {
                return true;
            }
        }
        
        return false;
    }

}
