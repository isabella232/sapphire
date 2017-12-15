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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ReadOnlySetFactory<E>
{
    private E firstElement = null;
    private Set<E> set = null;
    private boolean exported = false;
    
    private ReadOnlySetFactory() {}
    
    public static <E> ReadOnlySetFactory<E> create()
    {
        return new ReadOnlySetFactory<E>();
    }
    
    public static <E> Set<E> create( final E element )
    {
        return Collections.singleton( element );
    }
    
    public static <E> Set<E> create( final E... elements )
    {
        return ReadOnlySetFactory.<E>create().add( elements ).export();
    }
    
    public static <E> Set<E> create( final Collection<E> elements )
    {
        return ReadOnlySetFactory.<E>create().add( elements ).export();
    }

    public ReadOnlySetFactory<E> add( final E element )
    {
        if( this.exported )
        {
            throw new IllegalStateException();
        }
        
        if( this.set != null )
        {
            this.set.add( element );
        }
        else if( this.firstElement != null )
        {
            this.set = new HashSet<E>();
            this.set.add( this.firstElement );
            this.set.add( element );
            this.firstElement = null;
        }
        else
        {
            this.firstElement = element;
        }
        
        return this;
    }
    
    public ReadOnlySetFactory<E> add( final E... elements )
    {
        for( E element : elements )
        {
            add( element );
        }
        
        return this;
    }
    
    public ReadOnlySetFactory<E> add( final Collection<E> elements )
    {
        for( E element : elements )
        {
            add( element );
        }
        
        return this;
    }
    
    public Set<E> export()
    {
        if( this.exported )
        {
            throw new IllegalStateException();
        }
        
        this.exported = true;
        
        if( this.set != null )
        {
            return Collections.unmodifiableSet( this.set );
        }
        else if( this.firstElement != null )
        {
            return Collections.singleton( this.firstElement );
        }
        else
        {
            return Collections.emptySet();
        }
    }
    
}
