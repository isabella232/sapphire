/*******************************************************************************
 * Copyright (c) 2015 Oracle and Accenture
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Kamesh Sampath - initial implementation
 *******************************************************************************/

package org.eclipse.sapphire;

import org.eclipse.sapphire.services.DataService;

/**
 * Produces a value to assign to a property when the containing model element is created.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 */

public abstract class InitialValueService extends DataService<String> 
{
    @Override
    protected final void initDataService()
    {
        initInitialValueService();
    }

    protected void initInitialValueService()
    {
    }
    
    public final String value()
    {
        return data();
    }
}
