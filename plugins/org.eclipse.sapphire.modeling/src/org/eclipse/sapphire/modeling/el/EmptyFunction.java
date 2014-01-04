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

package org.eclipse.sapphire.modeling.el;

import java.util.Collection;

import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.Value;

/**
 * Function for determining if a value is null or empty. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class EmptyFunction

    extends Function

{
    public static EmptyFunction create( final Function operand )
    {
        final EmptyFunction function = new EmptyFunction();
        function.init( operand );
        return function;
    }
    
    @Override
    public String name()
    {
        return "empty";
    }

    @Override
    public boolean operator()
    {
        return true;
    }

    @Override
    public int precedence()
    {
        return 2;
    }

    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            @Override
            protected Object evaluate()
            {
                final Object a = operand( 0 );
                
                if( a == null )
                {
                    return true;
                }
                else if( a instanceof String && ( (String) a ).length() == 0 )
                {
                    return true;
                }
                else if( a instanceof Object[] && ( (Object[]) a ).length == 0 )
                {
                    return true;
                }
                else if( a instanceof Collection && ( (Collection<?>) a ).isEmpty() )
                {
                    return true;
                }
                else if( a instanceof Value<?> && ( (Value<?>) a ).content() == null )
                {
                    return true;
                }
                else if( a instanceof ElementHandle<?> && ( (ElementHandle<?>) a ).content() == null )
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
        };
    }

}
