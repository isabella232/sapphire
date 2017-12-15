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

import org.eclipse.sapphire.modeling.serialization.ValueSerializerImpl;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class FloatSerializer

    extends ValueSerializerImpl<Float>
    
{
    @Override
    protected Float decodeFromString( final String value )
    {
        Float result = null;
        
        try
        {
            result = Float.valueOf( value );
        }
        catch( NumberFormatException e )
        {
            // Intentionally ignored. It is not the job of serializer to report these
            // problems. That's handled by validators.
        }
        
        return result;
    }
    
}
