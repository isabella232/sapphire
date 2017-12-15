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

package org.eclipse.sapphire.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class SapphireJumpActionHandler extends SapphirePropertyEditorActionHandler
{
    private List<String> dependencies;
    
    @Override
    public void init( final SapphireAction action,
                      final ISapphireActionHandlerDef def )
    {
        super.init( action, def );
        
        this.dependencies = new ArrayList<String>();
        initDependencies( this.dependencies );
        
        final ModelPropertyListener listener = new ModelPropertyListener()
        {
            @Override
            public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
            {
                refreshEnablementState();
            }
        };
        
        final IModelElement element = getModelElement();
        
        for( String dependency : this.dependencies )
        {
            element.addListener( listener, dependency );
        }
        
        refreshEnablementState();
        
        attach
        (
            new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof DisposeEvent )
                    {
                        final IModelElement element = getModelElement();
                        
                        for( String dependency : SapphireJumpActionHandler.this.dependencies )
                        {
                            element.removeListener( listener, dependency );
                        }
                    }
                }
            }
        );
    }

    protected void initDependencies( final List<String> dependencies )
    {
        this.dependencies.add( getProperty().getName() );
    }
    
    @Override
    public final ValueProperty getProperty()
    {
        return (ValueProperty) super.getProperty();
    }
   
}