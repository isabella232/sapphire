/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.serialization.internal;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyService;
import org.eclipse.sapphire.modeling.ModelPropertyServiceFactory;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.internal.SapphireModelingExtensionSystem;
import org.eclipse.sapphire.modeling.serialization.ValueSerialization;
import org.eclipse.sapphire.modeling.serialization.ValueSerializationService;
import org.eclipse.sapphire.modeling.util.NLS;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ValueSerializationServiceFactory

    extends ModelPropertyServiceFactory
    
{
    private static final ValueSerializationService DEFAULT_SERIALIZER = new ValueSerializationService()
    {
        @Override
        protected Object decodeFromString( final String value )
        {
            return value;
        }
    };
    
    @Override
    public boolean applicable( final IModelElement element,
                               final ModelProperty property,
                               final Class<? extends ModelPropertyService> service )
    {
        return ( property instanceof ValueProperty );
    }

    @Override
    public ModelPropertyService create( final IModelElement element,
                                        final ModelProperty property,
                                        final Class<? extends ModelPropertyService> service )
    {
        final Class<?> type = property.getTypeClass();
        ValueSerializationService serializer = null;;
        
        if( type != String.class )
        {
            final ValueSerialization valueSerializerAnnotation = property.getAnnotation( ValueSerialization.class );
            
            if( valueSerializerAnnotation != null )
            {
                try
                {
                    serializer = valueSerializerAnnotation.service().newInstance();
                    serializer.init( element, property, valueSerializerAnnotation.params() );
                }
                catch( Exception e )
                {
                    LoggingService.log( e );
                }
            }
            
            if( serializer == null )
            {
                serializer = SapphireModelingExtensionSystem.createValueSerializer( element, (ValueProperty) property, type );
                
                if( serializer != null )
                {
                    try
                    {
                        serializer.init( element, property, new String[ 0 ] );
                    }
                    catch( Exception e )
                    {
                        LoggingService.log( e );
                        serializer = null;
                    }
                }
                else
                {
                    final String message
                        = NLS.bind( Resources.noSerializer, 
                                    element.getModelElementType().getModelElementClass().getName(),
                                    property.getName(), type.getName() );
                    
                    LoggingService.log( Status.createErrorStatus( message ) );
                }
            }
        }
        
        if( serializer == null )
        {
            serializer = DEFAULT_SERIALIZER;
        }
        
        final ValueSerializationService finalSerializer = serializer;
        
        // The serializer is wrapped before being returned because when a service is returned from
        // the factory, its init method is called, which will cause double initialization and can
        // override the params coming from @ValueSerialization annotation.
        
        final ValueSerializationService wrapper = new ValueSerializationService()
        {
            @Override
            public String encode( final Object value )
            {
                return finalSerializer.encode( value );
            }
            
            @Override
            protected Object decodeFromString( final String value )
            {
                return finalSerializer.decode( value );
            }
        };
        
        return wrapper;
    }
    
    private static final class Resources extends NLS
    {
        public static String noSerializer;
        
        static
        {
            initializeMessages( ValueSerializationServiceFactory.class.getName(), Resources.class );
        }
    }

}
