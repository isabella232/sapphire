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

package org.eclipse.sapphire.services;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class EnablementServiceData
{
    private final boolean enablement;
    
    public EnablementServiceData( final boolean enablement )
    {
        this.enablement = enablement;
    }
    
    public boolean enablement()
    {
        return this.enablement;
    }
    
    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof EnablementServiceData )
        {
            final EnablementServiceData data = (EnablementServiceData) obj;
            return ( this.enablement == data.enablement );
        }
        
        return false;
    }
    
    @Override
    public int hashCode()
    {
        return ( this.enablement ? 1 : 0 );
    }
    
}
