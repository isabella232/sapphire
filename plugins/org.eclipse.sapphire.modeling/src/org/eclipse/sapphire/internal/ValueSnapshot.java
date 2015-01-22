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

package org.eclipse.sapphire.internal;

import org.eclipse.sapphire.ValueProperty;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ValueSnapshot
{
    private final ValueProperty property;
    private final String text;
    
    public ValueSnapshot( final ValueProperty property, final String text )
    {
        this.property = property;
        this.text = text;
    }
    
    public ValueProperty property()
    {
        return this.property;
    }
    
    public String text()
    {
        return this.text;
    }
    
    @Override
    public String toString()
    {
        return this.text;
    }
    
}
