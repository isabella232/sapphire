/******************************************************************************
 * Copyright (c) 2015 Oracle and Modelity Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Roded Bahat - [374821] Support group with no label
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt.internal;

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdvindent;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.modeling.util.MiscUtil;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.GroupPart;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:rodedb@gmail.com">Roded Bahat</a>
 */

public final class GroupPresentation extends CompositePresentation
{
    public GroupPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
    }

    @Override
    public GroupPart part()
    {
        return (GroupPart) super.part();
    }
    
    @Override
    protected Composite renderOuterComposite( final GridData gd )
    {
        String label = MiscUtil.normalizeToEmptyString( part().definition().getLabel().localized() );
        
        if( label.length() != 0 ) 
        {
            label = LabelTransformer.transform( label, CapitalizationType.FIRST_WORD_ONLY, true );
        }
        
        final Group group = new Group( composite(), SWT.NONE );
        group.setLayoutData( gdhindent( gdvindent( gd, 5 ), 10 ) );
        group.setLayout( glayout( 1, 5, 5, 8, 5 ) );
        group.setText( label );
        
        group.setBackground( resources().color( part().getBackgroundColor() ) );
        group.setBackgroundMode( SWT.INHERIT_DEFAULT );
        
        register( group );
        
        final Composite composite = new Composite( group, SWT.NONE );
        composite.setLayoutData( gdfill() );
            
        return composite;
    }

}
