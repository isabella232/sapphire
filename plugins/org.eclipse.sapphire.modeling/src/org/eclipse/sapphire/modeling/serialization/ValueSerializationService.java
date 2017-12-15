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

package org.eclipse.sapphire.modeling.serialization;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.ModelElementService;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.extensibility.SerializersExtensionPoint;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;

/**
 * Service for handling serialization of values to and from string form. To access this service,
 * use code like the following:
 * 
 * <p><code>
 * IModelElement el = ...<br/>
 * ValueSerializationService svc = el.service( ValueSerializationService.class );
 * </code></p>
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ValueSerializationService

    extends ModelElementService
    
{
    private static final ValueSerializerImpl<Object> DEFAULT_SERIALIZER = new ValueSerializerImpl<Object>()
    {
        @Override
        protected Object decodeFromString( final String value )
        {
            return value;
        }
    };
    
    private final Map<ValueProperty,ValueSerializerImpl<Object>> serializers 
        = Collections.synchronizedMap( new HashMap<ValueProperty,ValueSerializerImpl<Object>>() );
    
    public String encode( final ValueProperty property, 
                          final Object value )
    {
        return getSerializer( property ).encode( value );
    }
    
    public Object decode( final ValueProperty property, 
                          final String value )
    {
        return getSerializer( property ).decode( value );
    }

    @SuppressWarnings( "unchecked" )
    
    private ValueSerializerImpl<Object> getSerializer( final ValueProperty property )
    {
        final Class<?> type = property.getTypeClass();
        ValueSerializerImpl<Object> serializer = DEFAULT_SERIALIZER;
        
        if( type != String.class )
        {
            serializer = this.serializers.get( property );
            
            final ValueSerializer valueSerializerAnnotation = property.getAnnotation( ValueSerializer.class );
            
            if( valueSerializerAnnotation != null )
            {
                try
                {
                    serializer = (ValueSerializerImpl<Object>) valueSerializerAnnotation.impl().newInstance();
                    serializer.init( getModelElement(), property, valueSerializerAnnotation.params() );
                }
                catch( Exception e )
                {
                    SapphireModelingFrameworkPlugin.log( e );
                }
            }
            
            if( serializer == null )
            {
                serializer = (ValueSerializerImpl<Object>) SerializersExtensionPoint.getSerializer( type );
                
                if( serializer != null )
                {
                    try
                    {
                        serializer.init( getModelElement(), property, new String[ 0 ] );
                    }
                    catch( Exception e )
                    {
                        SapphireModelingFrameworkPlugin.log( e );
                        serializer = DEFAULT_SERIALIZER;
                    }
                }
                else
                {
                    final String message
                        = NLS.bind( Resources.noSerializer, 
                                    new Object[] { getModelElement().getModelElementType().getModelElementClass().getName(),
                                    property.getName(), type.getName() } );
                    
                    SapphireModelingFrameworkPlugin.logError( message, null );
                    serializer = DEFAULT_SERIALIZER;
                }
                
                this.serializers.put( property, serializer );
            }
        }
        
        return serializer;
    }
    
    private static final class Resources
    
        extends NLS
    
    {
        public static String noSerializer;
        
        static
        {
            initializeMessages( ValueSerializationService.class.getName(), Resources.class );
        }
    }
    
}
