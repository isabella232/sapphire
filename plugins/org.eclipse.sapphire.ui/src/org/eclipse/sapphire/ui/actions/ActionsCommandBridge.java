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

package org.eclipse.sapphire.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ActionsCommandBridge
{
    private final Control control;
    private final List<ActionGroup> actions;
    private final IHandlerService handlerService;
    private final List<IHandlerActivation> handlerActivations;
    
    public ActionsCommandBridge( final Control control )
    {
        this.control = control;
        this.actions = new ArrayList<ActionGroup>();
        this.handlerService = (IHandlerService) PlatformUI.getWorkbench().getService( IHandlerService.class );
        this.handlerActivations = new ArrayList<IHandlerActivation>();
        
        this.control.addFocusListener
        (
            new FocusListener()
            {
                public void focusGained( final FocusEvent event )
                {
                    activateHandlers();
                }

                public void focusLost( final FocusEvent event )
                {
                    deactivateHandlers();
                }
            }
        );        
    }
    
    public void setActions( final List<ActionGroup> actions )
    {
        this.actions.clear();
        this.actions.addAll( actions );
        
        if( this.control.isFocusControl() )
        {
            activateHandlers();
        }
    }
    
    private void activateHandlers()
    {
        deactivateHandlers();
        
        for( ActionGroup group : this.actions )
        {
            for( final Action action : group.getActions() )
            {
                final String commandId = action.getCommandId();
                
                if( commandId != null )
                {
                    final IHandler handler = new AbstractHandler() 
                    {
                        public Object execute( final ExecutionEvent event )
                        {
                            action.execute( ActionsCommandBridge.this.control.getShell() );
                            return null;
                        }
                    };
                    
                    final IHandlerActivation activation = this.handlerService.activateHandler( commandId, handler );
                    this.handlerActivations.add( activation );
                }
            }
        }
    }
    
    private void deactivateHandlers()
    {
        for( IHandlerActivation activation : this.handlerActivations )
        {
            this.handlerService.deactivateHandler( activation );
        }
        
        this.handlerActivations.clear();
    }
    
}
