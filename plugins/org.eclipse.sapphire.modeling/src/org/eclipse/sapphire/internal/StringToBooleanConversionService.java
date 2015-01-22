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

package org.eclipse.sapphire.internal;

import org.eclipse.sapphire.ConversionService;

/**
 * ConversionService implementation for String to Boolean conversions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StringToBooleanConversionService extends ConversionService<String,Boolean>
{
    public StringToBooleanConversionService()
    {
        super( String.class, Boolean.class );
    }

    @Override
    public Boolean convert( final String string )
    {
        Boolean result = null;
        
        if( string.equalsIgnoreCase( Boolean.TRUE.toString() ) )
        {
            result = Boolean.TRUE;
        }
        else if( string.equalsIgnoreCase( Boolean.FALSE.toString() ) )
        {
            result = Boolean.FALSE;
        }
        
        return result;
    }

}
