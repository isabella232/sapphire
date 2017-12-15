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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IHandler;
import org.eclipse.sapphire.ui.util.internal.MutableReference;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireCommands
{
    public static final String CONTEXT_PROPERTY_EDITOR = "sapphire.property.editor";
    public static final String CONTEXT_CONTENT_OUTLINE = "sapphire.content.outline";
    
    public static final String COMMAND_BROWSE = "sapphire.browse"; //$NON-NLS-1$
    public static final String COMMAND_SHOW_ASSIST = "sapphire.show.assist";
    public static final String COMMAND_JUMP = "sapphire.jump";
    public static final String COMMAND_MOVE_UP = "sapphire.move.up";
    public static final String COMMAND_MOVE_DOWN = "sapphire.move.down";
    
    private static final Map<String,IContextActivation> CONTEXTS
        = new HashMap<String,IContextActivation>();
    
    private static final Map<String,IHandlerActivation> HANDLERS
        = new HashMap<String,IHandlerActivation>();
    
    public static void configurePropertyEditorContext( final Control control )
    {
        configureContext( control, CONTEXT_PROPERTY_EDITOR );
    }
    
    public static void configureContentOutlineContext( final Control control )
    {
        configureContext( control, CONTEXT_CONTENT_OUTLINE );
    }
    
    private static void configureContext( final Control control,
                                          final String contextId )
    {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final IContextService contextService = (IContextService) workbench.getService( IContextService.class );
        final MutableReference<IContextActivation> contextActivationRef = new MutableReference<IContextActivation>();
        
        control.addListener
        ( 
            SWT.Activate,
            new Listener()
            {
                public void handleEvent( final Event event )
                {
                    final IContextActivation existingContextActivation = CONTEXTS.get( contextId );
                    
                    if( existingContextActivation != null )
                    {
                        contextService.deactivateContext( existingContextActivation );
                    }
                    
                    final IContextActivation contextActivation = contextService.activateContext( contextId );
                    contextActivationRef.set( contextActivation );
                    CONTEXTS.put( contextId, contextActivation );
                }
            }
        );
        
        final Runnable deactivateContextOperation = new Runnable()
        {
            public void run()
            {
                IContextActivation contextActivation = contextActivationRef.get();
                
                if( contextActivation != null ) 
                {
                    contextService.deactivateContext( contextActivation );
                    
                    if( CONTEXTS.get( contextId ) == contextActivation )
                    {
                        CONTEXTS.remove( contextId );
                    }
                }
            }
        };

        control.addListener
        ( 
            SWT.Deactivate,
            new Listener()
            {
                public void handleEvent( final Event event )
                {
                    deactivateContextOperation.run();
                }
            }
        );
        
        control.addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    deactivateContextOperation.run();
                }
            }
        );
    }
    
    public static void attachBrowseHandler( final Control control,
                                            final IHandler handler )
    {
        attachHandler( control, COMMAND_BROWSE, handler );
    }

    public static void attachShowAssistHandler( final Control control,
                                                final IHandler handler )
    {
        attachHandler( control, COMMAND_SHOW_ASSIST, handler );
    }
    
    public static void attachJumpHandler( final Control control,
                                          final IHandler handler )
    {
        attachHandler( control, COMMAND_JUMP, handler );
    }
    
    private static void attachHandler( final Control control,
                                       final String commandId,
                                       final IHandler handler )
    {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final IHandlerService handlerService = (IHandlerService) workbench.getService( IHandlerService.class );
        final MutableReference<IHandlerActivation> handlerActivationRef = new MutableReference<IHandlerActivation>();
        
        control.addListener
        ( 
            SWT.Activate,
            new Listener()
            {
                public void handleEvent( final Event event )
                {
                    final IHandlerActivation existingHandler = HANDLERS.get( commandId );
                    
                    if( existingHandler != null )
                    {
                        handlerService.deactivateHandler( existingHandler );
                    }
                    
                    final IHandlerActivation handlerActivation
                        = handlerService.activateHandler( commandId, handler );
                    
                    handlerActivationRef.set( handlerActivation );
                    HANDLERS.put( commandId, handlerActivation );
                }
            }
        );
        
        final Runnable deactivateHandlerOperation = new Runnable()
        {
            public void run()
            {
                final IHandlerActivation handlerActivation = handlerActivationRef.get();
                
                if( handlerActivation != null ) 
                {
                    handlerService.deactivateHandler( handlerActivation );
                    
                    if( HANDLERS.get( commandId ) == handlerActivation )
                    {
                        HANDLERS.remove( commandId );
                    }
                }
            }
        };
        
        control.addListener
        ( 
            SWT.Deactivate,
            new Listener()
            {
                public void handleEvent( final Event event )
                {
                    deactivateHandlerOperation.run();
                }
            }
        );
        
        control.addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( DisposeEvent e )
                {
                    deactivateHandlerOperation.run();
                }
            }
        );
    }
    
}
