/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.sapphire.Context;
import org.eclipse.sapphire.util.ListFactory;
import org.osgi.framework.Bundle;

/**
 * Implementation of Context based on an OSGi bundle.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class BundleBasedContext extends Context
{
    /**
     * Returns a context based on the specified OSGi bundle.
     * 
     * @param loader the bundle that should be used as the context basis
     * @return a context based on the specified bundle
     */
    
    public static final Context adapt( final Bundle bundle )
    {
        if( bundle == null )
        {
            throw new IllegalArgumentException();
        }
        
        return new BundleBasedContext( bundle );
    }
    
    private final Bundle bundle;
    
    private BundleBasedContext( final Bundle bundle )
    {
        this.bundle = bundle;
    }
    
    @Override
    @SuppressWarnings( "unchecked" )
    
    public <T> Class<T> findClass( String name )
    {
        if( name == null )
        {
            throw new IllegalArgumentException();
        }
        
        try
        {
            return (Class<T>) this.bundle.loadClass( name );
        }
        catch( ClassNotFoundException e )
        {
            // Intentionally converting ClassNotFoundException to null return.
        }

        return null;
    }

    @Override
    public URL findResource( String name )
    {
        if( name == null )
        {
            throw new IllegalArgumentException();
        }
        
        return this.bundle.getResource( name );
    }

    @Override
    public List<URL> findResources( final String name )
    {
        if( name == null )
        {
            throw new IllegalArgumentException();
        }
        
        final ListFactory<URL> resourcesListFactory = ListFactory.start();

        try
        {
            final Enumeration<URL> enumeration = this.bundle.getResources( name );
            
            while( enumeration.hasMoreElements() )
            {
                final URL resource = enumeration.nextElement();
                
                if( resource != null )
                {
                    resourcesListFactory.add( resource );
                }
            }
        }
        catch( IOException e )
        {
            LoggingService.log( e );
        }
        
        return resourcesListFactory.result();
    }
    
}