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

import java.util.Map;

import org.eclipse.sapphire.Event;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ElementEvent extends Event
{
    private final IModelElement element;
    
    protected ElementEvent( final IModelElement element )
    {
        this.element = element;
    }
    
    public IModelElement element()
    {
        return this.element;
    }
    
    @Override
    public Map<String,String> fillTracingInfo( final Map<String,String> info )
    {
        super.fillTracingInfo( info );
        
        info.put( "element", element().type().getQualifiedName() + '(' + System.identityHashCode( element() ) + ')' );
        
        return info;
    }
    
}
