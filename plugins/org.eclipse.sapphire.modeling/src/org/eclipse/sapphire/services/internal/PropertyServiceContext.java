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

package org.eclipse.sapphire.services.internal;

import java.lang.annotation.Annotation;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.TransientProperty;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class PropertyServiceContext extends AnnotationsAwareServiceContext
{
    private final ModelProperty property;
    
    public PropertyServiceContext( final String type,
                                   final ServiceContext parent,
                                   final ModelProperty property )
    {
        super( type, parent );
        
        this.property = property;
    }
    
    @Override
    @SuppressWarnings( "unchecked" )
    
    public <T> T find( final Class<T> type )
    {
        T obj = super.find( type );
        
        if( obj == null )
        {
            if( type == ModelProperty.class )
            {
                obj = (T) this.property;
            }
            else if( type == ValueProperty.class )
            {
                if( this.property instanceof ValueProperty )
                {
                    obj = (T) this.property;
                }
            }
            else if( type == ListProperty.class )
            {
                if( this.property instanceof ListProperty )
                {
                    obj = (T) this.property;
                }
            }
            else if( type == ElementProperty.class )
            {
                if( this.property instanceof ElementProperty )
                {
                    obj = (T) this.property;
                }
            }
            else if( type == ImpliedElementProperty.class )
            {
                if( this.property instanceof ImpliedElementProperty )
                {
                    obj = (T) this.property;
                }
            }
            else if( type == TransientProperty.class )
            {
                if( this.property instanceof TransientProperty )
                {
                    obj = (T) this.property;
                }
            }
        }
        
        return obj;
    }
    
    @Override
    
    protected <A extends Annotation> A annotation( final Class<A> type )
    {
        return this.property.getAnnotation( type );
    }
    
}
