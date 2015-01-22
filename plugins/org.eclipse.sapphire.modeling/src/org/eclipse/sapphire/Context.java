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

package org.eclipse.sapphire;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * An abstraction for finding classes and other resources typically loaded from a class loader.
 * This abstraction allows integration with systems that do not provide easy access to a class loader.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class Context
{
    /**
     * Returns a context based on the specified class loader.
     * 
     * @param loader the class loader that should be used as the context basis
     * @return a context based on the specified class loader
     */
    
    public static final Context adapt( final ClassLoader loader )
    {
        if( loader == null )
        {
            throw new IllegalArgumentException();
        }
        
        return new ClassLoaderContext( loader );
    }
    
    /**
     * Returns a context based on the specified class. Such a context behaves similarly to
     * a context built around a class loader. The difference is ability to find classes and
     * resources based on simple (unqualified) names as long as they are located in the package
     * of the specified class. The package-level search is performed only if standard class loader
     * search fails to find a class or a resource.
     * 
     * @param cl the class that should be used as the context basis
     * @return a context based on the specified class
     */
    
    public static final Context adapt( final Class<?> cl )
    {
        if( cl == null )
        {
            throw new IllegalArgumentException();
        }
        
        return new ClassContext( cl );
    }
    
    /**
     * Finds a class with specified name.
     * 
     * @param name the name of the class
     * @return a class with specified name or null if not found
     * @throws IllegalArgumentException if class name is null
     */
    
    public abstract <T> Class<T> findClass( String name );
    
    /**
     * Finds a resource with specified name. 
     * 
     * @param name the name of the resource
     * @return a resource with specified name or null if not found
     * @throws IllegalArgumentException if resource name is null
     */
    
    public abstract InputStream findResource( String name );
    
    /**
     * Implementation of Context based on a class loader.
     */
    
    private static class ClassLoaderContext extends Context
    {
        private final ClassLoader loader;
        
        public ClassLoaderContext( final ClassLoader loader )
        {
            if( loader == null )
            {
                throw new IllegalArgumentException();
            }
            
            this.loader = loader;
        }
        
        @Override
        @SuppressWarnings( "unchecked" )
        
        public <T> Class<T> findClass( final String name )
        {
            if( name == null )
            {
                throw new IllegalArgumentException();
            }
            
            try
            {
                return (Class<T>) this.loader.loadClass( name );
            }
            catch( ClassNotFoundException e )
            {
                // Intentionally converting ClassNotFoundException to null return.
            }
            
            return null;
        }

        @Override
        public InputStream findResource( final String name )
        {
            if( name == null )
            {
                throw new IllegalArgumentException();
            }
            
            final URL url = this.loader.getResource( name );
            
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
            if( obj instanceof ClassLoaderContext )
            {
                final ClassLoaderContext context = (ClassLoaderContext) obj;
                return ( this.loader == context.loader );
            }
            
            return false;
        }

        @Override
        public int hashCode()
        {
            return this.loader.hashCode();
        }
    }
    
    /**
     * Implementation of Context based on a class.
     */
    
    private static final class ClassContext extends ClassLoaderContext
    {
        private final String pkg;
        private final String path;
        
        public ClassContext( final Class<?> cl )
        {
            super( cl.getClassLoader() );
            
            final Package pkg = cl.getPackage();
            
            if( pkg == null )
            {
                this.pkg = null;
                this.path = null;
            }
            else
            {
                this.pkg = pkg.getName();
                this.path = this.pkg.replace( '.', '/' );
            }
        }

        @Override
        public <T> Class<T> findClass( final String name )
        {
            Class<T> cl = super.findClass( name );
            
            if( cl == null && name.indexOf( '.' ) == -1 && this.pkg != null)
            {
                cl = super.findClass( this.pkg + "." + name );
            }
            
            return cl;
        }

        @Override
        public InputStream findResource( final String name )
        {
            InputStream stream = super.findResource( name );
            
            if( stream == null && name.indexOf( '/' ) == -1 && this.path != null )
            {
                stream = super.findResource( this.path + "/" + name );
            }
            
            return stream;
        }
        
        @Override
        public boolean equals( final Object obj )
        {
            if( obj instanceof ClassContext )
            {
                final ClassContext context = (ClassContext) obj;
                return super.equals( context ) && this.pkg.equals( context.pkg );
            }
            
            return false;
        }

        @Override
        public int hashCode()
        {
            return super.hashCode() ^ this.pkg.hashCode();
        }
    }
    
}
