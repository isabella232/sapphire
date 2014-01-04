/******************************************************************************
 * Copyright (c) 2014 Oracle
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
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
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
import org.eclipse.sapphire.ui.forms.swt.internal.text.SapphireFormText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ActuatorLinkPresentation extends ActuatorPresentation
{
    @Text( "Actuator label not specified" )
    private static LocalizableText labelNotSpecified;
    
    static
    {
        LocalizableText.init( ActuatorLinkPresentation.class );
    }

    private SapphireActionPresentationManager actionPresentationManager;
    
    public ActuatorLinkPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
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
        
        final ImageData image = part.image( 16 );
        
        final Composite composite = new Composite( composite(), SWT.NONE );
        composite.setLayoutData( gdhalign( gdhindent( gdhspan( gd(), hSpan ), 8 ), hAlignCode ) );
        composite.setLayout( glayout( ( image == null ? 1 : 2 ), 0, 0 ) );
        
        register( composite );

        final Label imageControl;
        
        if( image != null )
        {
            imageControl = new Label( composite, SWT.NONE );
            imageControl.setImage( resources().image( image ) );
            imageControl.setLayoutData( gdvalign( gd(), SWT.CENTER ) );
            imageControl.setEnabled( part.enabled() );
        }
        else
        {
            imageControl = null;
        }
        
        final SapphireFormText text = new SapphireFormText( composite, SWT.NONE );
        text.setLayoutData( gdvalign( gdhfill(), SWT.CENTER ) );
        
        keyboardActionPresentation.attach( text );
        
        String label = part.label( CapitalizationType.FIRST_WORD_ONLY );
        label = ( label == null ? labelNotSpecified.text() : label );
        
        final StringBuilder buf = new StringBuilder();
        buf.append( "<form><p vspace=\"false\"><a href=\"action\" nowrap=\"true\">" );
        buf.append( label );
        buf.append( "</a></p></form>" );
        
        text.setText( buf.toString(), true, false );
        
        text.addHyperlinkListener
        (
            new HyperlinkAdapter()
            {
                @Override
                public void linkActivated( final HyperlinkEvent event )
                {
                    final SapphireActionHandler handler = part.handler();
                    
                    if( handler != null )
                    {
                        handler.execute( ActuatorLinkPresentation.this );
                    }
                }
            }
        );
        
        text.setEnabled( part.enabled() );
        
        attachPartListener
        (
            new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof EnablementChangedEvent )
                    {
                        final boolean enabled = part.enabled();
                        
                        if( imageControl != null )
                        {
                            imageControl.setEnabled( enabled );
                        }
                        
                        text.setEnabled( enabled );
                    }
                    else if( event instanceof LabelChangedEvent )
                    {
                        final StringBuilder buf = new StringBuilder();
                        buf.append( "<form><p vspace=\"false\"><a href=\"action\" nowrap=\"true\">" );
                        buf.append( part.label( CapitalizationType.FIRST_WORD_ONLY ) );
                        buf.append( "</a></p></form>" );
                        
                        text.setText( buf.toString(), true, false );
                        
                        composite.getParent().layout( true, true );
                    }
                    else if( event instanceof ImageChangedEvent )
                    {
                        if( imageControl != null )
                        {
                            imageControl.setImage( resources().image( part.image( 16 ) ) );
                        }
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
