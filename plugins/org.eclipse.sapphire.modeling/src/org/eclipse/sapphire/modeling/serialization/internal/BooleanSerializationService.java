/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.serialization.internal;

import org.eclipse.sapphire.modeling.serialization.ValueSerializationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class BooleanSerializationService

    extends ValueSerializationService
    
{
    @Override
    protected Boolean decodeFromString( final String value )
    {
        Boolean result = null;
        
        if( value.equalsIgnoreCase( Boolean.TRUE.toString() ) )
        {
            return Boolean.TRUE;
        }
        else if( value.equalsIgnoreCase( Boolean.FALSE.toString() ) )
        {
            return Boolean.FALSE;
        }
        
        return result;
    }
    
}
