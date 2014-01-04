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

package org.eclipse.sapphire.ui.util;

import static org.eclipse.sapphire.modeling.util.MiscUtil.containsUsingIdentity;

import java.util.Collection;
import java.util.Set;

import org.eclipse.sapphire.util.IdentityHashSet;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class MiscUtil
{
    private MiscUtil()
    {
    }
    
    public static <T> T findSelectionPostDelete( final Collection<? extends T> items, final Collection<? extends T> toBeDeletedItems )
    {
        T selectionPostDelete = null;
        
        // Try to select the item following the last to-be-deleted item.
        
        final Set<T> toBeDeleted = new IdentityHashSet<T>( toBeDeletedItems );
        
        for( T item : items )
        {
            if( toBeDeleted.isEmpty() )
            {
                selectionPostDelete = item;
                break;
            }
            
            toBeDeleted.remove( item );
        }
        
        // Failing that, try to select the last item not on the to-be-deleted list.
        
        if( selectionPostDelete == null )
        {
            for( T item : items )
            {
                if( ! containsUsingIdentity( toBeDeletedItems, item ) )
                {
                    selectionPostDelete = item;
                }
            }
        }
        
        return selectionPostDelete;
    }

}
