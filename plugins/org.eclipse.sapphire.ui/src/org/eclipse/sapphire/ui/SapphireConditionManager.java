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

import java.util.List;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireConditionManager
{
    private final SapphirePart part;
    private final SapphireCondition condition;
    private final List<String> conditionDependencies;
    private final ModelPropertyListener conditionUpdateListener;
    private boolean conditionState;
    private final Runnable onConditionChangeCallback;
    
    public static SapphireConditionManager create( final SapphirePart part,
                                                   final Class<?> conditionClass,
                                                   final String conditionParameter,
                                                   final Runnable onConditionChangeCallback )
    {
        try
        {
            final SapphireCondition condition = (SapphireCondition) conditionClass.newInstance();
            condition.init( new SapphirePartContext( part ), conditionParameter );
            
            return new SapphireConditionManager( part, condition, onConditionChangeCallback );
        }
        catch( Exception e )
        {
            SapphireUiFrameworkPlugin.log( e );
        }
        
        return null;
    }

    private SapphireConditionManager( final SapphirePart part,
                                      final SapphireCondition condition,
                                      final Runnable onConditionChangeCallback )
    {
        this.part = part;
        this.condition = condition;
        this.conditionDependencies = this.condition.getDependencies();
        this.conditionState = this.condition.evaluate();
        this.onConditionChangeCallback = onConditionChangeCallback;
        
        if( this.conditionDependencies != null && this.conditionDependencies.size() > 0 )
        {
            this.conditionUpdateListener = new ModelPropertyListener()
            {
                @Override
                public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
                {
                    updateConditionState();
                }
            };
            
            final IModelElement contextModelElement = part.getModelElement();
            
            for( String dependency : this.conditionDependencies )
            {
                contextModelElement.addListener( this.conditionUpdateListener, dependency );
            }
        }
        else
        {
            this.conditionUpdateListener = null;
        }
    }
                                     
    public boolean getConditionState()
    {
        return this.conditionState;
    }

    private void updateConditionState()
    {
        final boolean newConditionState;
        
        try
        {
            newConditionState = this.condition.evaluate();
        }
        catch( Exception e )
        {
            SapphireUiFrameworkPlugin.log( e );
            return;
        }
        
        if( this.conditionState != newConditionState )
        {
            this.conditionState = newConditionState;
            this.onConditionChangeCallback.run();
        }
    }
    
    public void dispose()
    {
        if( this.conditionUpdateListener != null )
        {
            final IModelElement modelElement = this.part.getModelElement();
            
            for( String dependency : this.conditionDependencies )
            {
                modelElement.removeListener( this.conditionUpdateListener, dependency );
            }
        }
    }

}
