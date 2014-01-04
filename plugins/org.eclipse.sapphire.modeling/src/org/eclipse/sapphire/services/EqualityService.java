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
 * Provides means to implement equals() and hashCode() methods when the context object doesn't support
 * implementing these methods directly.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class EqualityService extends Service
{
    /**
     * Performs the delegated equality computation. 
     * 
     * <p>Note that one of the objects to compare must be retrieved from the service context. For instance, 
     * if the context is a model element, implementation would need to make <code>context( Element.class )</code>
     * call.</p>
     *  
     * @param obj the object to compare for equality with the context object
     * @return <code>true</code> if the specified object is equal to the context object; <code>false</code> otherwise 
     */
    
    public abstract boolean doEquals( Object obj );
    
    /**
     * Performs the delegated hash code computation.
     * 
     * @return a hash code value for the context object
     */
    
    public abstract int doHashCode();

}
