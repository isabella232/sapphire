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

package org.eclipse.sapphire;

import java.util.Map;

/**
 * The event that's broadcast when the content of a value property changes.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class ValuePropertyContentEvent extends PropertyContentEvent
{
    private final String before;
    private final String after;
    private final boolean refactor;
    
    ValuePropertyContentEvent( final Value<?> property, final String before, final String after, final boolean refactor )
    {
        super( property );
        
        this.before = before;
        this.after = after;
        this.refactor = refactor;
    }
    
    /**
     * Returns the property content before the change.
     */
    
    public String before()
    {
        return this.before;
    }
    
    /**
     * Returns the property content after the change.
     */
    
    public String after()
    {
        return this.after;
    }
    
    /**
     * Indicates whether the originating property change was flagged to allow refactoring. If true is returned, any listeners
     * can take action accordingly. For example, a ReferenceService implementation can update the reference when the target changes.
     */
    
    public boolean refactor()
    {
        return this.refactor;
    }
    
    @Override
    public Map<String,String> fillTracingInfo( final Map<String,String> info )
    {
        super.fillTracingInfo( info );
        
        info.put( "before", this.before );
        info.put( "after", this.after );
        info.put( "refactor", String.valueOf( this.refactor ) );
        
        return info;
    }
    
}
