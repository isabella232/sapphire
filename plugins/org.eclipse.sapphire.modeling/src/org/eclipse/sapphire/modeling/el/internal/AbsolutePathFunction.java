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
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.services.RelativePathService;

/**
 * Returns the absolute path of a value for properties with a RelativePathService.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class AbsolutePathFunction extends PropertyFunction<Value<?>>
{
    @Override
    public String name()
    {
        return "Absolute";
    }
    
    @Override
    protected Object evaluate( final Value<?> value )
    {
        final Object content = value.content();
        
        if( content instanceof Path )
        {
            final RelativePathService relativePathService = value.service( RelativePathService.class );
            
            if( relativePathService != null )
            {
                return relativePathService.convertToAbsolute( (Path) content );
            }
        }
        
        return value.text();
    }

    @Override
    protected boolean relevant( final PropertyEvent event )
    {
        return ( event instanceof PropertyContentEvent );
    }
    
}
