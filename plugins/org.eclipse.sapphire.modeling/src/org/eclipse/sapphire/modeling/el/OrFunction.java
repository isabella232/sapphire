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
 * Logical OR function. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class OrFunction

    extends Function

{
    public static OrFunction create( final FunctionContext context,
                                     final Function a,
                                     final Function b )
    {
        final OrFunction function = new OrFunction();
        function.init( context, a, b );
        return function;
    }

    @Override
    protected Boolean evaluate()
    {
        final boolean a = cast( operand( 0 ).value(), Boolean.class );
        final boolean b = cast( operand( 1 ).value(), Boolean.class );
        return ( a || b );
    }

}
