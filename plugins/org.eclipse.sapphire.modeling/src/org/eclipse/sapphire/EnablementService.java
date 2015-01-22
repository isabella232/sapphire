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

package org.eclipse.sapphire;

import org.eclipse.sapphire.services.DataService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class EnablementService extends DataService<Boolean>
{
    @Override
    protected final void initDataService()
    {
        initEnablementService();
    }

    protected void initEnablementService()
    {
    }
    
    public final boolean enablement()
    {
        final Boolean data = data();
        return ( data == null ? true : data );
    }
    
}
