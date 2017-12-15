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

package org.eclipse.sapphire.ui;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.ui.def.ISapphireWithDirectiveDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireWithDirective

    extends SapphirePartContainer
    
{
    private ISapphireWithDirectiveDef definition;
    private ElementProperty property;
    private IModelElement modelElementForChildParts;
    
    @Override
    
    protected void init()
    {
        this.definition = (ISapphireWithDirectiveDef) super.definition;
        this.property = (ElementProperty) resolve( this.definition.getProperty().getContent() );
        this.modelElementForChildParts = (IModelElement) this.property.invokeGetterMethod( getModelElement() );
        
        super.init();
    }

    @Override
    
    protected IModelElement getModelElementForChildParts()
    {
        return this.modelElementForChildParts;
    }
    
    @Override
    
    public boolean setFocus( final ModelPath path )
    {
        final ModelPath.Segment head = path.head();
        
        if( head instanceof ModelPath.PropertySegment )
        {
            final String propertyName = ( (ModelPath.PropertySegment) head ).getPropertyName();
            
            if( propertyName.equals( this.property.getName() ) && getModelElement().isPropertyEnabled( this.property ) )
            {
                super.setFocus( path.tail() );
            }
        }
        
        return false;
    }

}
