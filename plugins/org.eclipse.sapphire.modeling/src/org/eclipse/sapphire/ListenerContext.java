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

package org.eclipse.sapphire;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ListenerContext
{
    private static final boolean TRACE = false;
    
    private final Set<Listener> listeners = new LinkedHashSet<Listener>();
    private Queue<BroadcastJob> queue = new ConcurrentLinkedQueue<BroadcastJob>();
    private final Map<Class<? extends Event>,Event> suspended = new HashMap<Class<? extends Event>,Event>();
    
    public void coordinate( final ListenerContext context )
    {
        synchronized( this )
        {
            this.queue = ( context == null ? new ConcurrentLinkedQueue<BroadcastJob>() : context.queue );
        }
    }
    
    public boolean attach( final Listener listener )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( this )
        {
            return this.listeners.add( listener );
        }
    }
    
    public boolean detach( final Listener listener )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        final boolean removed;
        
        synchronized( this )
        {
            removed = this.listeners.remove( listener );
        }
        
        if( removed )
        {
            for( BroadcastJob job : this.queue )
            {
                if( job.listener().equals( listener ) )
                {
                    this.queue.remove( job );
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    public void post( final Event event )
    {
        final Class<? extends Event> eventType = event.getClass();
        final boolean post;
        
        synchronized( this )
        {
            if( this.suspended.containsKey( eventType ) )
            {
                this.suspended.put( eventType, event );
                post = false;
            }
            else
            {
                post = true;
            }
        }
        
        if( post )
        {
            for( BroadcastJob job : this.queue )
            {
                if( event.supersedes( job.event() ) )
                {
                    this.queue.remove( job );
                }
            }
            
            synchronized( this )
            {
                if( TRACE )
                {
                    event.trace( this.listeners.size() );
                }
                
                for( Listener listener : this.listeners )
                {
                    this.queue.add( new BroadcastJob( listener, event ) );
                }
            }
        }
    }
    
    public void broadcast()
    {
        for( BroadcastJob job = this.queue.poll(); job != null; job = this.queue.poll() )
        {
            job.run();
        }
    }

    public void broadcast( final Event event )
    {
        post( event );
        broadcast();
    }
    
    public void suspend( final Class<? extends Event> eventType )
    {
        synchronized( this )
        {
            if( ! this.suspended.containsKey( eventType ) )
            {
                this.suspended.put( eventType, null );
            }
        }
    }
    
    public void resume( final Class<? extends Event> eventType )
    {
        Event event = null;
        
        synchronized( this )
        {
            event = this.suspended.remove( eventType );
        }
        
        if( event != null )
        {
            broadcast( event );
        }
    }
    
    private static final class BroadcastJob
    {
        private final Listener listener;
        private final Event event;
         
        public BroadcastJob( final Listener listener,
                             final Event event )
        {
            this.listener = listener;
            this.event = event;
        }
        
        public Listener listener()
        {
            return this.listener;
        }
        
        public Event event()
        {
            return this.event;
        }

        public void run()
        {
            try
            {
                this.listener.handle( this.event );
            }
            catch( Exception e )
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
        }
    }

}
