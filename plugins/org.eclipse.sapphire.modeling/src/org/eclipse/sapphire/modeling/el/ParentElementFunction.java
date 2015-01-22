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

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Property;

/**
 * A function that returns the parent of the current model element. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ParentElementFunction extends Function
{
    public static ParentElementFunction create()
    {
        final ParentElementFunction function = new ParentElementFunction();
        function.init();
        return function;
    }
    
    @Override
    public String name()
    {
        return "Parent";
    }

    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            @Override
            protected Object evaluate()
            {
                final List<FunctionResult> operands = operands();
                final Element element;
                
                if( operands.isEmpty() )
                {
                    element = ( (ModelElementFunctionContext) context ).element();
                }
                else
                {
                    element = cast( operand( 0 ), Element.class );
                }
                
                final Property parent = element.parent();
                
                return ( parent == null ? null : parent.element() );
            }
        };
    }
    
}
