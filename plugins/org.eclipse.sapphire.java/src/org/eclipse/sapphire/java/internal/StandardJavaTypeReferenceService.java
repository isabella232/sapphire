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

package org.eclipse.sapphire.java.internal;

import org.eclipse.sapphire.java.ClassBasedJavaType;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.java.JavaTypeReferenceService;
import org.eclipse.sapphire.modeling.ClassLocator;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StandardJavaTypeReferenceService

    extends JavaTypeReferenceService
    
{
    private final ClassLocator classLocator;
    
    public StandardJavaTypeReferenceService( final ClassLocator classLocator )
    {
        this.classLocator = classLocator;
    }
    
    public StandardJavaTypeReferenceService( final ClassLoader classLoader )
    {
        this
        (
            new ClassLocator()
            {
                @Override
                public Class<?> find( final String name )
                {
                    try
                    {
                        return classLoader.loadClass( name );
                    }
                    catch( ClassNotFoundException e )
                    {
                        // Intentionally converting exception to null result.
                    }

                    return null;
                }
            }
        );
    }
    
    @Override
    public JavaType resolve( final String name )
    {
        final Class<?> cl = this.classLocator.find( name );
        
        if( cl != null )
        {
            return new ClassBasedJavaType( cl );
        }
        
        return null;
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            
            if( property != null && property.getTypeClass() == JavaTypeName.class )
            {
                final Reference referenceAnnotation = property.getAnnotation( Reference.class );
                
                if( referenceAnnotation != null && referenceAnnotation.target() == JavaType.class )
                {
                    return true;
                }
            }
            
            return false;
        }

        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            final IModelElement element = context.find( IModelElement.class );
            final ClassLocator classLocator = element.adapt( ClassLocator.class );
            
            if( classLocator != null )
            {
                return new StandardJavaTypeReferenceService( classLocator );
            }
            else
            {
                final ClassLoader classLoader = element.getModelElementType().getModelElementClass().getClassLoader();
                return new StandardJavaTypeReferenceService( classLoader );
            }
        }
    }

}
