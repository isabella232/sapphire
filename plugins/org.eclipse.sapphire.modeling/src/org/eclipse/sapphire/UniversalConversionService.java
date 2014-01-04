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

import org.eclipse.sapphire.services.Service;

/**
 * Converts an object to the specified type.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class UniversalConversionService extends Service
{
    /**
     * Converts an object of source type to target type.
     * 
     * @param object the object to convert
     * @param type the conversion target type
     * @return the converted object or null if could not be converted
     */
    
    public abstract <T> T convert( Object object, Class<T> type );
    
}
