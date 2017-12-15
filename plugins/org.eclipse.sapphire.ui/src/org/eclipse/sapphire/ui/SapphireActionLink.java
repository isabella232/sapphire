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

import static org.eclipse.sapphire.ui.util.SwtUtil.gd;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdhfill;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdhindent;
import static org.eclipse.sapphire.ui.util.SwtUtil.glayout;
import static org.eclipse.sapphire.ui.util.SwtUtil.hspan;
import static org.eclipse.sapphire.ui.util.SwtUtil.valign;

import org.eclipse.sapphire.ui.actions.Action;
import org.eclipse.sapphire.ui.def.ISapphireActionLinkDef;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireActionLink

    extends SapphirePart
    
{
    private Action action = null;
    
    public void render( final SapphireRenderingContext context )
    {
        final ISapphireActionLinkDef def = (ISapphireActionLinkDef) this.definition;        
        
        if( def.getImplClass().getText() != null )
        {
            final Class<?> actionClass = def.getImplClass().resolve();
            Action actionTemp = null;
            
            try
            {
                actionTemp = (Action) actionClass.newInstance();
            }
            catch( Exception e )
            {
                SapphireUiFrameworkPlugin.log( e );
            }
            
            this.action = actionTemp;

            if( this.action != null )
            {
                this.action.setPart( this );
            }
        }
        else
        {
            this.action = getAction( def.getActionId().getText() );
        }
        
        Image image = null;
        
        if( def.getShowImage().getContent() == true && this.action != null )
        {
            image = this.action.getImage();
        }
        
        final Composite composite = new Composite( context.getComposite(), SWT.NONE );
        composite.setLayout( glayout( ( image == null ? 1 : 2 ), 0, 0 ) );
        composite.setLayoutData( gdhindent( hspan( gd(), 2 ), 8 ) );
        
        if( image != null )
        {
            final Label label = new Label( composite, SWT.NONE );
            label.setImage( image );
            label.setLayoutData( valign( gd(), SWT.CENTER ) );
        }
        
        final FormText text = new FormText( composite, SWT.NONE );
        text.setLayoutData( valign( gdhfill(), SWT.CENTER ) );
        context.adapt( text );
        
        final StringBuilder buf = new StringBuilder();
        buf.append( "<form><p vspace=\"false\"><a href=\"action\" nowrap=\"true\">" );
        buf.append( def.getLabel().getLocalizedText() );
        buf.append( "</a></p></form>" );
        
        text.setText( buf.toString(), true, false );
        
        if( this.action != null )
        {
            text.addHyperlinkListener
            (
                new HyperlinkAdapter()
                {
                    @Override
                    public void linkActivated( final HyperlinkEvent event )
                    {
                        SapphireActionLink.this.action.execute( text.getShell() );
                    }
                }
            );
        }
    }

}
