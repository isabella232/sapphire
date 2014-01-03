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

package org.eclipse.sapphire.modeling.el.internal;

import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;

/**
 * Replaces all occurrences of a regular expression pattern with the provided replacement text. The full semantics
 * are specified by Java's {@link String#replaceAll( String, String )} function.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ReplaceFunction extends Function
{
    @Override
    public String name()
    {
        return "Replace";
    }

    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            @Override
            protected Object evaluate()
            {
                final String string = cast( operand( 0 ), String.class );
                final String pattern = cast( operand( 1 ), String.class );
                final String replacement = cast( operand( 2 ), String.class );
                
                return string.replaceAll( pattern, replacement );
            }
        };
    }
    
}
