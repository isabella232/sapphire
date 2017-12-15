/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.services;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyContentEvent;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ReferenceService<T> extends DataService<T>
{
    @Override
    protected final void initDataService()
    {
        context( Property.class ).attach
        (
            new FilteredListener<PropertyContentEvent>()
            {
                @Override
                protected void handleTypedEvent( final PropertyContentEvent event )
                {
                    refresh();
                }
            }
        );
        
        initReferenceService();
    }
    
    protected void initReferenceService()
    {
    }
    
    public final T target()
    {
        return data();
    }
    
    /**
     * Returns the key that can be used to reference the provided object. The default implementation
     * throws UnsupportedOperationException.
     * 
     * @param object the object
     * @return the key
     * @throws UnsupportedOperationException if this service does not support key extraction
     * @throws IllegalArgumentException if the object is not a valid target for this reference
     */
    
    public String reference( final T object )
    {
        throw new UnsupportedOperationException();
    }

}
