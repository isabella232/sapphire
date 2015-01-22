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

package org.eclipse.sapphire.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;

/**
 * Identity hash set implementation based on IdentityHashMap.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class IdentityHashSet<T> extends AbstractSet<T>
{
    private final IdentityHashMap<T,Object> map;
    
    public IdentityHashSet()
    {
        this.map = new IdentityHashMap<T,Object>();
    }
    
    public IdentityHashSet( final Collection<? extends T> set )
    {
        this();
        
        for( T item : set )
        {
            add( item );
        }
    }

    @Override
    public boolean add( final T item )
    {
        this.map.put( item, null );
        return true;
    }

    @Override
    public Iterator<T> iterator()
    {
        return this.map.keySet().iterator();
    }

    @Override
    public int size()
    {
        return this.map.size();
    }
    
}
