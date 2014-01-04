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

package org.eclipse.sapphire;

import java.util.List;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class ListPropertyBinding extends PropertyBinding
{
    public abstract List<? extends Resource> read();
    
    public abstract ElementType type( Resource resource );    
    
    public Resource insert( final ElementType type,
                            final int position )
    {
        throw new UnsupportedOperationException();
    }
    
    public void move( final Resource resource,
                      final int position )
    {
        throw new UnsupportedOperationException();
    }
    
    public void remove( final Resource resource )
    {
        throw new UnsupportedOperationException();
    }

}
