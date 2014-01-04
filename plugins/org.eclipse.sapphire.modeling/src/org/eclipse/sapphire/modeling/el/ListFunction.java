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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Function that constructs a list from arbitrary number of operands. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ListFunction extends Function
{
    @Override
    public String name()
    {
        return "List";
    }

    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            @Override
            protected Object evaluate()
            {
                final int size = operands().size();
                
                if( size == 0 )
                {
                    return Collections.emptyList();
                }
                else if( size == 1 )
                {
                    return Collections.singletonList( operand( 0 ) );
                }
                else
                {
                    final List<Object> list = new ArrayList<Object>();
                    
                    for( int i = 0; i < size; i++ )
                    {
                        list.add( operand( i ) );
                    }
                    
                    return list;
                }
            }
        };
    }

}
