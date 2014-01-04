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

package org.eclipse.sapphire.tests.modeling.el;

import java.math.BigInteger;

import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class FactorialFunction extends Function
{
    private static final BigInteger ZERO = BigInteger.valueOf( 0 );
    private static final BigInteger ONE = BigInteger.valueOf( 1 );
    
    @Override
    public String name()
    {
        return "test:factorial";
    }
    
    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            @Override
            protected Object evaluate()
            {
                final BigInteger x = cast( operand( 0 ), BigInteger.class );
                
                if( x.intValue() == 0 )
                {
                    return BigInteger.valueOf( 1 );
                }
                else
                {
                    BigInteger res = x;
                    
                    for( BigInteger i = x.subtract( ONE ); i.compareTo( ZERO ) > 0; i = i.subtract( ONE ) )
                    {
                        res = res.multiply( i );
                    }
                    
                    return res;
                }
            }
        };
    }

}
