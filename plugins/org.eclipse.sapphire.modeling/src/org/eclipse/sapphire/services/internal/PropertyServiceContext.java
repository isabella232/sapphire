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

package org.eclipse.sapphire.services.internal;

import java.lang.annotation.Annotation;
import java.util.List;

import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.EventDeliveryJob;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.JobQueue;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.TransientProperty;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class PropertyServiceContext extends AnnotationsAwareServiceContext
{
    private final PropertyDef property;
    
    public PropertyServiceContext( final String type,
                                   final ServiceContext parent,
                                   final PropertyDef property,
                                   final Object lock,
                                   final JobQueue<EventDeliveryJob> queue )
    {
        super( type, parent, lock, queue );
        
        this.property = property;
    }
    
    @Override
    @SuppressWarnings( "unchecked" )
    
    public <T> T find( final Class<T> type )
    {
        T obj = super.find( type );
        
        if( obj == null )
        {
            if( type == PropertyDef.class )
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
    
    protected <A extends Annotation> List<A> annotations( final Class<A> type )
    {
        return this.property.getAnnotations( type );
    }
    
}
