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

package org.eclipse.sapphire.internal;

import org.eclipse.sapphire.ElementType;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ElementClassLoader extends ClassLoader
{
    public ElementClassLoader( final ClassLoader parent )
    {
        super( parent );
    }
    
    public synchronized Class<?> loadImplementationClass( final ElementType type )
    {
        final Class<?> typeInterfaceClass = type.getModelElementClass();
        final String typeImplClassName = typeInterfaceClass.getName() + "$Impl";
        Class<?> typeImplClass = findLoadedClass( typeImplClassName );
        
        if( typeImplClass == null )
        {
            final byte[] typeImplClassBytes = ( new ElementCompiler( type ) ).compile();
            typeImplClass = defineClass( typeImplClassName, typeImplClassBytes, 0, typeImplClassBytes.length );
            resolveClass( typeImplClass );
        }
        
        return typeImplClass;
    }
    
}
