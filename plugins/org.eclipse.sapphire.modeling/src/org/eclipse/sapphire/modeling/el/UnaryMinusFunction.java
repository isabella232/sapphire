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

import static org.eclipse.sapphire.modeling.el.internal.FunctionUtils.isDecimalString;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.eclipse.osgi.util.NLS;

/**
 * Arithmetic unary minus function. 
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class UnaryMinusFunction

    extends Function

{
    public static UnaryMinusFunction create( final Function operand )
    {
        final UnaryMinusFunction function = new UnaryMinusFunction();
        function.init( operand );
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
                
                if( a == null )
                {
                    return (long) 0;
                }
                else if( a instanceof BigDecimal )
                {
                    return ( (BigDecimal) a ).negate();
                }
                else if( a instanceof BigInteger )
                {
                    return ( (BigInteger) a ).negate();
                }
                else if( a instanceof String )
                {
                    if( isDecimalString( a ) )
                    {
                        return -( cast( a, Double.class ) );
                    }
                    else
                    {
                        return -( cast( a, Long.class ) );
                    }
                }
                else if( a instanceof Byte )
                {
                    return -( (Byte) a );
                }
                else if( a instanceof Short )
                {
                    return -( (Short) a );
                }
                else if( a instanceof Integer )
                {
                    return -( (Integer) a );
                }
                else if( a instanceof Long )
                {
                    return -( (Long) a );
                }
                else if( a instanceof Float )
                {
                    return -( (Float) a );
                }
                else if( a instanceof Double )
                {
                    return -( (Double) a );
                }
                else
                {
                    throw new FunctionException( NLS.bind( Resources.cannotApplyMessage, a.getClass().getName() ) );
                }
            }
        };
    }

    private static final class Resources extends NLS
    {
        public static String cannotApplyMessage;
        
        static
        {
            initializeMessages( UnaryMinusFunction.class.getName(), Resources.class );
        }
    }

}
