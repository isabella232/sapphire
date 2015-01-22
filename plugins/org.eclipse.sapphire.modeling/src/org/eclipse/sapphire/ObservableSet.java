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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * An implementation of {@link Set} that is {@link Observable} using listeners.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ObservableSet<E> implements Set<E>, Observable
{
    private final Set<E> base;
    private ListenerContext listeners;
    
    public ObservableSet()
    {
        this( null );
    }
    
    public ObservableSet( final Set<E> base )
    {
        this.base = ( base == null ? new HashSet<E>() : base );
    }

    @Override
    public void attach( final Listener listener )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( this )
        {
            if( this.listeners == null )
            {
                this.listeners = new ListenerContext();
            }
            
            this.listeners.attach( listener );
        }
    }

    @Override
    public void detach( final Listener listener )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( this )
        {
            if( this.listeners != null )
            {
                this.listeners.detach( listener );
            }
        }
    }
    
    private void broadcast()
    {
        final ListenerContext listeners;
        
        synchronized( this )
        {
            listeners = this.listeners;
        }
        
        if( listeners != null )
        {
            listeners.broadcast( new Event() );
        }
    }

    @Override
    public int size()
    {
        synchronized( this )
        {
            return this.base.size();
        }
    }

    @Override
    public boolean isEmpty()
    {
        synchronized( this )
        {
            return this.base.isEmpty();
        }
    }

    @Override
    public boolean contains( final Object obj )
    {
        synchronized( this )
        {
            return this.base.contains( obj );
        }
    }

    @Override
    public boolean containsAll( final Collection<?> collection )
    {
        synchronized( this )
        {
            return this.base.containsAll( collection );
        }
    }

    @Override
    public Iterator<E> iterator()
    {
        synchronized( this )
        {
            return new Iterator<E>()
            {
                private final Iterator<E> base = ObservableSet.this.base.iterator();
    
                public boolean hasNext()
                {
                    synchronized( ObservableSet.this )
                    {
                        return this.base.hasNext();
                    }
                }
    
                public E next()
                {
                    synchronized( ObservableSet.this )
                    {
                        return this.base.next();
                    }
                }
    
                public void remove()
                {
                    synchronized( ObservableSet.this )
                    {
                        this.base.remove();
                        broadcast();
                    }
                }
            };
        }
    }

    @Override
    public Object[] toArray()
    {
        synchronized( this )
        {
            return this.base.toArray();
        }
    }

    @Override
    public <T> T[] toArray( final T[] array )
    {
        synchronized( this )
        {
            return this.base.toArray( array );
        }
    }

    @Override
    public boolean add( final E entry )
    {
        final boolean modified;
        
        synchronized( this )
        {
            modified = this.base.add( entry );
        }
        
        if( modified )
        {
            broadcast();
        }
        
        return modified;
    }

    @Override
    public boolean addAll( Collection<? extends E> collection )
    {
        final boolean modified;
        
        synchronized( this )
        {
            modified = this.base.addAll( collection );
        }
        
        if( modified )
        {
            broadcast();
        }
        
        return modified;
    }

    @Override
    public boolean remove( final Object obj )
    {
        final boolean modified;
        
        synchronized( this )
        {
            modified = this.base.remove( obj );
        }
        
        if( modified )
        {
            broadcast();
        }
        
        return modified;
    }

    @Override
    public boolean removeAll( final Collection<?> collection )
    {
        final boolean modified;
        
        synchronized( this )
        {
            modified = this.base.removeAll( collection );
        }
        
        if( modified )
        {
            broadcast();
        }
        
        return modified;
    }

    @Override
    public boolean retainAll( final Collection<?> collection )
    {
        final boolean modified;
        
        synchronized( this )
        {
            modified = this.base.retainAll( collection );
        }
        
        if( modified )
        {
            broadcast();
        }
        
        return modified;
    }

    @Override
    public void clear()
    {
        boolean broadcast = false;
        
        synchronized( this )
        {
            if( ! this.base.isEmpty() )
            {
                broadcast = true;
            }
            
            this.base.clear();
        }
        
        if( broadcast )
        {
            broadcast();
        }
    }

}
