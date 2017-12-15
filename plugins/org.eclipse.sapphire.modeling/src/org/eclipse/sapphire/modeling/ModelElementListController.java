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

import java.util.List;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ModelElementListController<T extends IModelElement>
{
    private IModelElement element;
    private ListProperty property;
    private ModelElementList<T> list;

    public void init( final IModelElement element,
                      final ListProperty property,
                      final ModelElementList<T> list,
                      final String[] params )
    {
        this.element = element;
        this.property = property;
        this.list = list;
    }
    
    public final IModelElement getModelElement()
    {
        return this.element;
    }
    
    public final ListProperty getProperty()
    {
        return this.property;
    }
    
    public final ModelElementList<T> getList()
    {
        return this.list;
    }
    
    public abstract List<T> refresh( List<T> contents );
    
    public T createNewElement( ModelElementType type )
    {
        throw new UnsupportedOperationException();
    }
    
    public void swap( final T a, 
                      final T b )
    {
        throw new UnsupportedOperationException();
    }
    
    public void handleElementRemovedEvent()
    {
    }

}
