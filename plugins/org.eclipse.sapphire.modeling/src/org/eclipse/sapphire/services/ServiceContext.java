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

package org.eclipse.sapphire.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.internal.SapphireModelingExtensionSystem;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class ServiceContext
{
    public static final String ID_ELEMENT_INSTANCE = "Sapphire.Element.Instance";
    public static final String ID_ELEMENT_METAMODEL = "Sapphire.Element.MetaModel";
    public static final String ID_PROPERTY_INSTANCE = "Sapphire.Property.Instance";
    public static final String ID_PROPERTY_METAMODEL = "Sapphire.Property.MetaModel";
    
    private final String type;
    private final ServiceContext parent;
    private final Map<Class<? extends Service>,List<Service>> services = new HashMap<Class<? extends Service>,List<Service>>();
    private boolean disposed = false;
    
    public ServiceContext( final String type,
                           final ServiceContext parent )
    {
        this.type = type;
        this.parent = parent;
    }

    public final String type()
    {
        return this.type;
    }
    
    public final ServiceContext parent()
    {
        return this.parent;
    }
    
    public <T> T find( final Class<T> type )
    {
        return null;
    }
    
    public final <S extends Service> S service( final Class<S> serviceType )
    {
        if( this.disposed )
        {
            throw new IllegalStateException();
        }
        
        final List<S> services = services( serviceType );
        return ( services.isEmpty() ? null : services.get( 0 ) );
    }

    @SuppressWarnings( "unchecked" )
    
    public final synchronized <S extends Service> List<S> services( final Class<S> serviceType )
    {
        if( this.disposed )
        {
            throw new IllegalStateException();
        }
        
        List<Service> services = this.services.get( serviceType );
        
        if( services == null )
        {
            services = new ArrayList<Service>();
            
            // Find all applicable service factories declared via the extension system.
            
            final Map<String,ServiceFactoryProxy> applicable = new HashMap<String,ServiceFactoryProxy>();

            for( ServiceFactoryProxy factory : SapphireModelingExtensionSystem.getServiceFactories() )
            {
                if( factory.applicable( this, serviceType ) )
                {
                    applicable.put( factory.id(), factory );
                }
            }
            
            // Remove those that are overridden by another applicable service. Note that a cycle will 
            // cause all services in the cycle to be removed.
            
            for( ServiceFactoryProxy factory : new ArrayList<ServiceFactoryProxy>( applicable.values() ) )
            {
                for( String overriddenServiceId : factory.overrides() )
                {
                    applicable.remove( overriddenServiceId );
                }
            }
            
            // Process local service definitions.
            
            for( ServiceFactoryProxy factory : local() )
            {
                if( factory.applicable( this, serviceType ) )
                {
                    Service instance = null;
                    
                    try
                    {
                        instance = factory.create( this, serviceType );
                    }
                    catch( Exception e )
                    {
                        LoggingService.log( e );
                    }
                    
                    if( instance != null )
                    {
                        for( String overriddenServiceId : factory.overrides() )
                        {
                            applicable.remove( overriddenServiceId );
                        }
                        
                        services.add( instance );
                    }
                }
            }
            
            // Instantiate global services that haven't been overridden.
            
            for( ServiceFactoryProxy factory : applicable.values() )
            {
                try
                {
                    final Service service = factory.create( this, serviceType );
                    
                    if( service != null )
                    {
                        services.add( service );
                    }
                }
                catch( Exception e )
                {
                    LoggingService.log( e );
                }
            }
            
            // Store the list of services for future use.
            
            final int count = services.size();
            
            if( count == 0 )
            {
                services = Collections.emptyList();
            }
            else if( count == 1 )
            {
                services = Collections.singletonList( services.get( 0 ) );
            }
            else
            {
                services = Collections.unmodifiableList( services );
            }
            
            this.services.put( serviceType, services );
        }
        
        if( services.isEmpty() && this.parent != null )
        {
            services = (List<Service>) this.parent.services( serviceType );
        }
        
        return (List<S>) services;
    }

    protected List<ServiceFactoryProxy> local()
    {
        return Collections.emptyList();
    }
    
    public final void dispose()
    {
        this.disposed = true;
        
        for( List<Service> services : this.services.values() )
        {
            for( Service service : services )
            {
                try
                {
                    service.dispose();
                }
                catch( Exception e )
                {
                    LoggingService.log( e );
                }
            }
        }
        
        this.services.clear();
    }
    
}
