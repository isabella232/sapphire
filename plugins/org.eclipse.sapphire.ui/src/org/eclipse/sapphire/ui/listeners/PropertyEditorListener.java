/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.listeners;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.ui.SapphireRenderingContext;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class PropertyEditorListener
{
    private SapphireRenderingContext context = null;
    private IModelElement element = null;
    private ModelProperty property = null;
    
    public final void initialize( final SapphireRenderingContext context,
                                  final IModelElement element,
                                  final ModelProperty property )
    {
        this.context = context;
        this.element = element;
        this.property = property;
    }
    
    public final IModelElement getModelElement()
    {
        return this.element;
    }
    
    public final ModelProperty getProperty()
    {
        return this.property;
    }
    
    public final SapphireRenderingContext getUiContext()
    {
        return this.context;
    }
    
}
