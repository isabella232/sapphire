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

package org.eclipse.sapphire.services.internal;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementImpl;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ElementInstanceServiceContext extends ElementServiceContext
{
    private final Element element;
    
    public ElementInstanceServiceContext( final Element element )
    {
        super( ID_ELEMENT_INSTANCE, element.type().services(), element.type(), element.root(), ( (ElementImpl) element ).queue() );
        
        this.element = element;
    }
    
    @Override
    @SuppressWarnings( "unchecked" )
    
    public <T> T find( final Class<T> type )
    {
        T obj = super.find( type );
        
        if( obj == null )
        {
            if( type == Element.class )
            {
                obj = (T) this.element;
            }
            else
            {
                obj = this.element.nearest( type );
            }
        }
        
        return obj;
    }
    
}
