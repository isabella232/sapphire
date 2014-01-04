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

import org.eclipse.sapphire.modeling.Status;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PropertyValidationEvent extends PropertyEvent
{
    private final Status before;
    private final Status after;
    
    PropertyValidationEvent( final Property property, final Status before, final Status after )
    {
        super( property );
        
        this.before = before;
        this.after = after;
    }
    
    public Status before()
    {
        return this.before;
    }
    
    public Status after()
    {
        return this.after;
    }
    
    @Override
    public Map<String,String> fillTracingInfo( final Map<String,String> info )
    {
        super.fillTracingInfo( info );
        
        info.put( "before", before().toString() );
        info.put( "after", after().toString() );
        
        return info;
    }

}
