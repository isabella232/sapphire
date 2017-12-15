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

/**
 * Base class for all model element services. One retrieves a service for a model
 * element by making a IModelElement.service( [type] ) call.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ModelElementService
{
    private IModelElement element;
    
    public void init( final IModelElement element )
    {
        this.element = element;
    }
    
    public final IModelElement getModelElement()
    {
        return this.element;
    }
    
}
