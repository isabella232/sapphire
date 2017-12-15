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

import static org.eclipse.sapphire.ui.util.SwtUtil.gdhfill;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdhindent;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdwhint;
import static org.eclipse.sapphire.ui.util.SwtUtil.hspan;

import org.eclipse.sapphire.ui.def.ISapphireLabelDef;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireLabel

    extends SapphirePart
    
{
    @Override
    public void render( final SapphireRenderingContext context )
    {
        final ISapphireLabelDef def = (ISapphireLabelDef) this.definition;
        
        final Label l = new Label( context.getComposite(), SWT.WRAP );
        l.setLayoutData( gdhindent( gdwhint( hspan( gdhfill(), 2 ), 100 ), 9 ) );
        l.setText( def.getText().getLocalizedText() );
        context.adapt( l );
    }
    
}
