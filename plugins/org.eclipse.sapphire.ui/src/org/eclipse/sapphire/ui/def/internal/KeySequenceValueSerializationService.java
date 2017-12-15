/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.def.internal;

import org.eclipse.sapphire.services.ValueSerializationService;
import org.eclipse.sapphire.ui.def.SapphireKeySequence;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class KeySequenceValueSerializationService

    extends ValueSerializationService

{
    @Override
    protected SapphireKeySequence decodeFromString( final String value )
    {
        try
        {
            return new SapphireKeySequence( value );
        }
        catch( IllegalArgumentException e )
        {
            // The value serializer infrastructure treats null decode results as serialization
            // failure. Exceptions during decoding are not to be propagated.
        }

        return null;
    }
    
}
