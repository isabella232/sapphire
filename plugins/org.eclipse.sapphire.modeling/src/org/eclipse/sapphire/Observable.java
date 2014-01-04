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
 * An entity that supports listeners.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface Observable
{
    /**
     * Attaches a listener to this entity.
     * 
     * @param listener the listener
     */
    
    void attach( Listener listener );
    
    /**
     * Detaches a listener from this entity.
     * 
     * @param listener the listener
     */
    
    void detach( Listener listener );
    
}
