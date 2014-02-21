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

import java.util.Comparator;

import org.eclipse.sapphire.services.DataService;

/**
 * Provides a comparator that can be used for sorting or matching string values. Most frequently specified
 * via @{@link Collation} annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class CollationService extends DataService<Comparator<String>>
{
    @Override
    protected final void initDataService()
    {
        initCollationService();
    }

    protected void initCollationService()
    {
    }
    
    public final Comparator<String> comparator()
    {
        return data();
    }
    
}
