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
 * Logical NOT function. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class NotFunction

    extends Function

{
    public static NotFunction create( final FunctionContext context,
                                      final Function operand )
    {
        final NotFunction function = new NotFunction();
        function.init( context, operand );
        return function;
    }

    @Override
    protected Boolean evaluate()
    {
        final boolean a = cast( operand( 0 ).value(), Boolean.class );
        return ( ! a );
    }

}
