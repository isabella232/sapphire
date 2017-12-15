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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ListenerContext;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class Service
{
    private ServiceContext context;
    private Map<String,String> params;
    private final ListenerContext listeners = new ListenerContext();
    
    final void init( final ServiceContext context,
                     final Map<String,String> params )
    {
        this.context = context;
        
        final int paramsCount = params.size();
        
        if( paramsCount == 0 )
        {
            this.params = Collections.emptyMap();
        }
        else if( paramsCount == 1 )
        {
            final Map.Entry<String,String> entry = params.entrySet().iterator().next();
            this.params = Collections.singletonMap( entry.getKey(), entry.getValue() );
        }
        else
        {
            this.params = new HashMap<String,String>( params );
            this.params = Collections.unmodifiableMap( this.params );
        }
    }
    
    protected void init()
    {
        
    }
    
    protected final ServiceContext context()
    {
        return this.context;
    }
    
    protected final <T> T context( final Class<T> type )
    {
        return this.context.find( type );
    }
    
    protected final Map<String,String> params()
    {
        return this.params;
    }
    
    protected final String param( final String name )
    {
        return this.params.get( name );
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
        this.listeners.broadcast();
    }
    
    public void dispose()
    {
    }
    
}
