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
import org.eclipse.sapphire.VersionConstraint;

/**
 * ConversionService implementation for String to VersionConstraint conversions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StringToVersionConstraintConversionService extends ConversionService<String,VersionConstraint>
{
    public StringToVersionConstraintConversionService()
    {
        super( String.class, VersionConstraint.class );
    }

    @Override
    public VersionConstraint convert( final String string )
    {
        VersionConstraint result = null;
        
        try
        {
            result = new VersionConstraint( string );
        }
        catch( IllegalArgumentException e )
        {
            // Intentionally ignored.
        }
        
        return result;
    }

}
