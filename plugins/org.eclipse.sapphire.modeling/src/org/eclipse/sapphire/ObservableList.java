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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * An implementation of {@link List} that is {@link Observable} using listeners.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ObservableList<E> extends AbstractList<E> implements Observable
{
    private final List<E> base;
    private ListenerContext listeners;
    
    public ObservableList()
    {
        this( null );
    }
    
    public ObservableList( final List<E> base )
    {
        this.base = ( base == null ? new ArrayList<E>() : base );
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
    public int indexOf( final Object obj )
    {
        synchronized( this )
        {
            return this.base.indexOf( obj );
        }
    }

    @Override
    public int lastIndexOf( final Object obj )
    {
        synchronized( this )
        {
            return this.base.lastIndexOf( obj );
        }
    }

    @Override
    public Iterator<E> iterator()
    {
        synchronized( this )
        {
            return new Iterator<E>()
            {
                private final Iterator<E> base = ObservableList.this.base.iterator();
    
                public boolean hasNext()
                {
                    synchronized( ObservableList.this )
                    {
                        return this.base.hasNext();
                    }
                }
    
                public E next()
                {
                    synchronized( ObservableList.this )
                    {
                        return this.base.next();
                    }
                }
    
                public void remove()
                {
                    synchronized( ObservableList.this )
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
    public E get( final int index )
    {
        synchronized( this )
        {
            return this.base.get( index );
        }
    }

    @Override
    public E set( final int index, final E entry )
    {
        final E previous;
        final boolean modified;
        
        synchronized( this )
        {
            previous = this.base.set( index, entry );
            modified = ( previous != entry );
        }
        
        if( modified )
        {
            broadcast();
        }
        
        return previous;
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
    public void add( final int index, final E entry )
    {
        synchronized( this )
        {
            this.base.add( index, entry );
        }
        
        broadcast();
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
    public boolean addAll( final int index, final Collection<? extends E> collection )
    {
        final boolean modified;
        
        synchronized( this )
        {
            modified = this.base.addAll( index, collection );
        }
        
        if( modified )
        {
            broadcast();
        }
        
        return modified;
    }

    @Override
    public E remove( final int index )
    {
        final E removed;
        
        synchronized( this )
        {
            removed = this.base.remove( index );
        }
        
        broadcast();
        
        return removed;
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
