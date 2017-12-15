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

/**
 * Function that returns one of two alternatives depending on a condition. 
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ConditionalFunction

    extends Function

{
    public static ConditionalFunction create( final Function condition,
                                              final Function positive,
                                              final Function negative )
    {
        final ConditionalFunction function = new ConditionalFunction();
        function.init( condition, positive, negative );
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
                final Boolean conditionValue = cast( operand( 0 ).value(), Boolean.class );
                
                if( conditionValue == true )
                {
                    return operand( 1 ).value();
                }
                else
                {
                    return operand( 2 ).value();
                }
            }
        };
    }
    
}
