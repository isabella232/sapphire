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

import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.Value;

/**
 * Determines if an object is of specified type. The object to be checked is the first operand and the
 * type is the second operand. The type must be a fully-qualified Java class name.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class InstanceOfFunction

    extends Function

{
    public static InstanceOfFunction create( final List<Function> operands )
    {
        final InstanceOfFunction function = new InstanceOfFunction();
        function.init( operands );
        return function;
    }

    public static InstanceOfFunction create( final Function a,
                                             final Function b )
    {
        final InstanceOfFunction function = new InstanceOfFunction();
        function.init( a, b );
        return function;
    }

    public static InstanceOfFunction create( final Function a,
                                             final String b )
    {
        return create( a, Literal.create( b ) );
    }

    @Override
    public String name()
    {
        return "InstanceOf";
    }

    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            @Override
            protected Object evaluate()
            {
                Object obj = operand( 0 );
                
                if( obj == null )
                {
                    return Boolean.FALSE;
                }
                else
                {
                    if( obj instanceof Value )
                    {
                        obj = ( (Value<?>) obj ).content();
                    }
                    else if( obj instanceof ElementHandle )
                    {
                        obj = ( (ElementHandle<?>) obj ).content();
                    }
                    
                    if( obj == null )
                    {
                        return Boolean.FALSE;
                    }
                    
                    final String type = cast( operand( 1 ), String.class );
                    
                    if( type == null )
                    {
                        return Boolean.FALSE;
                    }
                    
                    return evaluate( obj.getClass(), type );
                }
            }
            
            private boolean evaluate( final Class<?> cl,
                                      final String type )
            {
                if( cl.getName().equals( type ) )
                {
                    return true;
                }
                else
                {
                    final Class<?> base = cl.getSuperclass();
                    
                    if( base != null && evaluate( base, type ) )
                    {
                        return true;
                    }
                    
                    for( Class<?> ifc : cl.getInterfaces() )
                    {
                        if( evaluate( ifc, type ) )
                        {
                            return true;
                        }
                    }
                    
                    return false;
                }
            }
        };
    }
    
}
