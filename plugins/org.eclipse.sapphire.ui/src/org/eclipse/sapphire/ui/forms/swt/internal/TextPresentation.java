/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [329102] excess scroll space in editor sections
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt.internal;

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.forms.swt.SwtUtil.reflowOnResize;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.TextPart;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.text.SapphireFormText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public final class TextPresentation extends FormComponentPresentation
{
    public TextPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
    }

    @Override
    public TextPart part()
    {
        return (TextPart) super.part();
    }
    
    @Override
    public void render()
    {
        final SapphireFormText text = new SapphireFormText( composite(), SWT.NONE );
        text.setLayoutData( gdhindent( gdwhint( gdhspan( gdhfill(), 2 ), 100 ), 9 ) );
        
        attachPartListener
        (
            new FilteredListener<TextPart.ContentEvent>()
            {
                @Override
                protected void handleTypedEvent( TextPart.ContentEvent event )
                {
                    text.setText( part().content(), false, false );
                }
            }
        );
        
        text.setText( part().content(), false, false );
        
        reflowOnResize( text );
        
        register( text );
    }

}
