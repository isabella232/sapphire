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
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;

import org.eclipse.sapphire.ui.def.Orientation;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.SplitFormPart;
import org.eclipse.sapphire.ui.forms.SplitFormSectionPart;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SplitFormSectionPresentation extends CompositePresentation
{
    private Composite control;
    
    public SplitFormSectionPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
    }

    @Override
    public SplitFormSectionPart part()
    {
        return (SplitFormSectionPart) super.part();
    }
    
    @Override
    public void render()
    {
        final SplitFormSectionPart part = part();
        final SplitFormPart parent = part.parent();
        final Orientation orientation = parent.orientation();
        final int sectionCount = parent.children().all().size();
        final int sectionIndex = parent.children().all().indexOf( part );
        
        this.control = new Composite( composite(), SWT.NONE );

        register( this.control );
        
        this.control.setBackground( resources().color( part.getBackgroundColor() ) );
        this.control.setBackgroundMode( SWT.INHERIT_DEFAULT );
        
        this.control.setLayout
        (
            glayout
            (
                1,
                0,
                ( sectionIndex < sectionCount - 1 && orientation == Orientation.HORIZONTAL ? 4 : 0 ),
                ( sectionIndex > 0 && orientation == Orientation.VERTICAL ? 1 : 0 ),
                ( sectionIndex < sectionCount - 1 && orientation == Orientation.VERTICAL ? 1 : 0 )
            )
        );
        
        final Composite innerComposite = new Composite( this.control, SWT.NONE );
        innerComposite.setLayoutData( gdfill() );
        
        render( innerComposite );
    }
    
    public Composite control()
    {
        return this.control;
    }

    @Override
    public void dispose()
    {
        this.control = null;
        
        super.dispose();
    }

}
