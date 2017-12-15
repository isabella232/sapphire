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

import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.ui.def.ISapphireSeparatorDef;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireSeparator

    extends SapphirePart
    
{
    @Override
    public void render( final SapphireRenderingContext context )
    {
        final ISapphireSeparatorDef def = (ISapphireSeparatorDef) this.definition;
        
        final Composite separatorComposite = new Composite( context.getComposite(), SWT.NONE );
        separatorComposite.setLayoutData( gdhindent( gdhspan( gdhfill(), 2 ), 10 ) );
        separatorComposite.setLayout( glayout( 1, 0, 5 ) );
        context.adapt( separatorComposite );
        
        final String label = def.getLabel().getLocalizedText( CapitalizationType.TITLE_STYLE, false );
        
        if( label != null )
        {
            final Label l = new Label( separatorComposite, SWT.WRAP );
            l.setLayoutData( gd() );
            l.setText( label );
            context.adapt( l );
        }
        
        final Label separator = new Label( separatorComposite, SWT.SEPARATOR | SWT.HORIZONTAL );
        separator.setLayoutData( gdhfill() );
        context.adapt( separator );
    }
    
}
