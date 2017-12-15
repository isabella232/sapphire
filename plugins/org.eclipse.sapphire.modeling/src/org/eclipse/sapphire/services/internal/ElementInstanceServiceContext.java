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

package org.eclipse.sapphire.services.internal;

import org.eclipse.sapphire.modeling.IModelElement;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ElementInstanceServiceContext extends ElementServiceContext
{
    private final IModelElement element;
    
    public ElementInstanceServiceContext( final IModelElement element )
    {
        super( ID_ELEMENT_INSTANCE, element.getModelElementType().services(), element.getModelElementType() );
        
        this.element = element;
    }
    
    @Override
    @SuppressWarnings( "unchecked" )
    
    public <T> T find( final Class<T> type )
    {
        T obj = super.find( type );
        
        if( obj == null )
        {
            if( type == IModelElement.class )
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
