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
 * Function that ensures that the returned value is of specified type and prevents function
 * exceptions from propagating.  
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class FailSafeFunction

    extends Function

{
    public static Function create( final Function operand,
                                   final Function expectedType )
    {
        final FailSafeFunction function = new FailSafeFunction();
        function.init( operand, expectedType );
        return function;
    }

    public static Function create( final Function operand,
                                   final Class<?> expectedType )
    {
        if( operand instanceof Literal )
        {
            final Object val = ( (Literal) operand ).value();
            
            if( val != null && val.getClass().equals( expectedType ) )
            {
                return operand;
            }
        }
        
        return create( operand, Literal.create( expectedType ) );
    }
    
    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            @Override
            @SuppressWarnings( "unchecked" )
            
            protected Object evaluate()
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
        };
    }
    
    protected Object handleFunctionException( final FunctionException e )
    {
        return null;
    }

}
