/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.internal;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.sapphire.ElementType;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ElementClassLoaders
{
    private static final Map<ClassLoader,ElementClassLoader> loaders = new WeakHashMap<ClassLoader,ElementClassLoader>();
    
    public static Class<?> loadImplementationClass( final ElementType type )
    {
        final ClassLoader typeInterfaceClassLoader = type.getModelElementClass().getClassLoader();
        
        ElementClassLoader loader;
        
        synchronized( ElementClassLoaders.class )
        {
            loader = loaders.get( typeInterfaceClassLoader );
            
            if( loader == null )
            {
                loader = AccessController.doPrivileged
                (
                    new PrivilegedAction<ElementClassLoader>()
                    {
                        @Override
                        public ElementClassLoader run()
                        {
                            return new ElementClassLoader( typeInterfaceClassLoader );
                        }
                    }
                );
                
                loaders.put( typeInterfaceClassLoader, loader );
            }
        }
        
        return loader.loadImplementationClass( type );
    }
    
    private ElementClassLoaders() {}
    
}
