/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import static org.eclipse.sapphire.ui.forms.swt.SwtUtil.runOnDisplayThread;

import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyEvent;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class PropertyEditorActionHandler extends SapphireActionHandler
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
        
        property().attach( this.listener );
        
        refreshEnablementState();
        
        attach
        (
            new FilteredListener<DisposeEvent>()
            {
                @Override
                protected void handleTypedEvent( final DisposeEvent event )
                {
                    property().detach( PropertyEditorActionHandler.this.listener );
                }
            }
        );
    }
    
    @Override
    public final Element getModelElement()
    {
        return ( (PropertyEditorPart) getPart() ).getLocalModelElement();
    }

    public Property property()
    {
        return ( (PropertyEditorPart) getPart() ).property();
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
        return property().enabled();
    }
    
}