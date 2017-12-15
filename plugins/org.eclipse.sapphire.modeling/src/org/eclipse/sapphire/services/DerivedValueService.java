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

package org.eclipse.sapphire.services;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class DerivedValueService extends DataService<DerivedValueServiceData>
{
    @Override
    protected final void initDataService()
    {
        initDerivedValueService();
    }

    protected void initDerivedValueService()
    {
    }
    
    public final String value()
    {
        return data().value();
    }
    
}
