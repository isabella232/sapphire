/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [329102] excess scroll space in editor sections
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.swt.renderer.SwtUtil.reflowOnResize;

import java.util.Collections;
import java.util.Set;

import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.ui.def.ISapphireLabelDef;
import org.eclipse.sapphire.ui.swt.SapphireTextPopup;
import org.eclipse.sapphire.ui.swt.renderer.SapphireActionPresentationManager;
import org.eclipse.sapphire.ui.swt.renderer.SapphireKeyboardActionPresentation;
import org.eclipse.sapphire.ui.swt.renderer.internal.formtext.SapphireFormText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class LabelPart extends SapphirePart
{
    private final static String BREAK_TOKEN = "###brk###";
    
    private SapphireFormText text;
    private String labelExtendedContent;
    private FunctionResult labelFunctionResult;
    
    @Override
    public Set<String> getActionContexts()
    {
        return Collections.singleton( SapphireActionSystem.CONTEXT_LABEL );
    }

    @Override
    public void render( final SapphireRenderingContext context )
    {
        final ISapphireLabelDef def = (ISapphireLabelDef) this.definition;
        
        this.text = new SapphireFormText( context.getComposite(), SWT.NONE );
        this.text.setLayoutData( gdhindent( gdwhint( gdhspan( gdhfill(), 2 ), 100 ), 9 ) );
        context.adapt( this.text );
        
        final SapphireActionGroup actions = getActions( getMainActionContext() );
        final SapphireActionPresentationManager actionPresentationManager = new SapphireActionPresentationManager( context, actions );
        final SapphireKeyboardActionPresentation keyboardActionPresentation = new SapphireKeyboardActionPresentation( actionPresentationManager );
        
        keyboardActionPresentation.attach( this.text );
        keyboardActionPresentation.render();
        
        this.labelFunctionResult = initExpression
        (
            getModelElement(),
            def.getText().getContent(), 
            String.class,
            null,
            new Runnable()
            {
                public void run()
                {
                    refreshLabel();
                }
            }
        );

        this.text.addHyperlinkListener
        (
            new HyperlinkAdapter()
            {
                @Override
                public void linkActivated( final HyperlinkEvent event )
                {
                    activateExtendedDescriptionContentPopup();
                }
            }
        );

        refreshLabel();

        reflowOnResize( this.text );
        
        this.text.addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    actionPresentationManager.dispose();
                }
            }
        );
    }
    
    private void refreshLabel()
    {
        String description = null;
        
        if( this.labelFunctionResult != null )
        {
            description = (String) this.labelFunctionResult.value();
        }

        if( description == null )
        {
            description = "#null#";
        }
        else
        {
            description = description.trim();
        }
        
        description = description.replace( "<", "&lt;" );
    
        final int index = description.indexOf( BREAK_TOKEN );
        
        final StringBuilder buf = new StringBuilder();
        buf.append( "<form><p vspace=\"false\">");
    
        if( index > 0 ) 
        {
            final String displayDescription = description.substring( 0, index );
    
            buf.append( displayDescription );
            if (!displayDescription.endsWith(" ")) {
                buf.append(" ");
            }
            buf.append( "<a href=\"action\" nowrap=\"true\">");
            buf.append( Resources.moreDetails );
            buf.append( "</a>" );
            
            this.labelExtendedContent = displayDescription + description.substring( index + BREAK_TOKEN.length(), description.length() );
        }  
        else 
        {
            buf.append( description );
            this.labelExtendedContent = null;
        }
        
        buf.append( "</p></form>" );
        this.text.setText( buf.toString(), true, false );
    }

    private void activateExtendedDescriptionContentPopup()
    {
        if( this.labelExtendedContent != null )
        {
            final Point cursor = this.text.getDisplay().getCursorLocation();
            final Rectangle bounds = this.text.getBounds();
            final Point location = this.text.toDisplay( new Point( bounds.x, bounds.y ) );
            final Rectangle displayBounds = new Rectangle( location.x, location.y, bounds.width, bounds.height );
            
            Point position;
            
            if( displayBounds.contains( cursor ) ) 
            {
                position = cursor;
            } 
            else 
            {
                System.out.println("not in displayBounds: ");
                position = cursor;
                //position = new Point( location.x, location.y + bounds.height + 2 );
            }
            
            final SapphireTextPopup popup = new SapphireTextPopup( this.text.getShell(), position );
            popup.setText( this.labelExtendedContent );
            popup.open();
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String moreDetails;
        
        static
        {
            initializeMessages( LabelPart.class.getName(), Resources.class );
        }
    }

}
