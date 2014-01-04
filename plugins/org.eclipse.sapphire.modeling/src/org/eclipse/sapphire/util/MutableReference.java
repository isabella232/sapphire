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

package org.eclipse.sapphire.util;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ListenerContext;

/**
 * An object reference that can be changed after creation. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class MutableReference<T>
{
    private T value;
    private ListenerContext listeners;
   
    public MutableReference()
    {
        this( null );
    }
    
    public MutableReference( final T value )
    {
        this.value = value;
    }
    
    public synchronized T get() 
    { 
        return this.value;
    }
    
    public synchronized void set( final T value ) 
    { 
        final T oldValue = this.value;
        this.value = value;
        
        if( this.listeners != null )
        {
            this.listeners.broadcast( new ReferenceChangedEvent( oldValue, value ) );
        }
    }
    
    public synchronized boolean attach( final Listener listener )
    {
        if( this.listeners == null )
        {
            this.listeners = new ListenerContext();
        }
        
        return this.listeners.attach( listener );
    }
    
    public synchronized boolean detach( final Listener listener )
    {
        if( this.listeners == null )
        {
            this.listeners = new ListenerContext();
        }
        
        return this.listeners.detach( listener );
    }
    
    public final class ReferenceChangedEvent extends Event
    {
        private final T before;
        private final T after;
        
        private ReferenceChangedEvent( final T before,
                                       final T after )
        {
            this.before = before;
            this.after = after;
        }
        
        public T before()
        {
            return this.before;
        }
        
        public T after()
        {
            return this.after;
        }
    }

}
