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

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhhint;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdwhint;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.ui.def.HtmlContentSourceType;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.HtmlPanelDef;
import org.eclipse.sapphire.ui.forms.HtmlPanelPart;
import org.eclipse.sapphire.ui.forms.HtmlPanelPart.ContentEvent;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.text.SapphireFormText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class HtmlPanelPresentation extends FormComponentPresentation
{
    @Text( "Could not initialize embedded browser." )
    private static LocalizableText couldNotInitializeBrowserMessage;

    static 
    {
        LocalizableText.init( HtmlPanelPresentation.class );
    }

    public HtmlPanelPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
    }

    @Override
    public HtmlPanelPart part()
    {
        return (HtmlPanelPart) super.part();
    }
    
    @Override
    public void render()
    {
        final HtmlPanelPart part = part();
        final HtmlPanelDef def = part.definition();
        
        final boolean scaleVertically = part.getScaleVertically();
        GridData gd = gdhindent( gdwhint( gdhspan( ( scaleVertically ? gdfill() : gdhfill() ), 2 ), 100 ), 9 );
        
        if( ! scaleVertically )
        {
            gd = gdhhint( gd, def.getHeight().content() );
        }
        
        final int style = ( def.getShowBorder().content() == true ? SWT.BORDER : SWT.NONE );
        
        try
        {
            final Browser browser = new Browser( composite(), style );
            browser.setLayoutData( gd );
            
            register( browser );
            
            if( def.getContentSourceType().content() == HtmlContentSourceType.EMBEDDED )
            {
                browser.setText( part.content() );
                
                attachPartListener
                (
                    new FilteredListener<ContentEvent>()
                    {
                        @Override
                        protected void handleTypedEvent( final ContentEvent event )
                        {
                            browser.setText( part.content() );
                        }
                    }
                );
            }
            else
            {
                browser.setUrl( part.url() );
                
                attachPartListener
                (
                    new FilteredListener<ContentEvent>()
                    {
                        @Override
                        protected void handleTypedEvent( final ContentEvent event )
                        {
                            browser.setUrl( part.url() );
                        }
                    }
                );
            }
        }
        catch( SWTError e )
        {
            final SapphireFormText text = new SapphireFormText( composite(), style );
            text.setText( couldNotInitializeBrowserMessage.text(), false, false );
            text.setLayoutData( gd );
            
            register( text );
        }
    }

}
