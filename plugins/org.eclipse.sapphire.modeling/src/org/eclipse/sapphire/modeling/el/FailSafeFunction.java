/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.el;

import org.eclipse.sapphire.modeling.Value;

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
        return create( operand, expectedType, Literal.NULL );
    }

    public static Function create( Function operand,
                                   Function expectedType,
                                   Function defaultValue )
    {
        if( operand == null )
        {
            operand = Literal.NULL;
        }
        
        if( expectedType == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( defaultValue == null )
        {
            defaultValue = Literal.NULL;
        }
        
        if( operand instanceof Literal && expectedType instanceof Literal && defaultValue instanceof Literal )
        {
            final Object op = ( (Literal) operand ).value();
            final Object et = ( (Literal) expectedType ).value();
            final Object dv = ( (Literal) defaultValue ).value();
            
            if( dv == null && ( op == null || op.getClass().equals( et ) ) )
            {
                return operand;
            }
        }
        
        final FailSafeFunction function = new FailSafeFunction();
        function.init( operand, expectedType, defaultValue );
        function.initOrigin( operand.origin(), false );
        
        return function;
    }

    @Override
    public String name()
    {
        return "FailSafe";
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
                Object val = operand( 0 ).value();
                
                if( val == null || ( val instanceof Value && ( (Value<?>) val ).getContent() == null ) )
                {
                    val = operand( 2 ).value();
                }
                
                try
                {
                    return cast( val, cast( operand( 1 ).value(), Class.class ) );
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
