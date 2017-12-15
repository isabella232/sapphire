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

import org.eclipse.sapphire.ui.def.ISapphireStaticTextFieldDef;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireStaticTextField

    extends SapphirePart
    
{
    @Override
    public void render( final SapphireRenderingContext context )
    {
        final ISapphireStaticTextFieldDef def = (ISapphireStaticTextFieldDef) this.definition;
        final Composite parent = context.getComposite();
        
        final Label l = new Label( parent, SWT.NONE );
        l.setLayoutData( gd() );
        l.setText( def.getLabel().getLocalizedText() );
        
        final Text t = new Text( parent, SWT.READ_ONLY | SWT.BORDER );
        t.setLayoutData( gdhindent( gdhfill(), 10 ) );
        t.setText( def.getText().getText() );
    }
    
}
