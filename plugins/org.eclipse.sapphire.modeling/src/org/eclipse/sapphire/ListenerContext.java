/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ListenerContext
{
    private static final boolean TRACE = false;
    
    private final Set<Listener> listeners = new LinkedHashSet<Listener>();
    private final JobQueue<EventDeliveryJob> queue;
    
    public ListenerContext()
    {
        this( null );
    }
    
    public ListenerContext( final JobQueue<EventDeliveryJob> queue )
    {
        this.queue = ( queue == null ? new JobQueue<EventDeliveryJob>() : queue );
    }
    
    public JobQueue<EventDeliveryJob> queue()
    {
        return this.queue;
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
            this.queue.prune
            (
                new Filter<EventDeliveryJob>()
                {
                    @Override
                    public boolean allows( final EventDeliveryJob job )
                    {
                        return ! job.listener().equals( listener );
                    }
                    
                }
            );
            
            return true;
        }
        
        return false;
    }
    
    public void post( final Event event )
    {
        if( event == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.queue.prune
        (
            new Filter<EventDeliveryJob>()
            {
                @Override
                public boolean allows( final EventDeliveryJob job )
                {
                    return ! event.supersedes( job.event() );
                }
            }
        );
        
        synchronized( this )
        {
            if( TRACE )
            {
                event.trace( this.listeners.size() );
            }
            
            for( Listener listener : this.listeners )
            {
                this.queue.add( new EventDeliveryJob( listener, event ) );
            }
        }
    }
    
    public void broadcast()
    {
        this.queue.process();
    }

    public void broadcast( final Event event )
    {
        post( event );
        broadcast();
    }

}
