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

package org.eclipse.sapphire.services.internal;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListenerContext;
import org.eclipse.sapphire.Property;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PropertyInstanceServiceContext extends PropertyServiceContext
{
    private final Property property;
    
    public PropertyInstanceServiceContext( final Property instance,
                                           final ListenerContext listenerContextForCoordination )
    {
        super( ID_PROPERTY_INSTANCE, instance.definition().services(), instance.definition(), instance.root() );
        
        this.property = instance;
        
        if( listenerContextForCoordination != null )
        {
            coordinate( listenerContextForCoordination );
        }
    }
    
    @Override
    public <T> T find( final Class<T> type )
    {
        T obj = super.find( type );
        
        if( obj == null )
        {
            if( type.isInstance( this.property ) )
            {
                obj = type.cast( this.property );
            }
            else if( type == ElementType.class )
            {
                obj = type.cast( this.property.element().type() );
            }
            else if( type == Element.class )
            {
                obj = type.cast( this.property.element() );
            }
            else
            {
                obj = this.property.element().nearest( type );
            }
        }
        
        return obj;
    }
    
}
