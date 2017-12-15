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

package org.eclipse.sapphire.util;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ListenerContext;

/**
 * An object reference that can be changed after creation. 
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class MutableReference<T>
{
    private T value;
    private final ListenerContext listeners = new ListenerContext();
   
    public MutableReference()
    {
        this( null );
    }
    
    public MutableReference( final T value )
    {
        this.value = value;
    }
    
    public T get() 
    { 
        synchronized( this )
        {
            return this.value;
        }
    }
    
    public void set( final T value ) 
    { 
        final T oldValue;
        
        synchronized( this )
        {
            oldValue = this.value;
            this.value = value;
        }
        
        this.listeners.broadcast( new ReferenceChangedEvent( oldValue, value ) );
    }
    
    public boolean attach( final Listener listener )
    {
        return this.listeners.attach( listener );
    }
    
    public boolean detach( final Listener listener )
    {
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
