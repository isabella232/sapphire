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

import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyEnablementEvent;
import org.eclipse.sapphire.PropertyEvent;

/**
 * Returns the enablement of a property.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class EnabledFunction extends PropertyFunction<Property>
{
    @Override
    public String name()
    {
        return "Enabled";
    }
    
    @Override
    protected Object evaluate( final Property property )
    {
        return property.enabled();
    }

    @Override
    protected boolean relevant( final PropertyEvent event )
    {
        return ( event instanceof PropertyEnablementEvent );
    }
    
}
