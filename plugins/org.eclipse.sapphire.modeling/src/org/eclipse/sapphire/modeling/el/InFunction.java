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

import java.util.List;

/**
 * In function. 
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class InFunction

    extends Function

{
    public static InFunction create( final Function a,
                                     final Function b )
    {
        final InFunction function = new InFunction();
        function.init( a, b );
        return function;
    }
    
    @Override
    public String name()
    {
        return "in";
    }

    @Override
    public boolean operator()
    {
        return true;
    }

    @Override
    public int precedence()
    {
        return 5;
    }

    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            @Override
            protected Object evaluate()
            {
                final Object a = operand( 0 ).value();
                final List<?> b = cast( operand( 1 ).value(), List.class );
                
                if( b == null )
                {
                    return false;
                }
                else
                {
                    for( Object entry : b )
                    {
                        if( equal( a, entry ) )
                        {
                            return true;
                        }
                    }
                    
                    return false;
                }
            }
        };
    }

}
