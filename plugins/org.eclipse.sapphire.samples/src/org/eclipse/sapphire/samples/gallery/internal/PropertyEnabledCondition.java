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

package org.eclipse.sapphire.samples.gallery.internal;

import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphireModelCondition;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PropertyEnabledCondition extends SapphireModelCondition
{
    private String property;
    
    @Override
    protected void initCondition( final ISapphirePart part,
                                  final String parameter )
    {
        this.property = parameter;
        
        super.initCondition( part, parameter );
    }

    @Override
    protected boolean evaluate()
    {
        final IModelElement element = getPart().getModelElement();
        final ModelProperty property = element.getModelElementType().getProperty( this.property );
        return element.isPropertyEnabled( property );
    }

    @Override
    public List<String> getDependencies()
    {
        return Collections.singletonList( this.property );
    }
    
}
