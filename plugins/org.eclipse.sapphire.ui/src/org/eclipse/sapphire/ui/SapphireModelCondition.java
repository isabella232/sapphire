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

package org.eclipse.sapphire.ui;

import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.PropertyEvent;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class SapphireModelCondition extends SapphireCondition
{
    private List<String> dependencies;
    private Listener listener;
    
    protected void initCondition( final ISapphirePart part,
                                  final String parameter )
    {
        this.dependencies = getDependencies();
        
        if( this.dependencies != null && ! this.dependencies.isEmpty() )
        {
            this.listener = new FilteredListener<PropertyEvent>()
            {
                @Override
                protected void handleTypedEvent( final PropertyEvent event )
                {
                    updateConditionState();
                }
            };
            
            final IModelElement contextModelElement = part.getModelElement();
            
            for( String dependency : this.dependencies )
            {
                contextModelElement.attach( this.listener, dependency );
            }
        }
    }
    
    public List<String> getDependencies()
    {
        return Collections.emptyList();
    }
    
    public void dispose()
    {
        if( this.listener != null )
        {
            final IModelElement contextModelElement = getPart().getModelElement();
            
            for( String dependency : this.dependencies )
            {
                contextModelElement.detach( this.listener, dependency );
            }
        }
    }
    
}
