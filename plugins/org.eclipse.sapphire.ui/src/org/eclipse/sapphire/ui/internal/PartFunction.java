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

package org.eclipse.sapphire.ui.internal;

import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.ui.PartFunctionContext;
import org.eclipse.sapphire.ui.SapphirePart;

/**
 * Returns the context part.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PartFunction extends Function
{
    @Override
    public String name()
    {
        return "Part";
    }

    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        final SapphirePart part;
        
        if( context instanceof PartFunctionContext )
        {
            part = ( (PartFunctionContext) context ).part();
        }
        else
        {
            part = null;
        }
        
        return new FunctionResult( this, context )
        {
            @Override
            protected Object evaluate()
            {
                return part;
            }
        };
    }
    
}
