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

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class ValuePropertyBinding extends PropertyBinding
{
    /**
     * Reads the current value of the property and returns it to the caller. The method should 
     * return null when the property value is not set rather than returning a default value.
     * Default values should be specified via annotations on the property. 
     * 
     * @return the value of the property
     */
    
    public abstract String read();
    
    /**
     * Writes a new property value. Note that the value parameter can be null, in which case
     * the property value should be cleared in the resource. The implementation must be 
     * consistent with the read method. In particular, calling write( x ) followed by read()
     * should always return x, for all values of x including null.
     *  
     * @param value the new value for the property
     */
    
    public abstract void write( String value );
    
}
