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

package org.eclipse.sapphire.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.sapphire.Context;
import org.osgi.framework.Bundle;

/**
 * Implementation of Context based on an OSGi bundle.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class BundleBasedContext extends Context
{
    /**
     * Returns a context based on the specified OSGi bundle id.
     * 
     * @param bundleId the id of the bundle that should be used as the context basis
     * @return a context based on the specified bundle
     */
    
    public static final Context adapt( String bundleId )
    {
        if( bundleId == null )
        {
            throw new IllegalArgumentException();
        }
        
        return adapt( BundleLocator.find( bundleId ) );
    }

    /**
     * Returns a context based on the specified OSGi bundle.
     * 
     * @param bundle the bundle that should be used as the context basis
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
    public InputStream findResource( String name )
    {
        if( name == null )
        {
            throw new IllegalArgumentException();
        }
        
        final URL url = this.bundle.getResource( name );
        
        if( url != null )
        {
            try
            {
                return url.openStream();
            }
            catch( IOException e )
            {
                // Failure to open is equated with not found by returning null.
            }
        }
        
        return null;
    }
    
    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof BundleBasedContext )
        {
            final BundleBasedContext context = (BundleBasedContext) obj;
            return ( this.bundle == context.bundle );
        }
        
        return false;
    }

    @Override
    public int hashCode()
    {
        return this.bundle.hashCode();
    }
    
}