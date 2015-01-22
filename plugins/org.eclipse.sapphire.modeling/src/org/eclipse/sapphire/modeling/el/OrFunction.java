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

import java.util.List;

import org.eclipse.sapphire.util.ListFactory;

/**
 * Logical OR function. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class OrFunction extends Function
{
    public static Function create( final Function... operands )
    {
        return create( ListFactory.<Function>start().add( operands ) );
    }
    
    public static Function create( final List<Function> operands )
    {
        return create( ListFactory.<Function>start().add( operands ) );
    }
    
    private static Function create( final ListFactory<Function> operands )
    {
        final int cardinality = operands.size();
        
        if( cardinality == 0 )
        {
            return null;
        }
        else if( cardinality == 1 )
        {
            return operands.get( 0 );
        }
        else
        {
            final OrFunction function = new OrFunction();
            function.init( operands.result() );
            return function;
        }
    }

    @Override
    public String name()
    {
        return "||";
    }

    @Override
    public boolean operator()
    {
        return true;
    }

    @Override
    public int precedence()
    {
        return 6;
    }

    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            @Override
            protected Object evaluate()
            {
                boolean result = false;
                
                for( FunctionResult operand : operands() )
                {
                    result = result || cast( operand.value(), Boolean.class );
                }
                
                return result;
            }
        };
    }
    
}
