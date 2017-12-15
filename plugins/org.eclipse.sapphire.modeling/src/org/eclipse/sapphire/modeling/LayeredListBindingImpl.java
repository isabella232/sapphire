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

package org.eclipse.sapphire.modeling;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class LayeredListBindingImpl

    extends ListBindingImpl
    
{
    private final IdentityCache<Object,Resource> cache = new IdentityCache<Object,Resource>();

    @Override
    public final List<Resource> read()
    {
        this.cache.track();

        final List<Resource> list = new ArrayList<Resource>();
        
        for( Object obj : readUnderlyingList() )
        {
            Resource resource = this.cache.get( obj );
            
            if( resource == null )
            {
                resource = createResource( obj );
                this.cache.put( obj, resource );
            }
            
            list.add( resource );
        }
        
        this.cache.purge();
        
        return list;
    }
    
    protected abstract List<?> readUnderlyingList();
    
    @Override
    public final Resource add( final ModelElementType type )
    {
        final Object obj = addUnderlyingObject( type );
        final Resource resource = createResource( obj );
        
        this.cache.put( obj, resource );
        
        return resource;
    }
    
    protected Object addUnderlyingObject( final ModelElementType type )
    {
        throw new UnsupportedOperationException();
    }
    
    protected abstract Resource createResource( Object obj );

}
