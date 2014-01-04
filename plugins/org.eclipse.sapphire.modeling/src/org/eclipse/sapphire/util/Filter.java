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

package org.eclipse.sapphire.util;

/**
 * Generic filter interface that can be parameterized for different element types.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class Filter<E>
{
    /**
     * Evaluates whether the given element passes the criteria implemented by the filter.
     * 
     * @param element the element to evaluate or null
     * @return true if the given element is allowed by the filter, false otherwise
     */
    
    public abstract boolean allows( E element );
    
}
