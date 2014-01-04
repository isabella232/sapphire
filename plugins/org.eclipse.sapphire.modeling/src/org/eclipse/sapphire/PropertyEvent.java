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

package org.eclipse.sapphire;

import java.util.Map;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class PropertyEvent extends Event
{
    private final Property property;
    
    PropertyEvent( final Property property )
    {
        this.property = property;
    }
    
    public final Property property()
    {
        return this.property;
    }
    
    @Override
    public Map<String,String> fillTracingInfo( final Map<String,String> info )
    {
        super.fillTracingInfo( info );
        
        info.put( "element", property().element().type().getQualifiedName() + '(' + System.identityHashCode( property().element() ) + ')' );
        info.put( "property", property().name() );
        
        return info;
    }
    
}
