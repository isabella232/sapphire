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

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyService;
import org.eclipse.sapphire.modeling.ModelPropertyServiceFactory;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.internal.SapphireModelingExtensionSystem;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;
import org.eclipse.sapphire.modeling.serialization.ValueSerialization;
import org.eclipse.sapphire.modeling.serialization.ValueSerializationService;

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
                    SapphireModelingFrameworkPlugin.log( e );
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
                        SapphireModelingFrameworkPlugin.log( e );
                        serializer = null;
                    }
                }
                else
                {
                    final String message
                        = NLS.bind( Resources.noSerializer, 
                                    new Object[] { element.getModelElementType().getModelElementClass().getName(),
                                    property.getName(), type.getName() } );
                    
                    SapphireModelingFrameworkPlugin.logError( message, null );
                }
            }
        }
        
        if( serializer == null )
        {
            serializer = DEFAULT_SERIALIZER;
        }
        
        return serializer;
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
