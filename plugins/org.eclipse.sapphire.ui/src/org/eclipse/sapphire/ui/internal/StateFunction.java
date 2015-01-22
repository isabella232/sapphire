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

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionException;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.ui.PartFunctionContext;
import org.eclipse.sapphire.ui.SapphireEditorPagePart;

/**
 * Returns the root element of editor page's persisted state, allowing access to various state
 * properties. This is particularly useful when the persisted state is extended with custom
 * properties wired to custom actions, as it allows any EL-enabled facility to integrate with
 * the custom state property.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StateFunction extends Function
{
    @Text( "Editor page not found." )
    private static LocalizableText editorPageNotFound;
    
    static
    {
        LocalizableText.init( StateFunction.class );
    }

    @Override
    public String name()
    {
        return "State";
    }

    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            @Override
            protected Object evaluate()
            {
                SapphireEditorPagePart page = null;
                
                if( context instanceof PartFunctionContext )
                {
                    page = ( (PartFunctionContext) context ).part().nearest( SapphireEditorPagePart.class );
                }
                
                if( page == null )
                {
                    throw new FunctionException( editorPageNotFound.text() );
                }
                
                return page.state();
            }
        };
    }
    
}
