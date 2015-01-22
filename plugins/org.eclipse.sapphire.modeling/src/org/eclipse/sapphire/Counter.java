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

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a numeric counter that can be incremented, reset and read. Counters are useful for code instrumentation,
 * such as determining how many times a particular method is called. 
 * 
 * <p>A counter can be created and used directly or through the supplied static methods that work with a collection 
 * of global counters.</p>
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class Counter
{
    private static Map<String,Counter> counters;
    private long value;
    
    /**
     * Finds a named global counter, creating it if necessary.
     * 
     * @param name the name of the counter
     * @return the counter
     */
    
    public synchronized static Counter find( final String name )
    {
        if( name == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( counters == null )
        {
            counters = new HashMap<String,Counter>();
        }
        
        Counter counter = counters.get( name );
        
        if( counter == null )
        {
            counter = new Counter();
            counters.put( name, counter );
        }
        
        return counter;
    }
    
    /**
     * Finds a named global counter, creating it if necessary.
     * 
     * @param cl the class whose name should be used as the name of the counter
     * @return the counter
     */
    
    public static Counter find( final Class<?> cl )
    {
        if( cl == null )
        {
            throw new IllegalArgumentException();
        }
        
        return find( cl.getName() );
    }
    
    /**
     * Increments a named global counter by one. No action is taken if the counter does not exist by having
     * been previously accessed through the find method.
     * 
     * @param name the name of the counter
     */
    
    public synchronized static void increment( final String name )
    {
        if( name == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( counters != null )
        {
            final Counter counter = counters.get( name );
            
            if( counter != null )
            {
                counter.increment();
            }
        }
    }
    
    /**
     * Increments a named global counter by one. No action is taken if the counter does not exist by having
     * been previously accessed through the find method.
     * 
     * @param cl the class whose name should be used as the name of the counter
     */
    
    public static void increment( final Class<?> cl )
    {
        if( cl == null )
        {
            throw new IllegalArgumentException();
        }
        
        increment( cl.getName() );
    }
    
    /**
     * Increments the counter by one.
     */
    
    public synchronized void increment()
    {
        this.value++;
    }
    
    /**
     * Resets the counter to zero.
     */
    
    public synchronized void reset()
    {
        this.value = 0;
    }
    
    /**
     * Reads the current state of the counter.
     * 
     * @return the current state of the counter
     */

    public synchronized long read()
    {
        return this.value;
    }
    
}
