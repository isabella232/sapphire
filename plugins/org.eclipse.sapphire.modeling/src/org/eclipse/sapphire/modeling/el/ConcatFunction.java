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

import java.util.List;

/**
 * Concatenates two or more strings into a single string. Particularly useful in 
 * contexts where composite expressions cannot be used, such as where the result of 
 * concatenation needs to feed into another function or operator. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ConcatFunction extends Function
{
    public static ConcatFunction create( final List<Function> operands )
    {
        final ConcatFunction function = new ConcatFunction();
        function.init( operands );
        return function;
    }

    public static ConcatFunction create( final Function a,
                                         final Function b )
    {
        final ConcatFunction function = new ConcatFunction();
        function.init( a, b );
        return function;
    }

    public static ConcatFunction create( final String a,
                                         final String b )
    {
        return create( Literal.create( a ), Literal.create( b ) );
    }

    public static ConcatFunction create( final Function a,
                                         final String b )
    {
        return create( a, Literal.create( b ) );
    }

    public static ConcatFunction create( final String a,
                                         final Function b )
    {
        return create( Literal.create( a ), b );
    }

    @Override
    public String name()
    {
        return "Concat";
    }

    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            @Override
            protected Object evaluate()
            {
                final StringBuilder buf = new StringBuilder();
                
                for( FunctionResult operand : operands() )
                {
                    buf.append( cast( operand.value(), String.class ) );
                }
                
                return buf.toString();
            }
        };
    }

    @Override
    public void toString( final StringBuilder buf,
                          final boolean topLevel )
    {
        if( topLevel )
        {
            for( Function operand : operands() )
            {
                if( operand instanceof Literal )
                {
                    operand.toString( buf, true );
                }
                else
                {
                    buf.append( "$( " );
                    operand.toString( buf, false );
                    buf.append( " }" );
                }
            }
        }
        else
        {
            super.toString( buf, topLevel );
        }
    }

}
