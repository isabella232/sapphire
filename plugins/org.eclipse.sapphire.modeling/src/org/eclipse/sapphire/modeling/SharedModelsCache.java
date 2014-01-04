/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Resource;

/**
 * <p>Facility for managing shared model instances. Note that no exclusivity of access guarantees are made, so this
 * cache should only be used by code needing read-only access to a model. Code that needs to modify the model
 * should create it's own private model instance for the duration of the change operation.</p> 
 * 
 * <p>Models are stored in this cache using a key object. The only requirement on the key is that it correctly
 * implements hashCode() and equals() methods. For many cases the supplied StandardKey class makes a good
 * key. It combines ResourceStore (which identifies a file or another storage medium) and ElementType.</p>
 * 
 * <p>The cache is managed in a memory-sensitive manner. In particular, there is no guarantee that a retrieve
 * call will return a non-null result even if you know that the particular model was stored in the cache. If 
 * memory gets tight and there are no other references to the model in question, the model held in the cache
 * will be garbage collected.</p>
 * 
 * <p>The cache will never return a model whose resource reports that it is out of date. Models that are not
 * backed by a resource are never considered to be out of date.</p>
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SharedModelsCache
{
    private static final Map<Object,SoftReference<Element>> cache = new HashMap<Object,SoftReference<Element>>();
    
    public static synchronized Element retrieve( final Object key )
    {
        final SoftReference<Element> cachedModelRef = cache.get( key );
        
        if( cachedModelRef != null )
        {
            final Element model = cachedModelRef.get();
            
            if( model != null )
            {
                final Resource resource = model.resource();
                
                if( resource == null || ! resource.isOutOfDate() )
                {
                    return model;
                }
            }
            
            cache.remove( key );
        }
        
        return null;
    }
    
    public static synchronized void store( final Object key,
                                           final Element modelElement )
    {
        cache.put( key, new SoftReference<Element>( modelElement ) );
    }
    
    public static final class StandardKey
    {
        private final ResourceStore resourceStore;
        private final ElementType modelElementType;
        
        public StandardKey( final ResourceStore resourceStore,
                            final ElementType modelElementType )
        {
            this.resourceStore = resourceStore;
            this.modelElementType = modelElementType;
        }
        
        @Override
        public int hashCode()
        {
            return this.resourceStore.hashCode() ^ this.modelElementType.hashCode();
        }
        
        @Override
        public boolean equals( final Object obj )
        {
            if( obj instanceof StandardKey )
            {
                final StandardKey key = (StandardKey) obj;
                return this.resourceStore.equals( key.resourceStore ) && this.modelElementType.equals( key.modelElementType );
            }
            
            return false;
        }
    }
    
}
