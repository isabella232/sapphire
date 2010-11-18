/******************************************************************************
 * Copyright (c) 2010 Oracle
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
 * Function that ensures that the returned value is of specified type and prevents function
 * exceptions from propagating.  
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class FailSafeFunction

    extends Function

{
    public static FailSafeFunction create( final FunctionContext context,
                                           final Function operand,
                                           final Function expectedType )
    {
        final FailSafeFunction function = new FailSafeFunction();
        function.init( context, operand, expectedType );
        return function;
    }

    public static FailSafeFunction create( final FunctionContext context,
                                           final Function operand,
                                           final Class<?> expectedType )
    {
        return create( context, operand, Literal.create( context, expectedType ) );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    
    protected final Object evaluate()
    {
        try
        {
            return cast( operand( 0 ).value(), cast( operand( 1 ).value(), Class.class ) );
        }
        catch( FunctionException e )
        {
            return handleFunctionException( e );
        }
    }
    
    protected Object handleFunctionException( final FunctionException e )
    {
        return null;
    }
    
}
