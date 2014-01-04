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

package org.eclipse.sapphire.services;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ListenerContext;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.util.MapFactory;
import org.eclipse.sapphire.util.SetFactory;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class Service
{
    private boolean initialized;
    private ServiceContext context;
    private String id;
    private Map<String,String> params;
    private Set<String> overrides;
    private final ListenerContext listeners = new ListenerContext();
    
    final void init( final ServiceContext context,
                     final String id,
                     final Map<String,String> params,
                     final Set<String> overrides )
    {
        this.context = context;
        this.id = id;
        this.params = MapFactory.unmodifiable( params );
        this.overrides = SetFactory.unmodifiable( overrides );
    }
    
    final void initIfNecessary()
    {
        if( ! this.initialized )
        {
            this.initialized = true;
            
            try
            {
                init();
            }
            catch( Exception e )
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
        }
    }
    
    protected void init()
    {
        
    }
    
    public final ServiceContext context()
    {
        return this.context;
    }
    
    public final <T> T context( final Class<T> type )
    {
        return this.context.find( type );
    }
    
    protected final <S extends Service> S service( final Class<S> serviceType )
    {
        return this.context.service( serviceType );
    }

    protected final <S extends Service> List<S> services( final Class<S> serviceType )
    {
        return this.context.services( serviceType );
    }
    
    final String id()
    {
        return this.id;
    }
    
    protected final Map<String,String> params()
    {
        return this.params;
    }
    
    protected final String param( final String name )
    {
        return this.params.get( name );
    }
    
    final Set<String> overrides()
    {
        return this.overrides;
    }
    
    final void coordinate( final ListenerContext context )
    {
        this.listeners.coordinate( context );
    }
    
    public final boolean attach( final Listener listener )
    {
        return this.listeners.attach( listener );
    }
    
    public final boolean detach( final Listener listener )
    {
        return this.listeners.detach( listener );
    }
    
    protected final void broadcast( final Event event )
    {
        this.listeners.broadcast( event );
    }
    
    protected final void broadcast()
    {
        broadcast( new ServiceEvent( this ) );
    }
    
    public void dispose()
    {
    }
    
}
