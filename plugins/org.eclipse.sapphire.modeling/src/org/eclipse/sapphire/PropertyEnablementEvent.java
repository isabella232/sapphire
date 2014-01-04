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

public final class PropertyEnablementEvent extends PropertyEvent
{
    private final boolean before;
    private final boolean after;
    
    PropertyEnablementEvent( final Property property, final boolean before, final boolean after )
    {
        super( property );
        
        this.before = before;
        this.after = after;
    }
    
    public boolean before()
    {
        return this.before;
    }
    
    public boolean after()
    {
        return this.after;
    }
    
    @Override
    public Map<String,String> fillTracingInfo( final Map<String,String> info )
    {
        super.fillTracingInfo( info );
        
        info.put( "before", Boolean.toString( before() ) );
        info.put( "after", Boolean.toString( after() ) );
        
        return info;
    }

}
