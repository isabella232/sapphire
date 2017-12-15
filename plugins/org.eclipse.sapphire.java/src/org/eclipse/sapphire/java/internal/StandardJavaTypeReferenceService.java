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

package org.eclipse.sapphire.java.internal;

import org.eclipse.sapphire.Context;
import org.eclipse.sapphire.java.ClassBasedJavaType;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.java.JavaTypeReferenceService;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StandardJavaTypeReferenceService extends JavaTypeReferenceService
{
    private final Context context;
    
    public StandardJavaTypeReferenceService( final Context context )
    {
        if( context == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.context = context;
    }
    
    public StandardJavaTypeReferenceService( final ClassLoader loader )
    {
        this( Context.adapt( loader ) );
    }
    
    @Override
    public JavaType resolve( final String name )
    {
        final Class<?> cl = this.context.findClass( name );
        
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
            
            Context ctxt = element.adapt( Context.class );
            
            if( ctxt == null )
            {
                ctxt = Context.adapt( element.type().getModelElementClass().getClassLoader() );
            }

            return new StandardJavaTypeReferenceService( ctxt );
        }
    }

}
