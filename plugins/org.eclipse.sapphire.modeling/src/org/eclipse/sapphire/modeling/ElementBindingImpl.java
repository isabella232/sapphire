/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ElementBindingImpl

    extends BindingImpl
    
{
    @Override
    public final ElementProperty property()
    {
        return (ElementProperty) super.property();
    }
    
    public abstract Resource read();
    
    public abstract ModelElementType type( Resource resource );
    
    public Resource create( final ModelElementType type )
    {
        throw new UnsupportedOperationException();
    }
    
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
    
    public boolean removable()
    {
        return false;
    }

}
