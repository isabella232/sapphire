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

import static org.eclipse.sapphire.modeling.util.MiscUtil.equal;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DerivedValueServiceData
{
    private final String value;
    
    public DerivedValueServiceData( final String value )
    {
        this.value = value;
    }
    
    public String value()
    {
        return this.value;
    }
    
    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof DerivedValueServiceData )
        {
            final DerivedValueServiceData data = (DerivedValueServiceData) obj;
            return equal( this.value, data.value );
        }
        
        return false;
    }
    
    @Override
    public int hashCode()
    {
        return this.value.hashCode();
    }
    
}
