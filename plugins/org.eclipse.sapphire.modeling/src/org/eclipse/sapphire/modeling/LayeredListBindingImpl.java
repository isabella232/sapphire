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

package org.eclipse.sapphire.modeling;

import java.util.List;

import org.eclipse.sapphire.util.IdentityCache;
import org.eclipse.sapphire.util.ReadOnlyListFactory;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class LayeredListBindingImpl extends ListBindingImpl
{
    private final IdentityCache<Object,Resource> cache = new IdentityCache<Object,Resource>();

    @Override
    public final List<Resource> read()
    {
        this.cache.track();

        final ReadOnlyListFactory<Resource> list = ReadOnlyListFactory.create();
        
        for( Object obj : readUnderlyingList() )
        {
            Resource resource = this.cache.get( obj );
            
            if( resource == null )
            {
                resource = resource( obj );
                this.cache.put( obj, resource );
            }
            
            list.add( resource );
        }
        
        this.cache.purge();
        
        return list.export();
    }
    
    protected abstract List<?> readUnderlyingList();
    
    @Override
    public final Resource insert( final ModelElementType type,
                                  final int position )
    {
        final Object obj = insertUnderlyingObject( type, position );
        
        // Check the cache first before creating a new resource, because insertUnderlyingObject may have
        // caused a re-entrant call to read() and so the resource for the inserted underlying object may
        // have already been created.
        
        Resource resource = this.cache.get( obj );
        
        if( resource == null )
        {
            resource = resource( obj );
            this.cache.put( obj, resource );
        }
        
        return resource;
    }
    
    protected Object insertUnderlyingObject( final ModelElementType type,
                                             final int position )
    {
        throw new UnsupportedOperationException();
    }
    
    protected abstract Resource resource( Object obj );

}
