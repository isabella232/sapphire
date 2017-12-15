/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ModelService
{
    private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
    private IModelElement element;
    
    protected final void init( final IModelElement element )
    {
        this.element = element;
    }
    
    public final IModelElement element()
    {
        return this.element;
    }
    
    public final <T> T nearest( final Class<T> particleType )
    {
        return this.element.nearest( particleType );
    }

    public final <A> A adapt( final Class<A> adapterType )
    {
        return this.element.adapt( adapterType );
    }
    
    public final void addListener( final Listener listener )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.listeners.add( listener );
    }
    
    public final void removeListener( final Listener listener )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.listeners.remove( listener );
    }
    
    protected final void notifyListeners( final Event event )
    {
        for( Listener listener : this.listeners )
        {
            try
            {
                listener.handleEvent( event );
            }
            catch( Exception e )
            {
                LoggingService.log( e );
            }
        }
    }
    
    public void dispose()
    {
    }
    
    public static class Event
    {
        private final ModelService service;
        
        public Event( final ModelService service )
        {
            this.service = service;
        }
        
        public ModelService service()
        {
            return this.service;
        }
    }
    
    public static abstract class Listener
    {
        public abstract void handleEvent( Event event );
    }
    
}
