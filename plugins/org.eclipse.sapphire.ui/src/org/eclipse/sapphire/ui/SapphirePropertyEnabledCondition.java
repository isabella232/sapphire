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

import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class SapphirePropertyEnabledCondition

    extends SapphireCondition
    
{
    private ModelProperty property;
    
    @Override
    public void init( final SapphirePartContext context,
                      final String parameter )
    {
        super.init( context, parameter );
        
        final IModelElement element = this.context.getModelElement();
        this.property = element.getModelElementType().getProperty( parameter );
    }

    @Override
    public boolean evaluate()
    {
        if( this.property != null )
        {
            final IModelElement element = this.context.getModelElement();
            return element.isPropertyEnabled( this.property );
        }

        return false;
    }

    @Override
    public List<String> getDependencies()
    {
        return Collections.singletonList( this.property.getName() );
    }
    
}
