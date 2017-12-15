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

import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class SapphirePropertyEditorActionHandler extends SapphireActionHandler
{
    private ModelPropertyListener listener;
    
    @Override
    public void init( final SapphireAction action,
                      final ISapphireActionHandlerDef def )
    {
        super.init( action, def );
        
        this.listener = new ModelPropertyListener()
        {
            @Override
            public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
            {
                refreshEnablementState();
            }
        };
        
        getModelElement().addListener( this.listener, getProperty().getName() );
        
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
                        getModelElement().removeListener( SapphirePropertyEditorActionHandler.this.listener, getProperty().getName() );
                    }
                }
            }
        );
    }
    
    @Override
    public final IModelElement getModelElement()
    {
        return ( (SapphirePropertyEditor) getPart() ).getLocalModelElement();
    }

    public ModelProperty getProperty()
    {
        return ( (SapphirePropertyEditor) getPart() ).getProperty();
    }
    
    public final void refreshEnablementState()
    {
        setEnabled( computeEnablementState() );
    }
    
    protected boolean computeEnablementState()
    {
        return getModelElement().isPropertyEnabled( getProperty() );
    }
    
}