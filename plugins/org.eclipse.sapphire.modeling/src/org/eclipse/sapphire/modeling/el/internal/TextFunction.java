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

import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.PropertyEvent;
import org.eclipse.sapphire.Value;

/**
 * Returns the text of a value, taking into account the default, if applicable.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TextFunction extends PropertyFunction<Value<?>>
{
    @Override
    public String name()
    {
        return "Text";
    }
    
    @Override
    protected Object evaluate( final Value<?> value )
    {
        return value.text();
    }

    @Override
    protected boolean relevant( final PropertyEvent event )
    {
        return ( event instanceof PropertyContentEvent );
    }
    
}
