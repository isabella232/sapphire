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

package org.eclipse.sapphire.modeling.docsys;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class ListPart

    extends DocumentationPart
    
{
    private final List<ListItem> items = new ArrayList<ListItem>();
    
    public List<ListItem> getItems()
    {
        return this.items;
    }
    
    public void addItem( final ListItem item )
    {
        this.items.add( item );
    }
}
