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

package org.eclipse.sapphire.ui;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.localization.LocalizationService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PartFunctionContext extends ModelElementFunctionContext
{
    private final SapphirePart part;
    
    public PartFunctionContext( final SapphirePart part,
                                final Element element )
    {
        super( element, part.definition().adapt( LocalizationService.class ) );
        
        this.part = part;
    }
    
    public SapphirePart part()
    {
        return this.part;
    }

    @Override
    public FunctionResult property( final Object element,
                                    final String name )
    {
        if( name.equalsIgnoreCase( "params" ) )
        {
            return Literal.create( this.part.getParams() ).evaluate( this );
        }

        return super.property( element, name );
    }
    
}
