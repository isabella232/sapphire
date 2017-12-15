/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ModelPropertyChangeEvent
{
    private final IModelElement element;
    private final ModelProperty property;
    private final boolean oldEnablementState;
    private final boolean newEnablementState;
    
    public ModelPropertyChangeEvent( final IModelElement element,
                                     final ModelProperty property,
                                     final Boolean oldEnablementState,
                                     final boolean newEnablementState )
    {
        if( element == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.element = element;
        this.property = property;
        this.oldEnablementState = ( oldEnablementState == null ? newEnablementState : oldEnablementState );
        this.newEnablementState = newEnablementState;
    }
    
    public IModelElement getModelElement()
    {
        return this.element;
    }
    
    public ModelProperty getProperty()
    {
        return this.property;
    }
    
    public boolean getOldEnablementState()
    {
        return this.oldEnablementState;
    }
    
    public boolean getNewEnablementState()
    {
        return this.newEnablementState;
    }

}
