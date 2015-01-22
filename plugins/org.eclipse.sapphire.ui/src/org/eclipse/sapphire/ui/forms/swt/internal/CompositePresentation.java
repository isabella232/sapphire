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

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhhint;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;

import java.util.List;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ui.def.ISapphireDocumentation;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationRef;
import org.eclipse.sapphire.ui.forms.CompositeDef;
import org.eclipse.sapphire.ui.forms.CompositePart;
import org.eclipse.sapphire.ui.forms.ContainerPart.VisibleChildrenEvent;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.MasterDetailsContentNodePart;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.sapphire.ui.forms.swt.HelpSystem;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class CompositePresentation extends FormComponentPresentation
{
    private ScrolledComposite scrolledComposite;
    private Composite innerComposite;
    private List<FormComponentPresentation> children;
    
    public CompositePresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
    }

    @Override
    public CompositePart part()
    {
        return (CompositePart) super.part();
    }
    
    @Override
    public void render()
    {
        final CompositePart part = part();
        final CompositeDef def = part.definition();
        
        final boolean indent = ( def.getIndent().content() && ! ( part.parent() instanceof MasterDetailsContentNodePart ) );
        
        if( indent )
        {
            final Label label = new Label( composite(), SWT.NONE );
            label.setLayoutData( gd() );
            
            register( label );
        }

        final boolean scaleVertically = def.getScaleVertically().content(); 
        final int width = part.getWidth( -1 );
        final int height = part.getHeight( -1 );
        
        final Composite outerComposite = renderOuterComposite( gdwhint( gdhhint( gdhspan( ( scaleVertically ? gdfill() : gdhfill() ), ( indent ? 1 : 2 ) ), height ), width ) );
        
        render( outerComposite );
    }
    
    protected void render( final Composite composite )
    {
        final CompositePart part = part();
        final CompositeDef def = part.definition();
        
        composite.setBackground( resources().color( part.getBackgroundColor() ) );
        composite.setBackgroundMode( SWT.INHERIT_DEFAULT );
        composite.setLayout( glayout( 1, 0, 0 ) );
        
        final boolean scrollVertically = def.getScrollVertically().content();
        final boolean scrollHorizontally = def.getScrollHorizontally().content();
        
        if( scrollVertically || scrollHorizontally )
        {
            final int style
                = ( scrollVertically ? SWT.V_SCROLL : SWT.NONE ) | 
                  ( scrollHorizontally ? SWT.H_SCROLL : SWT.NONE );
            
            this.scrolledComposite = new ScrolledComposite( composite, style );
            this.scrolledComposite.setExpandHorizontal( true );
            this.scrolledComposite.setExpandVertical( true );
            
            // ScrolledComposite does not seem to inherit background color like other controls, so
            // we need to set it explicitly.
            
            this.scrolledComposite.setBackground( composite.getBackground() );
            this.scrolledComposite.setBackgroundMode( SWT.INHERIT_DEFAULT );
        }
        
        final int marginLeft = def.getMarginLeft().content();
        final int marginRight = def.getMarginRight().content();
        final int marginTop = def.getMarginTop().content();
        final int marginBottom = def.getMarginBottom().content();
        
        this.innerComposite = new Composite( ( this.scrolledComposite != null ? this.scrolledComposite : composite ), SWT.NONE ) {
            public Point computeSize (int wHint, int hHint, boolean changed) {
                if (this.getChildren().length == 0) {
                    return new Point(0, 0);
                }
                return super.computeSize(wHint, hHint, changed);
            }
        };
        
        this.innerComposite.setLayout( glayout( 2, marginLeft, marginRight, marginTop, marginBottom ) );
        
        if( this.scrolledComposite != null )
        {
            this.scrolledComposite.setContent( this.innerComposite );
            this.scrolledComposite.setLayoutData( gdfill() );
        }
        else
        {
            this.innerComposite.setLayoutData( gdfill() );
        }
        
        final ISapphireDocumentation doc = def.getDocumentation().content();
        
        if( doc != null )
        {
            ISapphireDocumentationDef docdef = null;
            
            if( doc instanceof ISapphireDocumentationDef )
            {
                docdef = (ISapphireDocumentationDef) doc;
            }
            else
            {
                docdef = ( (ISapphireDocumentationRef) doc ).resolve();
            }
            
            if( docdef != null )
            {
                HelpSystem.setHelp( this.innerComposite, docdef );
            }
        }
        
        refresh();
        
        attachPartListener
        (
            new FilteredListener<VisibleChildrenEvent>()
            {
                @Override
                protected void handleTypedEvent( final VisibleChildrenEvent event )
                {
                    refresh();
                }
            }
        );
    }

    protected Composite renderOuterComposite( final GridData gd )
    {
        final Composite composite = new Composite( composite(), SWT.NONE );
        composite.setLayoutData( gd );
        composite.setBackground( resources().color( part().getBackgroundColor() ) );
        composite.setBackgroundMode( SWT.INHERIT_DEFAULT );
        
        register( composite );
        
        return composite;
    }
    
    protected void renderChildren( final Composite composite )
    {
        final ListFactory<FormComponentPresentation> childrenListFactory = ListFactory.start();
        
        for( final FormComponentPart child : part().children().visible() )
        {
            childrenListFactory.add( child.createPresentation( this, composite ) );
        }
        
        this.children = childrenListFactory.result();
        
        for( final FormComponentPresentation child : this.children )
        {
            if( ! child.disposed() )
            {
                child.render();
            }
        }
    }

    @Override
    public void refresh()
    {
        boolean needToLayout = false;
        
        if( this.children != null )
        {
            for( final FormComponentPresentation child : this.children )
            {
                child.dispose();
            }
            
            this.children = null;
            needToLayout = true;
        }
        
        for( final Control control : this.innerComposite.getChildren() )
        {
            control.dispose();
        }
        
        renderChildren( this.innerComposite );
        
        if( this.scrolledComposite != null )
        {
            this.scrolledComposite.setMinSize( this.innerComposite.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
        }
        
        if( needToLayout )
        {
            layout();
        }
    }
    
    @Override
    public void dispose()
    {
        if( this.children != null )
        {
            for( final FormComponentPresentation child : this.children )
            {
                child.dispose();
            }
            
            this.children = null;
        }
        
        this.scrolledComposite = null;
        this.innerComposite = null;
        
        super.dispose();
    }

}
