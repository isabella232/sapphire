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

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdwhint;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.PageBookCasePart;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.text.SapphireFormText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PageBookCasePresentation extends FormPresentation
{
    @Text( "No additional properties are currently available." )
    private static LocalizableText noAdditionalPropertiesMessage;
    
    static
    {
        LocalizableText.init( PageBookCasePresentation.class );
    }

    public PageBookCasePresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
    }

    @Override
    public PageBookCasePart part()
    {
        return (PageBookCasePart) super.part();
    }
    
    @Override
    public void render()
    {
        if( part().children().visible().isEmpty() )
        {
            final SapphireFormText text = new SapphireFormText( composite(), SWT.NONE );
            text.setLayoutData( gdhindent( gdwhint( gdhspan( gdhfill(), 2 ), 100 ), 9 ) );
            text.setText( noAdditionalPropertiesMessage.text(), false, false );
            
            register( text );
        }
        else
        {
            super.render();
        }
    }

}
