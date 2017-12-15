/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.util;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * An object reference that can be changed after creation. 
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class MutableReference<T>
{
    private T value;
    private final Set<Listener> listeners;
   
    public MutableReference()
    {
        this( null );
    }
    
    public MutableReference( final T value )
    {
        this.value = value;
        this.listeners = new CopyOnWriteArraySet<Listener>();
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
        
        notifyListeners( oldValue, value );
    }
    
    public void addListener( final Listener listener )
    {
        this.listeners.add( listener );
    }
    
    public void removeListener( final Listener listener )
    {
        this.listeners.remove( listener );
    }
    
    private void notifyListeners( final T oldValue,
                                  final T newValue )
    {
        for( Listener listener : this.listeners )
        {
            listener.handleReferenceChanged( oldValue, newValue );
        }
    }
    
    public static abstract class Listener
    {
        public abstract void handleReferenceChanged( final Object oldValue,
                                                     final Object newValue );
    }

}
