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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.eclipse.sapphire.modeling.Value;

/**
 * Equality function. 
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class EqualityFunction

    extends Function

{
    public static EqualityFunction create( final Function a,
                                           final Function b )
    {
        final EqualityFunction function = new EqualityFunction();
        function.init( a, b );
        return function;
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
                final Object b = operand( 1 ).value();
                
                if( a == b )
                {
                    return true;
                }
                else if( a == null )
                {
                    if( b instanceof Value<?> )
                    {
                        return ( ( (Value<?>) b ).getText() == null );
                    }
                    
                    return false;
                }
                else if( b == null )
                {
                    if( a instanceof Value<?> )
                    {
                        return ( ( (Value<?>) a ).getText() == null );
                    }
                    
                    return false;
                }
                else if( a instanceof BigDecimal || b instanceof BigDecimal )
                {
                    final BigDecimal x = cast( a, BigDecimal.class );
                    final BigDecimal y = cast( b, BigDecimal.class );
                    return x.equals( y );
                }
                else if( a instanceof Float || a instanceof Double || b instanceof Float || b instanceof Double )
                {
                    final Double x = cast( a, Double.class );
                    final Double y = cast( b, Double.class );
                    return ( x == y );
                }
                else if( a instanceof BigInteger || b instanceof BigInteger )
                {
                    final BigInteger x = cast( a, BigInteger.class );
                    final BigInteger y = cast( b, BigInteger.class );
                    return x.equals( y );
                }
                else if( a instanceof Byte || a instanceof Short || a instanceof Character || a instanceof Integer || a instanceof Long || 
                         b instanceof Byte || b instanceof Short || b instanceof Character || b instanceof Integer || b instanceof Long )
                {
                    final Long x = cast( a, Long.class );
                    final Long y = cast( b, Long.class );
                    return ( x == y );
                }
                else if( a instanceof Boolean || b instanceof Boolean )
                {
                    final Boolean x = cast( a, Boolean.class );
                    final Boolean y = cast( b, Boolean.class );
                    return ( x == y );
                }
                else if( a instanceof Enum )
                {
                    return ( a == cast( b, a.getClass() ) );
                }
                else if( b instanceof Enum )
                {
                    return ( cast( a, b.getClass() ) == b );
                }
                else if( a instanceof String || b instanceof String )
                {
                    final String x = cast( a, String.class );
                    final String y = cast( b, String.class );
                    return ( x.compareTo( y ) == 0 );
                }
                else
                {
                    return a.equals( b );
                }
            }
        };
    }

}
