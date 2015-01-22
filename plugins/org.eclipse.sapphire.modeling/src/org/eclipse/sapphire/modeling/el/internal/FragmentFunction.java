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
 * Returns a fragment of a string. The fragment starts at the index specified by the second argument and extends to
 * the character before the index specified by the third argument. The length of the fragment is end index minus start
 * index.
 * 
 * <ul>
 *   <li>A negative start index is normalized to zero.</li>
 *   <li>A start index exceeding the length of the input is normalized to the length of the input.</li>
 *   <li>An end index exceeding the length of the input is normalized to the length of the input.</li>
 *   <li>An end index that is smaller than the start index is normalized to the start index.</li>
 * </ul>
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class FragmentFunction extends Function
{
    @Override
    public String name()
    {
        return "Fragment";
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
                
                int start = cast( operand( 1 ), Integer.class );
                int end = cast( operand( 2 ), Integer.class );
                
                if( start < 0 )
                {
                    start = 0;
                }
                
                if( start > stringLength )
                {
                    start = stringLength;
                }
                
                if( end > stringLength )
                {
                    end = stringLength;
                }
                
                if( end < start )
                {
                    end = start;
                }
                
                return string.substring( start, end );
            }
        };
    }
    
}
