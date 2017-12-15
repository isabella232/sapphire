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
import static org.eclipse.sapphire.ui.util.SwtUtil.gdvindent;
import static org.eclipse.sapphire.ui.util.SwtUtil.glayout;
import static org.eclipse.sapphire.ui.util.SwtUtil.hspan;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.LabelTransformer;
import org.eclipse.sapphire.ui.def.ISapphireGroupDef;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class SapphireGroup

    extends SapphireComposite
    
{
    @Override
    protected Composite createOuterComposite( final SapphireRenderingContext context )
    {
        final ISapphireGroupDef def = (ISapphireGroupDef) this.definition;
        
        final String label = LabelTransformer.transform( def.getLabel().getLocalizedText(), CapitalizationType.FIRST_WORD_ONLY, true );
        
        final Group group = new Group( context.getComposite(), SWT.NONE );
        group.setLayoutData( gdhindent( gdvindent( hspan( gdhfill(), 2 ), 5 ), 10 ) );
        group.setLayout( glayout( 2, 5, 5, 8, 5 ) );
        group.setText( label );
        context.adapt( group );
            
        return group;
    }
    
}
