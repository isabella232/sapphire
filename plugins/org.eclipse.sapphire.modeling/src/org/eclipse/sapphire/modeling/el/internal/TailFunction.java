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

package org.eclipse.sapphire.modeling.el.internal;

import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;

/**
 * Returns a fragment of a string starting at the end and not exceeding the specified length.
 * 
 * <ul>
 *   <li>A negative fragment length is normalized to zero.</li>
 *   <li>A fragment length exceeding the length of the input is normalized to the length of the input.</li>
 * </ul>
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TailFunction extends Function
{
    @Override
    public String name()
    {
        return "Head";
    }

    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            @Override
            protected Object evaluate()
            {
                final String string = cast( operand( 0 ), String.class );
                final int stringLength = string.length();
                
                int length = cast( operand( 1 ), Integer.class );
                
                if( length < 0 )
                {
                    length = 0;
                }
                else if( length > stringLength )
                {
                    length = stringLength;
                }
                
                return string.substring( stringLength - length );
            }
        };
    }
    
}
