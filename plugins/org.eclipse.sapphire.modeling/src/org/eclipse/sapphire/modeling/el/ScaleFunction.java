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

import java.math.BigDecimal;

/**
 * Changes the scale of a decimal. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ScaleFunction extends Function
{
    @Override
    public String name()
    {
        return "Scale";
    }

    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            @Override
            protected Object evaluate()
            {
                BigDecimal num = cast( operand( 0 ), BigDecimal.class );
                
                if( num == null )
                {
                    num = new BigDecimal( 0 );
                }
                
                Integer scale = cast( operand( 1 ), Integer.class );
                
                if( scale == null )
                {
                    scale = 0;
                }
                
                return num.setScale( scale, BigDecimal.ROUND_HALF_UP );
            }
        };
    }
    
}
