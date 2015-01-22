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

import org.eclipse.sapphire.ImageData;

/**
 * Provides the image to use when presenting a given value to the user.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class ValueImageService

    extends Service
    
{
    /**
     * Returns the image to use when presenting a given value to the user. If an unrecognized value 
     * is encountered, the implementation should return null.
     * 
     * @param value the value that will be presented to the user
     * @return the image to use when presenting a given value to the user
     */
    
    public abstract ImageData provide( String value );
    
}
