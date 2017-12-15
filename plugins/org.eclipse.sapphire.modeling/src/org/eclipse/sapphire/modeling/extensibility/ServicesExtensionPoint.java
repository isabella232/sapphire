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
import org.eclipse.sapphire.modeling.ModelElementService;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;
import org.eclipse.sapphire.modeling.util.internal.PluginUtil.InvalidExtensionException;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ServicesExtensionPoint
{
    private static final String EL_SERVICE = "service";
    private static final String ATTR_IMPL = "impl";
    private static final ServiceRecord BAD_SERVICE_RECORD = new ServiceRecord();
    
    private static List<ServiceRecord> services = null;
    private static Map<Class<?>,ServiceRecord> cachedServiceLookups = null;
    
    @SuppressWarnings( "unchecked" )
    
    public static <S extends ModelElementService> S getService( final Class<S> serviceType )
    {
        initialize();
        
        ServiceRecord serviceRecord = null;
        
        synchronized( ServicesExtensionPoint.class )
        {
            serviceRecord = cachedServiceLookups.get( serviceType );
            
            if( serviceRecord == null )
            {
                for( ServiceRecord rec : services )
                {
                    if( serviceType.isAssignableFrom( rec.implClass ) )
                    {
                        serviceRecord = rec;
                        cachedServiceLookups.put( serviceType, rec );
                        
                        break;
                    }
                }
            }
        }
        
        S service = null;
        
        if( serviceRecord != null && serviceRecord != BAD_SERVICE_RECORD )
        {
            service = (S) instantiate( serviceRecord.declaringBundleId, serviceRecord.implClass );
            
            if( service == null )
            {
                synchronized( ServicesExtensionPoint.class )
                {
                    cachedServiceLookups.put( serviceType, BAD_SERVICE_RECORD );
                    services.remove( serviceRecord );
                }
            }
        }
        
        return service;
    }
    
    private static synchronized void initialize()
    {
        if( services == null )
        {
            services = new ArrayList<ServiceRecord>();
            cachedServiceLookups = new HashMap<Class<?>,ServiceRecord>();
            
            for( IConfigurationElement el : getTopLevelElements( findExtensions( SapphireModelingFrameworkPlugin.PLUGIN_ID, "services" ) ) )
            {
                final String declaringBundleId = el.getNamespaceIdentifier();
                
                if( el.getName().equals( EL_SERVICE ) )
                {
                    final ServiceRecord serviceRecord = new ServiceRecord();
                    serviceRecord.declaringBundleId = declaringBundleId;
                    
                    try
                    {
                        final String clname = findRequiredAttribute( el, ATTR_IMPL );
                        serviceRecord.implClass = loadClass( declaringBundleId, clname, ModelElementService.class );
                        
                        if( serviceRecord.implClass != null )
                        {
                            services.add( serviceRecord );
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
    
    private static final class ServiceRecord
    {
        public Class<ModelElementService> implClass;
        public String declaringBundleId;
    }
    
}
