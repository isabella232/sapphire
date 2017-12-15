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

package org.eclipse.sapphire.modeling.el;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.eclipse.sapphire.modeling.util.NLS;

/**
 * Less than or equal comparison function. 
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class LessThanOrEqualFunction

    extends Function

{
    public static LessThanOrEqualFunction create( final Function a,
                                                  final Function b )
    {
        final LessThanOrEqualFunction function = new LessThanOrEqualFunction();
        function.init( a, b );
        return function;
    }

    @Override
    public String name()
    {
        return "<=";
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
            @SuppressWarnings( { "unchecked", "rawtypes" } )
            
            protected Object evaluate()
            {
                final Object a = operand( 0 ).value();
                final Object b = operand( 1 ).value();
                
                if( a == b )
                {
                    return true;
                }
                else if( a == null || b == null )
                {
                    return false;
                }
                else if( a instanceof BigDecimal || b instanceof BigDecimal )
                {
                    final BigDecimal x = cast( a, BigDecimal.class );
                    final BigDecimal y = cast( b, BigDecimal.class );
                    return ( x.compareTo( y ) <= 0 );
                }
                else if( a instanceof Float || a instanceof Double || b instanceof Float || b instanceof Double )
                {
                    final Double x = cast( a, Double.class );
                    final Double y = cast( b, Double.class );
                    return ( x <= y );
                }
                else if( a instanceof BigInteger || b instanceof BigInteger )
                {
                    final BigInteger x = cast( a, BigInteger.class );
                    final BigInteger y = cast( b, BigInteger.class );
                    return ( x.compareTo( y ) <= 0 );
                }
                else if( a instanceof Byte || a instanceof Short || a instanceof Character || a instanceof Integer || a instanceof Long || 
                         b instanceof Byte || b instanceof Short || b instanceof Character || b instanceof Integer || b instanceof Long )
                {
                    final Long x = cast( a, Long.class );
                    final Long y = cast( b, Long.class );
                    return ( x <= y );
                }
                else if( a instanceof String || b instanceof String )
                {
                    final String x = cast( a, String.class );
                    final String y = cast( b, String.class );
                    return ( x.compareTo( y ) <= 0 );
                }
                else if( a instanceof Comparable )
                {
                    return ( ( (Comparable) a ).compareTo( b ) <= 0 );
                }
                else if( b instanceof Comparable )
                {
                    return ( ( (Comparable) b ).compareTo( a ) >= 0 );
                }
                else
                {
                    throw new FunctionException( NLS.bind( Resources.cannotApplyMessage, a.getClass().getName(), b.getClass().getName() ) );
                }
            }
        };
    }

    private static final class Resources extends NLS
    {
        public static String cannotApplyMessage;
        
        static
        {
            initializeMessages( LessThanOrEqualFunction.class.getName(), Resources.class );
        }
    }

}
