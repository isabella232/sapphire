/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Danny Ju - [257456} DelayedTasksExecutor does not restart
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.swt.widgets.Display;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:danny.ju@oracle.com">Danny Ju</a>
 */

public final class DelayedTasksExecutor
{
    private static final long DELAY = 300;
    private static final long WORKER_THREAD_SHUTDOWN_DELAY = 10 * 60 * 1000;
    private static final Task[] NO_TASKS = new Task[ 0 ];
    private static final TaskPriorityComparator TASK_PRIORITY_COMPARATOR = new TaskPriorityComparator();
    
    private static final Set<Task> tasks = new LinkedHashSet<Task>();
    private static long timeOfLastAddition = 0;
    private static long timeOfLastWork = System.currentTimeMillis();
    private static WorkerThread workerThread = null;
    
    public static void schedule( final Task task )
    {
        syncExec
        (
            new Runnable()
            {
                public void run()
                {
                    boolean taskScheduled = false;
                    
                    for( Task t : tasks )
                    {
                        if( t.subsumes( task ) )
                        {
                            taskScheduled = true;
                            break;
                        }
                        else if( task.subsumes( t ) )
                        {
                            tasks.remove( t );
                            break;
                        }
                    }
                    
                    if( ! taskScheduled )
                    {
                        tasks.add( task );
                    }
                    
                    timeOfLastAddition = System.currentTimeMillis();
                    
                    if( workerThread == null || ! workerThread.isAlive() )
                    {
                        timeOfLastWork = System.currentTimeMillis();
                        workerThread = new WorkerThread();
                        workerThread.start();
                    }
                }
            }
        );
    }
    
    public static void sweep()
    {
        process( true );
    }

    private static void process( final boolean doNotDelay )
    {
        syncExec
        (
            new Runnable()
            {
                public void run()
                {
                    Task[] tasksToProcess = NO_TASKS;
                    
                    if( ! tasks.isEmpty() )
                    {
                        boolean process = doNotDelay;
                        
                        if( ! process )
                        {
                            final long now = System.currentTimeMillis();
                            final long diff = now - timeOfLastAddition;
                            
                            process = ( diff >= DELAY );
                        }
                        
                        if( process )
                        {
                            tasksToProcess = tasks.toArray( new Task[ tasks.size() ] );
                            tasks.clear();
                            timeOfLastAddition = 0;
                        }
                    }
                    
                    if( tasksToProcess.length > 0 )
                    {
                        Arrays.sort( tasksToProcess, TASK_PRIORITY_COMPARATOR );
                        
                        for( final Runnable task : tasksToProcess )
                        {
                            try
                            {
                                task.run();
                            }
                            catch( Exception e )
                            {
                                Sapphire.service( LoggingService.class ).log( e );
                            }
                        }
                        
                        timeOfLastWork = System.currentTimeMillis();
                    }
                }
            }
        );
    }
    
    private static void syncExec( final Runnable op )
    {
        final Display display = Display.getDefault();
        
        if( Thread.currentThread() == display.getThread() )
        {
            op.run();
        }
        else
        {
            display.syncExec( op );
        }
    }
    
    public static abstract class Task implements Runnable
    {
        public int getPriority()
        {
            return 0;
        }
        
        public boolean subsumes( final Task task )
        {
            return equals( task );
        }
    }
    
    private static final class TaskPriorityComparator implements Comparator<Task>
    {
        public int compare( final Task t1, final Task t2 )
        {
            return t2.getPriority() - t1.getPriority();
        }
    }
    
    private static final class WorkerThread extends Thread
    {
        public WorkerThread()
        {
            super( "Sapphire Delayed Tasks Executor" );
        }
        
        public void run()
        {
            while( true )
            {
                process( false );
                
                if( System.currentTimeMillis() - timeOfLastWork >= WORKER_THREAD_SHUTDOWN_DELAY )
                {
                    return;
                }
                
                try
                {
                    sleep( DELAY );
                }
                catch( InterruptedException e ) {}
            }
        }
    }

}
