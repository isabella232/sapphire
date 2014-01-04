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

import java.util.Map;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class ElementEvent extends Event
{
    private final Element element;
    
    protected ElementEvent( final Element element )
    {
        this.element = element;
    }
    
    public Element element()
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
