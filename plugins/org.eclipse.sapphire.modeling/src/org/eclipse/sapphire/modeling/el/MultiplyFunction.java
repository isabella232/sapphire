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

import static org.eclipse.sapphire.modeling.el.internal.FunctionUtils.isDecimalString;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.eclipse.sapphire.Value;

/**
 * Arithmetic multiplication function. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class MultiplyFunction extends Function
{
    public static MultiplyFunction create( final Function a,
                                           final Function b )
    {
        final MultiplyFunction function = new MultiplyFunction();
        function.init( a, b );
        return function;
    }

    public static MultiplyFunction create( final Number a,
                                           final Number b )
    {
        return create( Literal.create( a ), Literal.create( b ) );
    }
    
    @Override
    public String name()
    {
        return "*";
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
                else if( a instanceof BigDecimal || b instanceof BigDecimal )
                {
                    final BigDecimal x = cast( a, BigDecimal.class );
                    final BigDecimal y = cast( b, BigDecimal.class );
                    return x.multiply( y );
                }
                else if( a instanceof Float || a instanceof Double || isDecimalString( a ) || 
                         b instanceof Float || b instanceof Double || isDecimalString( b ) )
                {
                    if( a instanceof BigInteger || b instanceof BigInteger )
                    {
                        final BigDecimal x = cast( a, BigDecimal.class );
                        final BigDecimal y = cast( b, BigDecimal.class );
                        return x.multiply( y );
                    }
                    else
                    {
                        final Double x = cast( a, Double.class );
                        final Double y = cast( b, Double.class );
                        return x * y;
                    }
                }
                else if( a instanceof BigInteger || b instanceof BigInteger )
                {
                    final BigInteger x = cast( a, BigInteger.class );
                    final BigInteger y = cast( b, BigInteger.class );
                    return x.multiply( y );
                }
                else
                {
                    final Long x = cast( a, Long.class );
                    final Long y = cast( b, Long.class );
                    return x * y;
                }
            }
        };
    }
    
}
