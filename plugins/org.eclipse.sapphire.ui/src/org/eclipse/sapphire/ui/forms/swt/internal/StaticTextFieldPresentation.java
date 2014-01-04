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

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhindent;

import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.StaticTextFieldDef;
import org.eclipse.sapphire.ui.forms.StaticTextFieldPart;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StaticTextFieldPresentation extends FormComponentPresentation
{
    public StaticTextFieldPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
    }

    @Override
    public StaticTextFieldPart part()
    {
        return (StaticTextFieldPart) super.part();
    }
    
    @Override
    public void render()
    {
        final StaticTextFieldDef def = part().definition();
        final Composite parent = composite();
        
        final Label l = new Label( parent, SWT.NONE );
        l.setLayoutData( gd() );
        l.setText( def.getLabel().localized() );
        
        register( l );
        
        final Text t = new Text( parent, SWT.READ_ONLY | SWT.BORDER );
        t.setLayoutData( gdhindent( gdhfill(), 10 ) );
        t.setText( def.getText().text() );
        
        register( t );
    }

}
