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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A generic queue for processing jobs. 
 * 
 * <p>Used by {@link ListenerContext} to deliver events to listeners. A single {@link JobQueue} can be shared by multiple
 * {@link ListenerContext} instances in order to synchronize event delivery.</p>
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class JobQueue<T extends Runnable>
{
    private final Queue<T> queue = new ConcurrentLinkedQueue<T>();
    private final List<Filter<T>> filters = new CopyOnWriteArrayList<Filter<T>>();
    
    /**
     * Adds a job to the end of the queue.
     * 
     * @param job the job
     * @throws IllegalArgumentException if the job is null
     */
    
    public void add( final T job )
    {
        if( job == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.queue.add( job );
    }
    
    /**
     * Processes all of the jobs in the queue until the queue is empty or all of the remaining jobs are suspended
     * through the attached filters.
     */
    
    public void process()
    {
        List<T> skipped = null;
        
        for( T job = this.queue.poll(); job != null; job = this.queue.poll() )
        {
            boolean skip = false;
            
            for( final Filter<T> filter : this.filters )
            {
                try
                {
                    if( ! filter.allows( job ) )
                    {
                        skip = true;
                        break;
                    }
                }
                catch( Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                }
            }
            
            if( skip )
            {
                if( skipped == null )
                {
                    skipped = new ArrayList<T>();
                }
                
                skipped.add( job );
            }
            else
            {
                try
                {
                    job.run();
                }
                catch( Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                }
            }
        }
        
        if( skipped != null )
        {
            this.queue.addAll( skipped );
        }
    }
    
    /**
     * Removes jobs from the queue that are rejected by the filter.
     * 
     * @param filter the filter
     * @throws IllegalArgumentException if the filter is null
     */
    
    public void prune( final Filter<T> filter )
    {
        if( filter == null )
        {
            throw new IllegalArgumentException();
        }
        
        for( final Iterator<T> itr = this.queue.iterator(); itr.hasNext(); )
        {
            if( ! filter.allows( itr.next() ) )
            {
                itr.remove();
            }
        }
    }
    
    /**
     * Suspends jobs that are rejected by the filter. The suspended jobs will again become available for
     * processing once the suspension is released. 
     * 
     * @param filter the filter
     * @return a handle that must be disposed to release the suspension
     * @throws IllegalArgumentException if the filter is null
     */
    
    public Disposable suspend( final Filter<T> filter )
    {
        if( filter == null )
        {
            throw new IllegalArgumentException();
        }
        
        final DisposableFilter disposable = new DisposableFilter( filter );
        
        this.filters.add( disposable );
        
        return disposable;
    }
    
    private final class DisposableFilter implements Filter<T>, Disposable
    {
        private final Filter<T> base;
        
        public DisposableFilter( final Filter<T> base )
        {
            this.base = base;
        }

        @Override
        public boolean allows( final T element )
        {
            return this.base.allows( element );
        }

        @Override
        public void dispose()
        {
            JobQueue.this.filters.remove( this );
        }
    }

}
