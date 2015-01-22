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

package org.eclipse.sapphire.modeling.el;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.eclipse.sapphire.Value;

/**
 * Arithmetic division function. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DivideFunction extends Function
{
    public static DivideFunction create( final Function a,
                                         final Function b )
    {
        final DivideFunction function = new DivideFunction();
        function.init( a, b );
        return function;
    }

    public static DivideFunction create( final Number a,
                                         final Number b )
    {
        return create( Literal.create( a ), Literal.create( b ) );
    }
    
    @Override
    public String name()
    {
        return "/";
    }

    @Override
    public boolean operator()
    {
        return true;
    }

    @Override
    public int precedence()
    {
        return 3;
    }

    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            @Override
            protected Object evaluate()
            {
                Object a = operand( 0 );
                
                if( a instanceof Value<?> )
                {
                    a = ( (Value<?>) a ).content();
                }
                
                Object b = operand( 1 );

                if( b instanceof Value<?> )
                {
                    b = ( (Value<?>) b ).content();
                }
                
                if( a == null && b == null )
                {
                    return (long) 0;
                }
                else if( a instanceof BigDecimal || b instanceof BigDecimal || a instanceof BigInteger )
                {
                    final BigDecimal x = cast( a, BigDecimal.class );
                    final BigDecimal y = cast( b, BigDecimal.class );
                    return x.divide( y, BigDecimal.ROUND_HALF_UP );
                }
                else
                {
                    final Double x = cast( a, Double.class );
                    final Double y = cast( b, Double.class );
                    return x / y;
                }
            }
        };
    }

}
