/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling;

import org.eclipse.core.runtime.IStatus;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ValidationStateChangeEvent
{
    private final IModelElement modelElement;
    private final IStatus oldValidationState;
    private final IStatus newValidationState;
    
    public ValidationStateChangeEvent( final IModelElement modelElement,
                                       final IStatus oldValidationState,
                                       final IStatus newValidationState )
    {
        this.modelElement = modelElement;
        this.oldValidationState = oldValidationState;
        this.newValidationState = newValidationState;
    }
    
    public IModelElement getModelElement()
    {
        return this.modelElement;
    }
    
    public IStatus getOldValidationState()
    {
        return this.oldValidationState;
    }
    
    public IStatus getNewValidationState()
    {
        return this.newValidationState;
    }

}
