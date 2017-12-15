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

package org.eclipse.sapphire.modeling.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ListFactory<E>
{
    private E firstElement = null;
    private List<E> list = null;
    private boolean created = false;
    
    public void add( E element )
    {
        if( this.created )
        {
            throw new IllegalStateException();
        }
        
        if( this.list != null )
        {
            this.list.add( element );
        }
        else if( this.firstElement != null )
        {
            this.list = new ArrayList<E>();
            this.list.add( this.firstElement );
            this.list.add( element );
            this.firstElement = null;
        }
        else
        {
            this.firstElement = element;
        }
    }
    
    public List<E> create()
    {
        if( this.created )
        {
            throw new IllegalStateException();
        }
        
        this.created = true;
        
        if( this.list != null )
        {
            return Collections.unmodifiableList( this.list );
        }
        else if( this.firstElement != null )
        {
            return Collections.singletonList( this.firstElement );
        }
        else
        {
            return Collections.emptyList();
        }
    }
    
}
