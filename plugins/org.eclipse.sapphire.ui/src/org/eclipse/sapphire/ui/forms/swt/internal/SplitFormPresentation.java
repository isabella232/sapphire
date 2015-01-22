/******************************************************************************
 * Copyright (c) 2015 Oracle
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
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;

import java.util.List;

import org.eclipse.sapphire.ui.def.Orientation;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.SplitFormDef;
import org.eclipse.sapphire.ui.forms.SplitFormPart;
import org.eclipse.sapphire.ui.forms.SplitFormSectionPart;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SplitFormPresentation extends FormComponentPresentation
{
    private SashForm form;
    private List<SplitFormSectionPresentation> children;
    
    public SplitFormPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
    }

    @Override
    public SplitFormPart part()
    {
        return (SplitFormPart) super.part();
    }
    
    @Override
    public final void render()
    {
        final SplitFormPart part = part();
        final SplitFormDef def = part.definition();
        
        final int formMarginLeft = def.getMarginLeft().content();
        final int formMarginRight = def.getMarginRight().content();
        final int formMarginTop = def.getMarginTop().content();
        final int formMarginBottom = def.getMarginBottom().content();
        
        final Composite formMarginsComposite = new Composite( composite(), SWT.NONE );
        formMarginsComposite.setLayout( glayout( 1, formMarginLeft, formMarginRight, formMarginTop, formMarginBottom ) );
        formMarginsComposite.setLayoutData( gdhspan( ( part.getScaleVertically() ? gdfill() : gdhfill() ), 2 ) );
        
        formMarginsComposite.setBackground( resources().color( part.getBackgroundColor() ) );
        formMarginsComposite.setBackgroundMode( SWT.INHERIT_DEFAULT );
        
        this.form = new SashForm( formMarginsComposite, ( part.orientation() == Orientation.HORIZONTAL ? SWT.HORIZONTAL : SWT.VERTICAL ) | SWT.SMOOTH );
        
        register( this.form );
        
        this.form.setLayoutData( gdfill() );
        this.form.setBackground( resources().color( part.getBackgroundColor() ) );
        this.form.setBackgroundMode( SWT.INHERIT_DEFAULT );
        
        final ListFactory<SplitFormSectionPresentation> childrenListFactory = ListFactory.start();
        
        for( final SplitFormSectionPart section : part().children().all() )
        {
            childrenListFactory.add( section.createPresentation( this, form ) );
        }
        
        this.children = childrenListFactory.result();
        
        for( final SplitFormSectionPresentation child : this.children )
        {
            child.render();
        }
        
        final int[] weights = new int[ this.children.size() ];
        
        for( int i = 0, n = weights.length; i < n; i++ )
        {
            weights[ i ] = this.children.get( i ).part().weight();
        }
        
        this.form.setWeights( weights );
    }

    @Override
    public void dispose()
    {
        if( this.children != null )
        {
            final Orientation orientation = part().orientation();
            
            for( final SplitFormSectionPresentation child : this.children )
            {
                final Point size = child.control().getSize();
                final int weight = ( orientation == Orientation.HORIZONTAL ? size.x : size.y );
                
                if( weight != 0 )
                {
                    child.part().weight( orientation == Orientation.HORIZONTAL ? size.x : size.y );
                }
                
                child.dispose();
            }
            
            this.children = null;
        }
        
        this.form = null;
        
        super.dispose();
    }

}
