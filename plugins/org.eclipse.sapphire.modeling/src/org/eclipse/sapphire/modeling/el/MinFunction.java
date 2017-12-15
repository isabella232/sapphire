/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.el;

import java.math.BigDecimal;
import java.util.List;

/**
 * Finds the smallest number in a collection. Typically, this function takes the collection as the sole
 * parameter. However, when the collection is a model element list, a second parameter may be necessary
 * to specify the name (in the form of a string) of the list entry's value property to use in aggregation.
 * If the the collection is a model element list and the second parameter is not specified, this function
 * will use list entry's first value property for aggregation. 
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class MinFunction extends AggregateFunction
{
    @Override
    public String name()
    {
        return "Min";
    }
    
    @Override
    public final FunctionResult evaluate( final FunctionContext context )
    {
        return new AggregateFunctionResult( this, context )
        {
            @Override
            protected Object evaluate( final List<Object> items )
            {
                BigDecimal min = null;
                
                for( Object item : items )
                {
                    if( item != null )
                    {
                        final BigDecimal itemAsDecimal = cast( item, BigDecimal.class );
                        
                        if( min == null )
                        {
                            min = itemAsDecimal;
                        }
                        else if( itemAsDecimal.compareTo( min ) < 0 )
                        {
                            min = itemAsDecimal;
                        }
                    }
                }
                
                return min;
            }
        };
    }

}
