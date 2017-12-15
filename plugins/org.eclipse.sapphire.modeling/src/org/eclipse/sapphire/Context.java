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

package org.eclipse.sapphire;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.util.ListFactory;

/**
 * An abstraction for finding classes and other resources typically loaded from a class loader.
 * This abstraction allows integration with systems that do not provide easy access to a class loader.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
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
    
    public abstract URL findResource( String name );
    
    /**
     * Finds all resources with specified name.
     * 
     * @param name the name of the resource
     * @return a list of all resources with specified name or empty list if not found
     * @throws IllegalArgumentException if resource name is null
     */
    
    public abstract List<URL> findResources( String name );
    
    /**
     * Implementation of Context based on a class loader.
     */
    
    private static class ClassLoaderContext extends Context
    {
        private final ClassLoader loader;
        
        public ClassLoaderContext( final ClassLoader loader )
        {
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
        public URL findResource( final String name )
        {
            if( name == null )
            {
                throw new IllegalArgumentException();
            }
            
            return this.loader.getResource( name );
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
                final Enumeration<URL> enumeration = this.loader.getResources( name );
                
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
        public URL findResource( final String name )
        {
            URL resource = super.findResource( name );
            
            if( resource == null && name.indexOf( '/' ) == -1 && this.path != null )
            {
                resource = super.findResource( this.path + "/" + name );
            }
            
            return resource;
        }

        @Override
        public List<URL> findResources( final String name )
        {
            List<URL> resources = super.findResources( name );
            
            if( resources.isEmpty() && name.indexOf( '/' ) == -1 && this.path != null )
            {
                resources = super.findResources( this.path + "/" + name );
            }
            
            return resources;
        }
    }
    
}
