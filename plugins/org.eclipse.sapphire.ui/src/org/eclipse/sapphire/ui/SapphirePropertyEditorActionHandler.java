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

import static org.eclipse.sapphire.ui.swt.renderer.SwtUtil.runOnDisplayThread;

import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.PropertyEvent;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class SapphirePropertyEditorActionHandler extends SapphireActionHandler
{
    private Listener listener;
    
    @Override
    public void init( final SapphireAction action,
                      final ActionHandlerDef def )
    {
        super.init( action, def );
        
        this.listener = new FilteredListener<PropertyEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyEvent event )
            {
                refreshEnablementState();
            }
        };
        
        getModelElement().attach( this.listener, getProperty().getName() );
        
        refreshEnablementState();
        
        attach
        (
            new FilteredListener<DisposeEvent>()
            {
                @Override
                protected void handleTypedEvent( final DisposeEvent event )
                {
                    getModelElement().detach( SapphirePropertyEditorActionHandler.this.listener, getProperty().getName() );
                }
            }
        );
    }
    
    @Override
    public final IModelElement getModelElement()
    {
        return ( (PropertyEditorPart) getPart() ).getLocalModelElement();
    }

    public ModelProperty getProperty()
    {
        return ( (PropertyEditorPart) getPart() ).getProperty();
    }
    
    public final void refreshEnablementState()
    {
        final Runnable op = new Runnable()
        {
            public void run()
            {
                setEnabled( computeEnablementState() );
            }
        };
        
        runOnDisplayThread( op );
    }
    
    protected boolean computeEnablementState()
    {
        return getModelElement().enabled( getProperty() );
    }
    
}