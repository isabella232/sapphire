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

package org.eclipse.sapphire.modeling.el;

/**
 * Concatenates two or more strings into a single string. Particularly useful in 
 * contexts where composite expressions cannot be used, such as where the result of 
 * concatenation needs to feed into another function or operator. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ConcatFunction

    extends Function

{
    public static ConcatFunction create( final FunctionContext context,
                                         final Function a,
                                         final Function b )
    {
        final ConcatFunction function = new ConcatFunction();
        function.init( context, a, b );
        return function;
    }

    public static ConcatFunction create( final FunctionContext context,
                                         final String a,
                                         final String b )
    {
        return create( context, Literal.create( context, a ), Literal.create( context, b ) );
    }

    @Override
    protected String evaluate()
    {
        final StringBuilder buf = new StringBuilder();
        
        for( Function operand : operands() )
        {
            buf.append( cast( operand.value(), String.class ) );
        }
        
        return buf.toString();
    }

}
