/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.el.internal;

import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.TypeCast;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StringToVersionTypeCast extends TypeCast
{
    @Override
    public boolean applicable( final FunctionContext context,
                               final Function requestor,
                               final Object value,
                               final Class<?> target )
    {
        return ( target == Version.class && value instanceof String );
    }

    @Override
    public Object evaluate( final FunctionContext context,
                            final Function requestor,
                            final Object value,
                            final Class<?> target )
    {
        try
        {
            return new Version( (String) value );
        }
        catch( IllegalArgumentException e )
        {
            return null;
        }
    }

}
