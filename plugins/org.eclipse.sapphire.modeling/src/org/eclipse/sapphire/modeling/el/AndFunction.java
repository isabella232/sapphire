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

package org.eclipse.sapphire.modeling.el;

/**
 * Logical AND function. 
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class AndFunction

    extends Function

{
    public static AndFunction create( final Function a,
                                      final Function b )
    {
        final AndFunction function = new AndFunction();
        function.init( a, b );
        return function;
    }

    @Override
    public String name()
    {
        return "&&";
    }

    @Override
    public boolean operator()
    {
        return true;
    }

    @Override
    public int precedence()
    {
        return 6;
    }

    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            @Override
            protected Object evaluate()
            {
                final boolean a = cast( operand( 0 ).value(), Boolean.class );
                final boolean b = cast( operand( 1 ).value(), Boolean.class );
                return ( a && b );
            }
        };
    }

}
