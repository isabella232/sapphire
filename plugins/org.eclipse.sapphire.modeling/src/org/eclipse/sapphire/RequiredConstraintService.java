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

import org.eclipse.sapphire.services.DataService;

/**
 * Determines whether a property is required to have content. Most frequently specified
 * via an @Required annotation. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class RequiredConstraintService extends DataService<Boolean>
{
    @Override
    protected final void initDataService()
    {
        initRequiredConstraintService();
    }

    protected void initRequiredConstraintService()
    {
    }
    
    public final boolean required()
    {
        final Boolean data = data();
        return ( data == null ? false : data );
    }
    
}
