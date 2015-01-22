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

package org.eclipse.sapphire.services;

import java.util.SortedSet;

import org.eclipse.sapphire.ElementType;

/**
 * Enumerates the possible child element types for a list or an element property. Each 
 * returned type is required to derive from the property's base type.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class PossibleTypesService extends DataService<PossibleTypesServiceData>
{
    @Override
    protected final void initDataService()
    {
        initPossibleTypesService();
    }

    protected void initPossibleTypesService()
    {
    }
    
    public final SortedSet<ElementType> types()
    {
        return data().types();
    }
    
}
