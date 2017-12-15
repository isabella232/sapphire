/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling;

import java.util.List;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ListBindingImpl

    extends BindingImpl
    
{
    @Override
    public final ListProperty property()
    {
        return (ListProperty) super.property();
    }
    
    public abstract List<Resource> read();
    
    public abstract ModelElementType type( Resource resource );    
    
    public Resource add( final ModelElementType type )
    {
        throw new UnsupportedOperationException();
    }
    
    public void remove( final Resource resource )
    {
        throw new UnsupportedOperationException();
    }
    
    public void swap( final Resource a, 
                      final Resource b )
    {
        throw new UnsupportedOperationException();
    }

}
