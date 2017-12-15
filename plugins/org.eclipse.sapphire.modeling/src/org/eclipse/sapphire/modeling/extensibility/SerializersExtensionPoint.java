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

package org.eclipse.sapphire.modeling.extensibility;

import static org.eclipse.sapphire.modeling.util.internal.PluginUtil.findExtensions;
import static org.eclipse.sapphire.modeling.util.internal.PluginUtil.findRequiredAttribute;
import static org.eclipse.sapphire.modeling.util.internal.PluginUtil.getTopLevelElements;
import static org.eclipse.sapphire.modeling.util.internal.PluginUtil.instantiate;
import static org.eclipse.sapphire.modeling.util.internal.PluginUtil.loadClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;
import org.eclipse.sapphire.modeling.serialization.ValueSerializerImpl;
import org.eclipse.sapphire.modeling.util.internal.PluginUtil.InvalidExtensionException;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SerializersExtensionPoint
{
    private static final String EL_VALUE_SERIALIZER = "value-serializer";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_IMPL = "impl";
    private static final SerializerRecord BAD_SERIALIZER_RECORD = new SerializerRecord();
    
    private static List<SerializerRecord> serializers = null;
    private static Map<Class<?>,SerializerRecord> cachedSerializerLookups = null;
    
    public static ValueSerializerImpl<?> getSerializer( final Class<?> type )
    {
        initialize();
        
        SerializerRecord serializerRecord = null;
        
        synchronized( SerializersExtensionPoint.class )
        {
            serializerRecord = cachedSerializerLookups.get( type );
            
            if( serializerRecord == null )
            {
                for( SerializerRecord rec : serializers )
                {
                    if( rec.typeClass.isAssignableFrom( type ) )
                    {
                        serializerRecord = rec;
                        cachedSerializerLookups.put( type, rec );
                        
                        break;
                    }
                }
            }
        }
        
        ValueSerializerImpl<?> serializer = null;
        
        if( serializerRecord != null && serializerRecord != BAD_SERIALIZER_RECORD )
        {
            serializer = instantiate( serializerRecord.declaringBundleId, serializerRecord.implClass );
            
            if( serializer == null )
            {
                synchronized( SerializersExtensionPoint.class )
                {
                    cachedSerializerLookups.put( type, BAD_SERIALIZER_RECORD );
                    serializers.remove( serializerRecord );
                }
            }
        }
        
        return serializer;
    }
    
    private static synchronized void initialize()
    {
        if( serializers == null )
        {
            serializers = new ArrayList<SerializerRecord>();
            cachedSerializerLookups = new HashMap<Class<?>,SerializerRecord>();
            
            for( IConfigurationElement el : getTopLevelElements( findExtensions( SapphireModelingFrameworkPlugin.PLUGIN_ID, "serializers" ) ) )
            {
                final String declaringBundleId = el.getNamespaceIdentifier();
                
                if( el.getName().equals( EL_VALUE_SERIALIZER ) )
                {
                    final SerializerRecord serviceRecord = new SerializerRecord();
                    serviceRecord.declaringBundleId = declaringBundleId;
                    
                    try
                    {
                        final String type = findRequiredAttribute( el, ATTR_TYPE );
                        serviceRecord.typeClass = loadClass( declaringBundleId, type, Object.class );

                        final String impl = findRequiredAttribute( el, ATTR_IMPL );
                        serviceRecord.implClass = loadClass( declaringBundleId, impl, ValueSerializerImpl.class );
                        
                        if( serviceRecord.typeClass != null && serviceRecord.implClass != null )
                        {
                            serializers.add( serviceRecord );
                        }
                    }
                    catch( InvalidExtensionException e )
                    {
                        // Problem already reported to the user. Just need to continue gracefully.
                    }
                }
            }
        }
    }
    
    @SuppressWarnings( "unchecked" )

    private static final class SerializerRecord
    {
        public Class<?> typeClass;
        public Class<ValueSerializerImpl> implClass;
        public String declaringBundleId;
    }
    
}
