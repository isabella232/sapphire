/*******************************************************************************
 * Copyright (c) 2013 Liferay and Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gregory Amerson - initial implementation
 *    Konstantin Komissarchik - initial implementation review and related changes
 ******************************************************************************/

package org.eclipse.sapphire.services;

/**
 * Provides means to extend the behavior of the adapt method in a given context.
 * 
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class AdapterService extends Service
{
    /**
     * Returns an object which is an instance of the given class associated with this context or 
     * <code>null</code> if no such object can be found.
     * 
     * @param adapterType the class to adapt to
     * @return the adapted object or <code>null</code>
     */
    
    public abstract <A> A adapt( Class<A> adapterType );

}