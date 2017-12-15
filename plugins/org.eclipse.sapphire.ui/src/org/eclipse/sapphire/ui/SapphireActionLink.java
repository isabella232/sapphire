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

import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.util.MutableReference;
import org.eclipse.sapphire.ui.SapphireAction.HandlersChangedEvent;
import org.eclipse.sapphire.ui.SapphireActionSystemPart.EnablementChangedEvent;
import org.eclipse.sapphire.ui.def.ISapphireActionLinkDef;
import org.eclipse.sapphire.ui.swt.renderer.internal.formtext.SapphireFormText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireActionLink extends SapphirePart
{
    public void render( final SapphireRenderingContext context )
    {
        final ISapphireActionLinkDef def = (ISapphireActionLinkDef) this.definition;
        final String actionId = def.getActionId().getContent();
        final String actionHandlerId = def.getActionHandlerId().getContent();
        final SapphireAction action = getAction( actionId );
        final String labelText = def.getLabel().getLocalizedText( CapitalizationType.FIRST_WORD_ONLY, false );
        final boolean showImage = def.getShowImage().getContent();

        Image image = null;
        
        if( showImage && action != null )
        {
            final ImageDescriptor imageDescriptor = action.getImage( 16 );
            
            if( imageDescriptor != null )
            {
                image = getImageCache().getImage( imageDescriptor );
            }
        }
        
        final Composite composite = new Composite( context.getComposite(), SWT.NONE );
        composite.setLayout( glayout( ( image == null ? 1 : 2 ), 0, 0 ) );
        composite.setLayoutData( gdhindent( gdhspan( gd(), 2 ), 8 ) );
        
        final Label imageLabel;
        
        if( image != null )
        {
            imageLabel = new Label( composite, SWT.NONE );
            imageLabel.setImage( image );
            imageLabel.setLayoutData( gdvalign( gd(), SWT.CENTER ) );
        }
        else
        {
            imageLabel = null;
        }
        
        final SapphireFormText text = new SapphireFormText( composite, SWT.NONE );
        text.setLayoutData( gdvalign( gdhfill(), SWT.CENTER ) );
        context.adapt( text );
        
        final StringBuilder buf = new StringBuilder();
        buf.append( "<form><p vspace=\"false\"><a href=\"action\" nowrap=\"true\">" );
        buf.append( labelText );
        buf.append( "</a></p></form>" );
        
        text.setText( buf.toString(), true, false );
        
        if( action != null )
        {
            final MutableReference<SapphireActionHandler> actionHandlerRef = new MutableReference<SapphireActionHandler>();
            final MutableReference<Listener> actionHandlerListenerRef = new MutableReference<Listener>();
            
            final Runnable refreshEnablementStateOp = new Runnable()
            {
                public void run()
                {
                    final SapphireActionHandler actionHandler = actionHandlerRef.get();
                    final boolean enabled = ( actionHandler != null && actionHandler.isEnabled() );
                    
                    if( imageLabel != null )
                    {
                        imageLabel.setEnabled( enabled );
                    }
                    
                    text.setEnabled( enabled );
                }
            };

            final Runnable resolveHandlerOp = new Runnable()
            {
                public void run()
                {
                    SapphireActionHandler actionHandler = actionHandlerRef.get();
                    
                    if( actionHandler != null )
                    {
                        final Listener actionHandlerListener = actionHandlerListenerRef.get();
                        
                        if( actionHandlerListener != null )
                        {
                            actionHandler.detach( actionHandlerListener );
                        }
                        
                        actionHandler = null;
                    }
                    
                    if( actionHandlerId == null )
                    {
                        actionHandler = action.getFirstActiveHandler();
                    }
                    else
                    {
                        for( SapphireActionHandler h : action.getActiveHandlers() )
                        {
                            if( h.getId().equalsIgnoreCase( actionHandlerId ) )
                            {
                                actionHandler = h;
                                break;
                            }
                        }
                    }
                    
                    actionHandlerRef.set( actionHandler );

                    if( actionHandler != null )
                    {
                        Listener actionHandlerListener = actionHandlerListenerRef.get();
                        
                        if( actionHandlerListener == null )
                        {
                            actionHandlerListener = new Listener()
                            {
                                @Override
                                public void handle( final Event event )
                                {
                                    if( event instanceof EnablementChangedEvent )
                                    {
                                        refreshEnablementStateOp.run();
                                    }
                                }
                            };
                            
                            actionHandlerListenerRef.set( actionHandlerListener );
                        }
                        
                        actionHandler.attach( actionHandlerListener );
                    }
                    
                    refreshEnablementStateOp.run();
                }
            };
            
            resolveHandlerOp.run();
            
            final Listener actionListener = new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof HandlersChangedEvent )
                    {
                        resolveHandlerOp.run();
                    }
                }
            };
            
            action.attach( actionListener );
            
            text.addDisposeListener
            (
                new DisposeListener()
                {
                    public void widgetDisposed( final DisposeEvent event )
                    {
                        action.detach( actionListener );
                        
                        final SapphireActionHandler actionHandler = actionHandlerRef.get();
                        final Listener actionHandlerListener = actionHandlerListenerRef.get();
                        
                        if( actionHandler != null && actionHandlerListener != null )
                        {
                            actionHandler.detach( actionHandlerListener );
                        }
                    }
                }
            );
            
            text.addHyperlinkListener
            (
                new HyperlinkAdapter()
                {
                    @Override
                    public void linkActivated( final HyperlinkEvent event )
                    {
                        final SapphireActionHandler handler = actionHandlerRef.get();
                        
                        if( handler != null )
                        {
                            handler.execute( context );
                        }
                    }
                }
            );
        }
        else
        {
            if( imageLabel != null )
            {
                imageLabel.setEnabled( false );
            }
            
            text.setEnabled( false );
        }
    }

    @Override
    public Set<String> getActionContexts()
    {
        return Collections.singleton( SapphireActionSystem.CONTEXT_ACTION_LINK );
    }

}
