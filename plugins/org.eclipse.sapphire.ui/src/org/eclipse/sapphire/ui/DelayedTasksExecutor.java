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

package org.eclipse.sapphire.ui;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.util.MutableReference;
import org.eclipse.swt.widgets.Display;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DelayedTasksExecutor
{
    private static final long DELAY = 300;
    private static final long WORKER_THREAD_SHUTDOWN_DELAY = 10 * 60 * 1000;
    private static final Task[] NO_TASKS = new Task[ 0 ];
    private static final TaskPriorityComparator TASK_PRIORITY_COMPARATOR = new TaskPriorityComparator();
    
    private static final Set<Task> tasks = new LinkedHashSet<Task>();
    private static long timeOfLastAddition = 0;
    private static WorkerThread workerThread = null;
    
    public static void schedule( final Task task )
    {
        schedule( task, false );
    }
    
    private static void schedule( final Task task,
                                  final boolean doNotDelay )
    {
        synchronized( tasks )
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
            
            timeOfLastAddition = ( doNotDelay ? 0 : System.currentTimeMillis() );
            
            if( workerThread == null || ! workerThread.isAlive() )
            {
                workerThread = new WorkerThread();
                workerThread.start();
            }
            else
            {
                if( doNotDelay )
                {
                    workerThread.interrupt();
                }
            }
        }
    }
    
    public static void sweep()
    {
        final MutableReference<Boolean> sweeperCompletionResult = new MutableReference<Boolean>( false );
        
        final Task sweeper = new Task()
        {
            @Override
            public int getPriority()
            {
                return Integer.MIN_VALUE;
            }

            public void run()
            {
                synchronized( sweeperCompletionResult )
                {
                    sweeperCompletionResult.set( true );
                    sweeperCompletionResult.notifyAll();
                }
            }
        };
        
        schedule( sweeper, true );
        
        final Display display = Display.getDefault();
        
        if( display.getThread() == Thread.currentThread() )
        {
            while( sweeperCompletionResult.get() == false )
            {
                if( ! display.readAndDispatch() )
                {
                    display.sleep();
                }
            }
        }
        else
        {
            synchronized( sweeperCompletionResult )
            {
                try
                {
                    synchronized( sweeperCompletionResult )
                    {
                        while( sweeperCompletionResult.get() == false )
                        {
                            sweeperCompletionResult.wait();
                        }
                    }
                }
                catch( InterruptedException e ) {}
            }
        }
    }
    
    private static Task[] getTasksToRun()
    {
        synchronized( tasks )
        {
            Task[] result = NO_TASKS;
            
            if( ! tasks.isEmpty() )
            {
                final long now = System.currentTimeMillis();
                final long diff = now - timeOfLastAddition;
                
                if( diff >= DELAY )
                {
                    result = tasks.toArray( new Task[ tasks.size() ] );
                    tasks.clear();
                    timeOfLastAddition = 0;
                }
            }
            
            return result;
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
        public int compare( final Task t1,
                            final Task t2 )
        {
            return t2.getPriority() - t1.getPriority();
        }
    }
    
    private static final class WorkerThread extends Thread
    {
        private long timeOfLastWork = System.currentTimeMillis();
        
        public void run()
        {
            while( true )
            {
                Task[] tasks = getTasksToRun();
                
                if( tasks.length > 0 )
                {
                    final Task[] tasksToRun = tasks;
                    Arrays.sort( tasksToRun, TASK_PRIORITY_COMPARATOR );
                    
                    Display.getDefault().syncExec
                    (
                        new Runnable()
                        {
                            public void run()
                            {
                                for( Runnable task : tasksToRun )
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
                            }
                        }
                    );
                    
                    this.timeOfLastWork = System.currentTimeMillis();
                }
                else
                {
                    if( System.currentTimeMillis() - this.timeOfLastWork >= WORKER_THREAD_SHUTDOWN_DELAY )
                    {
                        return;
                    }
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
