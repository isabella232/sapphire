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
import static org.eclipse.sapphire.ui.util.SwtUtil.gdhhint;
import static org.eclipse.sapphire.ui.util.SwtUtil.glayout;
import static org.eclipse.sapphire.ui.util.SwtUtil.hspan;

import org.eclipse.sapphire.ui.def.ISapphireSpacerDef;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireSpacer

    extends SapphirePart
    
{
    @Override
    public void render( final SapphireRenderingContext context )
    {
        final ISapphireSpacerDef def = (ISapphireSpacerDef) this.definition;
        
        final Composite spacer = new Composite( context.getComposite(), SWT.NONE );
        spacer.setLayoutData( gdhhint( hspan( gdhfill(), 2 ), def.getSize().getContent() ) );
        spacer.setLayout( glayout( 1, 0, 0, 0, 0 ) );
        context.adapt( spacer );
    }
    
}
