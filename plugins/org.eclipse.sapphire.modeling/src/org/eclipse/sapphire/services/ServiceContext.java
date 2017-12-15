/******************************************************************************
 * Copyright (c) 2013 Oracle
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private final List<Service> services = new ArrayList<Service>();
    private final Set<Class<?>> initializedServiceTypes = new HashSet<Class<?>>();
    private final Set<String> exhaustedServiceFactories = new HashSet<String>();
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

    public final synchronized <S extends Service> List<S> services( final Class<S> serviceType )
    {
        if( this.disposed )
        {
            throw new IllegalStateException();
        }
        
        if( ! this.initializedServiceTypes.contains( serviceType ) )
        {
            this.initializedServiceTypes.add( serviceType );
            
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
                final String id = factory.id();
                
                if( ! this.exhaustedServiceFactories.contains( id ) && factory.applicable( this, serviceType ) )
                {
                    this.exhaustedServiceFactories.add( id );
                    
                    Service service = null;
                    
                    try
                    {
                        service = factory.create( this, serviceType );
                        service.init( this, factory.parameters() );
                    }
                    catch( Exception e )
                    {
                        LoggingService.log( e );
                    }
                    
                    if( service != null )
                    {
                        for( String overriddenServiceId : factory.overrides() )
                        {
                            applicable.remove( overriddenServiceId );
                        }
                        
                        this.services.add( service );
                        
                        try
                        {
                            service.init();
                        }
                        catch( Exception e )
                        {
                            LoggingService.log( e );
                        }
                    }
                }
            }
            
            // Instantiate global services that haven't been overridden.
            
            for( ServiceFactoryProxy factory : applicable.values() )
            {
                final String id = factory.id();
                
                if( ! this.exhaustedServiceFactories.contains( id ) )
                {
                    this.exhaustedServiceFactories.add( id );
                    
                    Service service = null;
                    
                    try
                    {
                        service = factory.create( this, serviceType );
                        service.init( this, factory.parameters() );
                    }
                    catch( Exception e )
                    {
                        LoggingService.log( e );
                    }
    
                    if( service != null )
                    {
                        this.services.add( service );
                        
                        try
                        {
                            service.init();
                        }
                        catch( Exception e )
                        {
                            LoggingService.log( e );
                        }
                    }
                }
            }
        }
        
        final List<S> result = new ArrayList<S>( 1 );
        
        for( Service service : this.services )
        {
            if( serviceType.isInstance( service ) )
            {
                result.add( serviceType.cast( service ) );
            }
        }
        
        if( this.parent != null && result.isEmpty() )
        {
            result.addAll( this.parent.services( serviceType ) );
        }
        
        return Collections.unmodifiableList( result );
    }

    protected List<ServiceFactoryProxy> local()
    {
        return Collections.emptyList();
    }
    
    public final void dispose()
    {
        this.disposed = true;
        
        for( Service service : this.services )
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
        
        this.services.clear();
    }
    
}
