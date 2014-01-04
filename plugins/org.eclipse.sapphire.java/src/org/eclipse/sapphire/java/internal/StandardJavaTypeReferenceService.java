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

package org.eclipse.sapphire.java.internal;

import org.eclipse.sapphire.Context;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.java.ClassBasedJavaType;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.java.JavaTypeReferenceService;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StandardJavaTypeReferenceService extends JavaTypeReferenceService
{
    private Context context;
    
    public StandardJavaTypeReferenceService()
    {
    }

    /**
     * Constructor used by the unit tests.
     */
    
    public StandardJavaTypeReferenceService( final ClassLoader loader )
    {
        this.context = Context.adapt( loader );
    }
    
    @Override
    protected void init()
    {
        super.init();
        
        final Element element = context( Element.class );
        
        this.context = element.adapt( Context.class );
        
        if( this.context == null )
        {
            this.context = Context.adapt( element.type().getModelElementClass().getClassLoader() );
        }
    }

    @Override
    public JavaType resolve( final String name )
    {
        if( name != null )
        {
            final Class<?> cl = this.context.findClass( name );
            
            if( cl != null )
            {
                return new ClassBasedJavaType( cl );
            }
        }
        
        return null;
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
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
    }

}
