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

import java.util.List;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.ui.def.Orientation;
import org.eclipse.sapphire.ui.def.SplitFormBlockDef;
import org.eclipse.sapphire.ui.def.SplitFormDef;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SplitFormPart extends FormPart
{
    @Override
    protected List<SapphirePart> initChildParts()
    {
        final IModelElement element = getLocalModelElement();
        final ListFactory<SapphirePart> partsListFactory = ListFactory.start();
        
        for( SplitFormBlockDef splitFormBlockDef : definition().getBlocks() )
        {
            partsListFactory.add( create( this, element, splitFormBlockDef, this.params ) );
        }
        
        return partsListFactory.result();
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
            
            final Listener blockChildPartsListener = new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof PartVisibilityEvent || event instanceof PartChildrenEvent )
                    {
                        final SapphirePart part = ( (PartEvent) event ).part();
                        
                        if( event instanceof PartChildrenEvent && ! ( part instanceof CompositePart || part instanceof SplitFormBlockPart ) )
                        {
                            attachChildPartsListener( part, this );
                        }
                        
                        for( Control control : blockComposite.getChildren() )
                        {
                            control.dispose();
                        }
                        
                        block.render( blockContext );
                        context.layout();
                    }
                }
            };
            
            for( SapphirePart child : block.getChildParts() )
            {
                attachChildPartsListener( child, blockChildPartsListener );
            }
            
            blockComposite.addDisposeListener
            (
                new DisposeListener()
                {
                    public void widgetDisposed( final DisposeEvent event )
                    {
                        detachChildPartsListener( block, blockChildPartsListener );
                    }
                }
            );
            
            weights[ i ] = block.getWeight();
        }
        
        form.setWeights( weights );
    }
    
    private static void attachChildPartsListener( final SapphirePart part,
                                                  final Listener listener )
    {
        part.attach( listener );
        
        if( part instanceof FormPart && ! ( part instanceof CompositePart || part instanceof SplitFormBlockPart ) )
        {
            for( SapphirePart child : ( (FormPart) part ).getChildParts() )
            {
                attachChildPartsListener( child, listener );
            }
        }
        else if( part instanceof ConditionalPart )
        {
            for( SapphirePart child : ( (ConditionalPart) part ).getCurrentBranchContent() )
            {
                attachChildPartsListener( child, listener );
            }
        }
    }
    
    private static void detachChildPartsListener( final SapphirePart part,
                                                  final Listener listener )
    {
        part.detach( listener );
        
        if( part instanceof FormPart && ! ( part instanceof CompositePart || part instanceof SplitFormBlockPart ) )
        {
            for( SapphirePart child : ( (FormPart) part ).getChildParts() )
            {
                detachChildPartsListener( child, listener );
            }
        }
        else if( part instanceof ConditionalPart )
        {
            for( SapphirePart child : ( (ConditionalPart) part ).getCurrentBranchContent() )
            {
                detachChildPartsListener( child, listener );
            }
        }
    }
    
}
