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

package org.eclipse.sapphire.ui.forms;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.sapphire.util.ListFactory;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class MasterDetailsContentNodeList implements List<MasterDetailsContentNodePart>
{
     private final List<MasterDetailsContentNodePart> nodes;
    
    public MasterDetailsContentNodeList( final List<MasterDetailsContentNodePart> nodes )
    {
        this.nodes = nodes;
    }
    
    public List<MasterDetailsContentNodePart> visible()
    {
        final ListFactory<MasterDetailsContentNodePart> visible = ListFactory.start();
        
        for( MasterDetailsContentNodePart node : this.nodes )
        {
            if( node.visible() )
            {
                visible.add( node );
            }
        }
        
        return visible.result();
    }

    public int size()
    {
        return this.nodes.size();
    }

    public boolean isEmpty()
    {
        return this.nodes.isEmpty();
    }

    public boolean contains( final Object object )
    {
        return this.nodes.contains( object );
    }

    public Iterator<MasterDetailsContentNodePart> iterator()
    {
        return this.nodes.iterator();
    }

    public Object[] toArray()
    {
        return this.nodes.toArray();
    }

    public <T> T[] toArray( final T[] array )
    {
        return this.nodes.toArray( array );
    }

    public boolean add( final MasterDetailsContentNodePart object )
    {
        throw new UnsupportedOperationException();
    }

    public boolean remove( final Object object )
    {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll( final Collection<?> collection )
    {
        return this.nodes.containsAll( collection );
    }

    public boolean addAll( final Collection<? extends MasterDetailsContentNodePart> collection )
    {
        throw new UnsupportedOperationException();
    }

    public boolean addAll( final int index,
                           final Collection<? extends MasterDetailsContentNodePart> collection )
    {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll( final Collection<?> collection )
    {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll( final Collection<?> collection )
    {
        throw new UnsupportedOperationException();
    }

    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    public MasterDetailsContentNodePart get( final int index )
    {
        return this.nodes.get( index );
    }

    public MasterDetailsContentNodePart set( final int index,
                                         final MasterDetailsContentNodePart element )
    {
        throw new UnsupportedOperationException();
    }

    public void add( final int index,
                     final MasterDetailsContentNodePart element )
    {
        throw new UnsupportedOperationException();
    }

    public MasterDetailsContentNodePart remove( final int index )
    {
        throw new UnsupportedOperationException();
    }

    public int indexOf( final Object object )
    {
        return this.nodes.indexOf( object );
    }

    public int lastIndexOf( final Object object )
    {
        return this.nodes.lastIndexOf( object );
    }

    public ListIterator<MasterDetailsContentNodePart> listIterator()
    {
        return this.nodes.listIterator();
    }

    public ListIterator<MasterDetailsContentNodePart> listIterator( final int index )
    {
        return this.nodes.listIterator( index );
    }

    public List<MasterDetailsContentNodePart> subList( final int fromIndex,
                                                   final int toIndex )
    {
        return this.nodes.subList( fromIndex, toIndex );
    }
    
    @Override
    public int hashCode()
    {
        return this.nodes.hashCode();
    }

    @Override
    public boolean equals( final Object object )
    {
        return this.nodes.equals( object );
    }

    @Override
    public String toString()
    {
        return this.nodes.toString();
    }

}
