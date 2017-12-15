/*******************************************************************************
 * Copyright (c) 2012 Oracle and Accenture
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Kamesh Sampath - initial implementation
 *******************************************************************************/

package org.eclipse.sapphire.services;

/**
 * Produces a value to assign to a property when the containing model element is created.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 */

public abstract class InitialValueService extends DataService<InitialValueServiceData> 
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
        return data().value();
    }
}
