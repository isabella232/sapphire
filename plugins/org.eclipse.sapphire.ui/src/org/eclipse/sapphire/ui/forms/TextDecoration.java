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

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.Color;
import org.eclipse.sapphire.modeling.el.FunctionResult;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TextDecoration
{
    private FunctionResult text;
    private FunctionResult color;
    
    TextDecoration( final FunctionResult text, final FunctionResult color )
    {
        this.text = text;
        this.color = color;
    }
    
    public String text()
    {
        return (String) this.text.value();
    }
    
    public Color color()
    {
        return (Color) this.color.value();
    }
    
    void dispose()
    {
        if( this.text != null )
        {
            this.text.dispose();
            this.text = null;
        }
        
        if( this.color != null )
        {
            this.color.dispose();
        }
    }
    
}
