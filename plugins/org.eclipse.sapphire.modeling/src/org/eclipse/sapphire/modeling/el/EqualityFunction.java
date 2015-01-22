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

import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.Value;

/**
 * Equality function. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class EqualityFunction extends Function
{
    public static EqualityFunction create( final Function a,
                                           final Function b )
    {
        final EqualityFunction function = new EqualityFunction();
        function.init( a, b );
        return function;
    }
    
    @Override
    public String name()
    {
        return "==";
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
                Object a = operand( 0 );
                
                if( a instanceof Value<?> )
                {
                    a = ( (Value<?>) a ).content();
                }
                else if( a instanceof ElementHandle<?> )
                {
                    a = ( (ElementHandle<?>) a ).content();
                }
                
                Object b = operand( 1 );

                if( b instanceof Value<?> )
                {
                    b = ( (Value<?>) b ).content();
                }
                else if( b instanceof ElementHandle<?> )
                {
                    b = ( (ElementHandle<?>) b ).content();
                }
                
                return equal( a, b );
            }
        };
    }

}
