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

package org.eclipse.sapphire.ui.forms.swt.internal;

import static org.eclipse.sapphire.modeling.util.MiscUtil.EMPTY_STRING;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhalign;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhspan;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphirePart.ImageChangedEvent;
import org.eclipse.sapphire.ui.SapphirePart.LabelChangedEvent;
import org.eclipse.sapphire.ui.def.HorizontalAlignment;
import org.eclipse.sapphire.ui.forms.ActuatorDef;
import org.eclipse.sapphire.ui.forms.ActuatorPart;
import org.eclipse.sapphire.ui.forms.ActuatorPart.EnablementChangedEvent;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.swt.SapphireActionPresentationManager;
import org.eclipse.sapphire.ui.forms.swt.SapphireKeyboardActionPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ActuatorButtonPresentation extends ActuatorPresentation
{
    private SapphireActionPresentationManager actionPresentationManager;
    
    public ActuatorButtonPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
    }

    @Override
    public void render()
    {
        final ActuatorPart part = part();
        final ActuatorDef def = part.definition();
        
        final SapphireActionGroup actions = part.getActions();
        this.actionPresentationManager = new SapphireActionPresentationManager( this, actions );
        final SapphireKeyboardActionPresentation keyboardActionPresentation = new SapphireKeyboardActionPresentation( this.actionPresentationManager );
        
        final HorizontalAlignment hAlign = def.getHorizontalAlignment().content();
        final int hAlignCode = ( hAlign == HorizontalAlignment.LEFT ? SWT.LEFT : ( hAlign == HorizontalAlignment.RIGHT ? SWT.RIGHT : SWT.CENTER ) );
        
        final int hSpan = ( def.getSpanBothColumns().content() ? 2 : 1 );
        
        if( hSpan == 1 )
        {
            final Label spacer = new Label( composite(), SWT.NONE );
            spacer.setLayoutData( gd() );
            spacer.setText( EMPTY_STRING );
            
            register( spacer );
        }
        
        final Button button = new Button( composite(), SWT.PUSH );
        button.setLayoutData( gdhspan( gdhindent( gdhalign( gd(), hAlignCode ), 8 ), hSpan ) );
        
        register( button );
        
        keyboardActionPresentation.attach( button );
        
        final String label = part.label( CapitalizationType.TITLE_STYLE, true );
        
        if( label != null )
        {
            button.setText( label );
        }
        
        final ImageData image = part.image( 16 );
        
        if( image != null )
        {
            button.setImage( resources().image( image ) );
        }
        
        button.addSelectionListener
        (
            new SelectionAdapter()
            {
                @Override
                public void widgetSelected( SelectionEvent e )
                {
                    final SapphireActionHandler handler = part.handler();
                    
                    if( handler != null )
                    {
                        handler.execute( ActuatorButtonPresentation.this );
                    }
                }
            }
        );
        
        button.setEnabled( part.enabled() );
        
        attachPartListener
        (
            new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof EnablementChangedEvent )
                    {
                        button.setEnabled( part.enabled() );
                    }
                    else if( event instanceof LabelChangedEvent )
                    {
                        final String label = part.label( CapitalizationType.TITLE_STYLE );
                        button.setText( label == null ? EMPTY_STRING : label );
                        button.getParent().layout( true, true );
                    }
                    else if( event instanceof ImageChangedEvent )
                    {
                        button.setImage( resources().image( part.image( 16 ) ) );
                    }
                }
            }
        );
            
        keyboardActionPresentation.render();
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.actionPresentationManager != null )
        {
            this.actionPresentationManager.dispose();
            this.actionPresentationManager = null;
        }
    }

}
