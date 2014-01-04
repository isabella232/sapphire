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

package org.eclipse.sapphire.modeling.el.internal;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyEvent;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class PropertyFunction<P extends Property> extends Function
{
    @Override
    public final FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            private P property;
            private Listener listener;
            
            @Override
            protected Object evaluate()
            {
                P p = null;
                
                try
                {
                    p = operand( 0, findPropertyType(), false );
                }
                finally
                {
                    if( this.property != p )
                    {
                        if( this.property != null && this.listener != null )
                        {
                            this.property.detach( this.listener );
                        }
                        
                        this.property = p;
                        
                        if( this.property != null )
                        {
                            if( this.listener == null )
                            {
                                this.listener = new Listener()
                                {
                                    @Override
                                    public void handle( final Event event )
                                    {
                                        if( event instanceof PropertyEvent && relevant( (PropertyEvent) event ) )
                                        {
                                            refresh();
                                        }
                                    }
                                };
                            }
                            
                            this.property.attach( this.listener );
                        }
                    }
                }
                
                return PropertyFunction.this.evaluate( this.property );
            }
            
            @Override
            public void dispose()
            {
                super.dispose();
                
                if( this.property != null )
                {
                    this.property.detach( this.listener );
                    this.property = null;
                    this.listener = null;
                }
            }
        };
    }
    
    protected abstract Object evaluate( P property );
    
    protected abstract boolean relevant( PropertyEvent event );
    
    private Class<P> findPropertyType()
    {
        return findPropertyType( PropertyFunction.this.getClass() );
    }
    
    @SuppressWarnings( "unchecked" )
    
    private Class<P> findPropertyType( final Class<?> cl )
    {
        final Type superGenericType = cl.getGenericSuperclass();
        
        if( superGenericType instanceof ParameterizedType )
        {
            final ParameterizedType superParameterizedType = (ParameterizedType) superGenericType;
            final Class<?> superRawType = (Class<?>) superParameterizedType.getRawType();
            
            if( superRawType == PropertyFunction.class )
            {
                final Type t = superParameterizedType.getActualTypeArguments()[ 0 ];
                
                if( t instanceof Class )
                {
                    return (Class<P>) t;
                }
                else if( t instanceof ParameterizedType )
                {
                    return (Class<P>) ( (ParameterizedType) t ).getRawType();
                }
                else
                {
                    throw new IllegalStateException();
                }
            }
            else
            {
                return findPropertyType( superRawType );
            }
        }
        else
        {
            final Class<?> superRawType = (Class<?>) superGenericType;
            
            if( superRawType == PropertyFunction.class )
            {
                throw new IllegalStateException( "PropertyFunction must be parameterized." );
            }
            else
            {
                return findPropertyType( superRawType );
            }
        }
    }
    
}
