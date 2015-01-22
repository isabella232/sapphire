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

package org.eclipse.sapphire.ui.def.internal;

import org.eclipse.sapphire.ConversionService;
import org.eclipse.sapphire.ui.def.SapphireKeySequence;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StringToKeySequenceConversionService extends ConversionService<String,SapphireKeySequence>
{
    public StringToKeySequenceConversionService()
    {
        super( String.class, SapphireKeySequence.class );
    }

    @Override
    public SapphireKeySequence convert( final String string )
    {
        SapphireKeySequence result = null;
        
        try
        {
            return new SapphireKeySequence( string );
        }
        catch( IllegalArgumentException e )
        {
            // Intentionally ignored.
        }
        
        return result;
    }

}
