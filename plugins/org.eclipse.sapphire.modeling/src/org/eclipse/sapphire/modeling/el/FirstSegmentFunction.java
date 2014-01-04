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

package org.eclipse.sapphire.modeling.el;

import java.util.StringTokenizer;

/**
 * Breaks a string into segments using the provided separators and returns the first
 * segment. If no separators are found in the string, the entire string is returned.
 * This function takes two string operands. The first is the string to tokenize. The
 * second is a sequence of characters that individually should be treated as
 * valid separators.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class FirstSegmentFunction extends Function
{
    @Override
    public String name()
    {
        return "FirstSegment";
    }

    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            @Override
            protected Object evaluate()
            {
                final String str = cast( operand( 0 ), String.class );
                final String separators = cast( operand( 1 ), String.class );
                
                final StringTokenizer tokenizer = new StringTokenizer( str, separators );
                
                return ( tokenizer.hasMoreTokens() ? tokenizer.nextToken() : str );
            }
        };
    }
    
}
