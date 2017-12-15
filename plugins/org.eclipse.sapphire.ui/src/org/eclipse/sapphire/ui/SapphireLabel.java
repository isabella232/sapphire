/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - Bugzilla 329102 -  excess scroll space in editor sections
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdwhint;

import org.eclipse.sapphire.ui.def.ISapphireLabelDef;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.SharedScrolledComposite;

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
        
        final GridData gd = gdhindent( gdwhint( gdhspan( gdhfill(), 2 ), 100 ), 9 ); 
        final Label l = new Label( context.getComposite(), SWT.WRAP );
        l.setLayoutData( gd );
        l.setText( def.getText().getLocalizedText() );
        context.adapt( l );
        
        l.addControlListener(new ControlListener() {
            public void controlMoved(ControlEvent e) {
            }
            public void controlResized(ControlEvent e) {
                if (l.getBounds().width != gd.widthHint + 20) {
                	if (l.getBounds().width == gd.widthHint) {
                        gd.widthHint = 100; // reset widthHint to original size
                	} else {
                        gd.widthHint = l.getBounds().width - 20;
                	}
                    relayout(context.getComposite());
                }
            }
        });
    }
    
    private void relayout(final Composite composite) {
        composite.getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (composite.isDisposed())
                    return;
                Composite parent = composite;
                while (parent != null) {
                    if (parent instanceof SharedScrolledComposite) {
                        parent.layout(true, true);
                        ((SharedScrolledComposite)parent).reflow(true);
                        return;
                    } else if (parent instanceof Shell) {
                        parent.layout(true, true);
                        return;
                    }
                    parent = parent.getParent();
                }
            }
        });
    }
    
}
