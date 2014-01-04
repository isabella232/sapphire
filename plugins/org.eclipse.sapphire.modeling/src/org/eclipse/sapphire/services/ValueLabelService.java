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

package org.eclipse.sapphire.services;

/**
 * Provides the label to be used when presenting a given value to the user. The default behavior is
 * to use the value itself.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class ValueLabelService extends Service
{
    /**
     * Returns the label to use when presenting a given value to the user. If an unrecognized value 
     * is encountered, the implementation should return the value itself.
     *   
     * @param value the value that will be presented to the user
     * @return the label to use when presenting a given value to the user
     */
    
    public abstract String provide( String value );
    
}
