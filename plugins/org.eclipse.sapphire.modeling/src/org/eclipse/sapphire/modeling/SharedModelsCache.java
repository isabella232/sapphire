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

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SharedModelsCache
{
    private static final Map<Key,SoftReference<IModel>> cache = new HashMap<Key,SoftReference<IModel>>();
    
    public static synchronized IModel retrieve( final ModelStore modelStore,
                                                final ModelElementType modelType )
    {
        final Key key = new Key( modelStore, modelType );
        final SoftReference<IModel> cachedModelRef = cache.get( key );
        
        if( cachedModelRef != null )
        {
            final IModel model = cachedModelRef.get();
            
            if( model != null )
            {
                if( ! model.getModelStore().isOutOfDate() )
                {
                    return model;
                }
            }
            
            cache.remove( key );
        }
        
        return null;
    }
    
    public static synchronized void store( final IModel model )
    {
        final Key key = new Key( model.getModelStore(), model.getModelElementType() );
        cache.put( key, new SoftReference<IModel>( model ) );
    }
    
    private static final class Key
    {
        private final ModelStore modelStore;
        private final ModelElementType modelType;
        
        public Key( final ModelStore modelStore,
                    final ModelElementType modelType )
        {
            this.modelStore = modelStore;
            this.modelType = modelType;
        }
        
        @Override
        public int hashCode()
        {
            return this.modelStore.hashCode() ^ this.modelType.hashCode();
        }
        
        @Override
        public boolean equals( final Object obj )
        {
            if( obj instanceof Key )
            {
                final Key key = (Key) obj;
                return this.modelStore.equals( key.modelStore ) && this.modelType.equals( key.modelType );
            }
            
            return false;
        }
    }
    
}
