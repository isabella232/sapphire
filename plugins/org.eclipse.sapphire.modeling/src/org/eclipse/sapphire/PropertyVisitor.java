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
 * Implemented in order to visit properties using Element.visit() method.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class PropertyVisitor
{
    /**
     * Called to visit a property.
     * 
     * @param property the property
     * @return true to continue traversal and false otherwise
     */
    
    public boolean visit( final Property property )
    {
        if( property instanceof Value )
        {
            return visit( (Value<?>) property );
        }
        else if( property instanceof Transient )
        {
            return visit( (Transient<?>) property );
        }
        else if( property instanceof ElementHandle )
        {
            return visit( (ElementHandle<?>) property );
        }
        else if( property instanceof ElementList )
        {
            return visit( (ElementList<?>) property );
        }
        else
        {
            throw new IllegalStateException();
        }
    }
    
    /**
     * Called to visit a value property.
     * 
     * @param property the value property
     * @return true to continue traversal and false otherwise
     */
    
    public boolean visit( final Value<?> property )
    {
        // Default implementation does nothing.
        
        return true;
    }
    
    /**
     * Called to visit a transient property.
     * 
     * @param property the transient property
     * @return true to continue traversal and false otherwise
     */
    
    public boolean visit( final Transient<?> property )
    {
        // Default implementation does nothing.
        
        return true;
    }
    
    /**
     * Called to visit an element property.
     * 
     * @param property the element property
     * @return true to continue traversal and false otherwise
     */
    
    public boolean visit( final ElementHandle<?> property )
    {
        // Default implementation does nothing.
        
        return true;
    }
    
    /**
     * Called to visit a list property.
     * 
     * @param property the list property
     * @return true to continue traversal and false otherwise
     */
    
    public boolean visit( final ElementList<?> property )
    {
        // Default implementation does nothing.
        
        return true;
    }

}
