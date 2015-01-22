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

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.PropertyEvent;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Transient;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.el.FunctionException;

/**
 * Returns the content of a value or a transient. For value properties, the default is taken into account, if applicable.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ContentFunction extends PropertyFunction<Property>
{
    @Text( "Function Content cannot be applied to a {0} object." )
    private static LocalizableText unsupportedTypeMessage;
    
    static
    {
        LocalizableText.init( ContentFunction.class );
    }

    @Override
    public String name()
    {
        return "Content";
    }
    
    @Override
    protected Object evaluate( final Property property )
    {
        if( property instanceof Value )
        {
            return ( (Value<?>) property ).content();
        }
        else if( property instanceof Transient )
        {
            return ( (Transient<?>) property ).content();
        }
        
        final String msg = unsupportedTypeMessage.format( property.getClass().getName() );
        throw new FunctionException( msg );
    }

    @Override
    protected boolean relevant( final PropertyEvent event )
    {
        return ( event instanceof PropertyContentEvent );
    }
    
}
