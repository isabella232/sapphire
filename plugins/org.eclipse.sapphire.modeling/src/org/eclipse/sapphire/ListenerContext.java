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

package org.eclipse.sapphire;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.sapphire.modeling.LoggingService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ListenerContext
{
    private final Set<Listener> listeners = new CopyOnWriteArraySet<Listener>();
    private Queue<BroadcastJob> queue = new ConcurrentLinkedQueue<BroadcastJob>();
    private final Map<Class<? extends Event>,Event> suspended = new HashMap<Class<? extends Event>,Event>();
    
    public void coordinate( final ListenerContext context )
    {
        synchronized( this )
        {
            this.queue = context.queue;
        }
    }
    
    public boolean attach( final Listener listener )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        return this.listeners.add( listener );
    }
    
    public boolean detach( final Listener listener )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( this.listeners.remove( listener ) )
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
    
    public void broadcast( final Event event )
    {
        final Class<? extends Event> eventType = event.getClass();
        final boolean broadcast;
        
        synchronized( this )
        {
            if( this.suspended.containsKey( eventType ) )
            {
                this.suspended.put( eventType, event );
                broadcast = false;
            }
            else
            {
                broadcast = true;
            }
        }
        
        if( broadcast )
        {
            for( BroadcastJob job : this.queue )
            {
                if( event.supersedes( job.event() ) )
                {
                    this.queue.remove( job );
                }
            }
            
            for( Listener listener : this.listeners )
            {
                this.queue.add( new BroadcastJob( listener, event ) );
            }
            
            for( BroadcastJob job = this.queue.poll(); job != null; job = this.queue.poll() )
            {
                job.run();
            }
        }
    }
    
    public void broadcast()
    {
        broadcast( new Event() );
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
                LoggingService.log( e );
            }
        }
    }

}
