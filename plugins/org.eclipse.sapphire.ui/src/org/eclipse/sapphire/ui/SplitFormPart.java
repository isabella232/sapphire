/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [bugzilla 329114] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.ui.def.Orientation;
import org.eclipse.sapphire.ui.def.SplitFormBlockDef;
import org.eclipse.sapphire.ui.def.SplitFormDef;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SplitFormPart extends SapphirePartContainer
{
    @Override
    protected List<SapphirePart> initChildParts()
    {
        final IModelElement element = getLocalModelElement();
        final List<SapphirePart> childParts = new ArrayList<SapphirePart>();
        
        for( SplitFormBlockDef splitFormBlockDef : definition().getBlocks() )
        {
            final SapphirePart childPart = create( this, element, splitFormBlockDef, this.params );
            childParts.add( childPart );
        }
        
        return childParts;
    }

    @Override
    public SplitFormDef definition()
    {
        return (SplitFormDef) super.definition();
    }
    
    public Orientation getOrientation()
    {
        return definition().getOrientation().getContent();
    }
    
    @Override
    @SuppressWarnings( "unchecked" )
    
    public List<SplitFormBlockPart> getChildParts()
    {
        return (List<SplitFormBlockPart>) super.getChildParts();
    }

    @Override
    public final void render( final SapphireRenderingContext context )
    {
        final SashForm form = new SashForm( context.getComposite(), ( getOrientation() == Orientation.HORIZONTAL ? SWT.HORIZONTAL : SWT.VERTICAL ) | SWT.SMOOTH );
        form.setLayoutData( gdhspan( ( getScaleVertically() ? gdfill() : gdhfill() ), 2 ) );
        context.adapt( form );
        
        final List<SplitFormBlockPart> blockParts = getChildParts();
        final int blockPartsCount = blockParts.size();
        final int[] weights = new int[ blockPartsCount ];
        
        for( int i = 0; i < blockPartsCount; i++ )
        {
            final SplitFormBlockPart block = blockParts.get( i );
            final Composite blockComposite = new Composite( form, SWT.NONE );
            context.adapt( blockComposite );
            final SapphireRenderingContext blockContext = new SapphireRenderingContext( this, context, blockComposite );
            
            final int rightMargin = ( i < blockPartsCount - 1 && getOrientation() == Orientation.HORIZONTAL ? 4 : 0 );
            final int bottomMargin = ( i < blockPartsCount - 1 && getOrientation() == Orientation.VERTICAL ? 1 : 0 );
            final int topMargin = ( i > 0 && getOrientation() == Orientation.VERTICAL ? 1 : 0 );
            blockComposite.setLayout( glayout( 2, 0, rightMargin, topMargin, bottomMargin ) );
            
            block.render( blockContext );
            
            weights[ i ] = block.getWeight();
        }
        
        form.setWeights( weights );
    }
    
}
