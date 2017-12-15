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

import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhhint;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;

import java.util.Collections;
import java.util.Set;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ui.def.ISapphireCompositeDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentation;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationRef;
import org.eclipse.sapphire.ui.def.PartDef;
import org.eclipse.sapphire.ui.util.SapphireHelpSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class SapphireComposite extends SapphirePartContainer
{
    @Override
    public ISapphireCompositeDef definition()
    {
        return (ISapphireCompositeDef) super.definition();
    }
    
    @Override
    public Set<String> getActionContexts()
    {
        return Collections.singleton( SapphireActionSystem.CONTEXT_FORM );
    }

    @Override
    public final void render( final SapphireRenderingContext context )
    {
        final SapphireRenderingContext ctxt;
        Composite parent = context.getComposite();
        
        if( getPreferFormStyle() == true )
        {
            final FormToolkit toolkit = new FormToolkit( context.getDisplay() );
            
            ctxt = new SapphireRenderingContext( this, context, parent )
            {
                public void adapt( final Control control )
                {
                    super.adapt( control );
                    
                    if( control instanceof Composite )
                    {
                        toolkit.adapt( (Composite) control );
                    }
                    else if( control instanceof Label )
                    {
                        toolkit.adapt( control, false, false );
                    }
                    else
                    {
                        toolkit.adapt( control, true, true );
                    }
                }
            };
        }
        else
        {
            ctxt = context;
        }
        
        parent = createOuterComposite( ctxt );

        final ISapphireCompositeDef def = (ISapphireCompositeDef) this.definition;
        final boolean indent = def.getIndent().getContent();
        final boolean scrollVertically = def.getScrollVertically().getContent();
        final boolean scrollHorizontally = def.getScrollHorizontally().getContent();
        
        if( indent )
        {
            final Label label = new Label( parent, SWT.NONE );
            label.setLayoutData( gd() );
            context.adapt( label );
        }

        final ScrolledComposite scrolledComposite;

        if( scrollVertically || scrollHorizontally )
        {
            final int style
                = ( scrollVertically ? SWT.V_SCROLL : SWT.NONE ) | 
                  ( scrollHorizontally ? SWT.H_SCROLL : SWT.NONE );
            
            scrolledComposite = new ScrolledComposite( parent, style );
            scrolledComposite.setExpandHorizontal( true );
            scrolledComposite.setExpandVertical( true );
            
            parent = scrolledComposite;
        }
        else
        {
            scrolledComposite = null;
        }
        
        final boolean scaleVertically = def.getScaleVertically().getContent(); 
        
        final int width = getWidth( -1 );
        final int height = getHeight( -1 );
        
        final GridData gd = gdwhint( gdhhint( gdhspan( ( scaleVertically ? gdfill() : gdhfill() ), ( indent ? 1 : 2 ) ), height ), width );
        
        final int marginLeft = def.getMarginLeft().getContent();
        final int marginRight = def.getMarginRight().getContent();
        final int marginTop = def.getMarginTop().getContent();
        final int marginBottom = def.getMarginBottom().getContent();
        
        final Composite composite = new Composite( parent, SWT.NONE ) {
            public Point computeSize (int wHint, int hHint, boolean changed) {
                if (this.getChildren().length == 0) {
                    return new Point(0, 0);
                }
                return super.computeSize(wHint, hHint, changed);
            }
        };
        composite.setLayout( glayout( 2, marginLeft, marginRight, marginTop, marginBottom ) );
        ctxt.adapt( composite );
        
        if( scrolledComposite != null )
        {
            scrolledComposite.setContent( composite );
            scrolledComposite.setLayoutData( gd );
        }
        else
        {
            composite.setLayoutData( gd );
        }
        
        final ISapphireDocumentation doc = this.definition.getDocumentation().element();
        
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
                SapphireHelpSystem.setHelp( composite, docdef );
            }
        }

        final SapphireRenderingContext innerContext = new SapphireRenderingContext( this, ctxt, composite );
        
        super.render( innerContext );
        
        if( scrolledComposite != null )
        {
            scrolledComposite.setMinSize( composite.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
        }
        
        final Listener partListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                if( event instanceof StructureChangedEvent )
                {
                    // Something changed in the tree of parts arranged beneath this composite part. If this is
                    // the composite closest to the affected part, it will need to re-render.
                    
                    ISapphirePart part = ( (StructureChangedEvent) event ).part();
                    Boolean needToReRender = null;
                    
                    while( part != null && needToReRender == null )
                    {
                        part = part.getParentPart();
                        
                        if( part instanceof SapphireComposite )
                        {
                            if( part == SapphireComposite.this )
                            {
                                needToReRender = Boolean.TRUE;
                            }
                            else
                            {
                                needToReRender = Boolean.FALSE;
                            }
                        }
                    }
                    
                    if( needToReRender == Boolean.TRUE )
                    {
                        for( Control control : composite.getChildren() )
                        {
                            control.dispose();
                        }
                        
                        SapphireComposite.super.render( innerContext );
                        
                        if( scrolledComposite != null )
                        {
                            scrolledComposite.setMinSize( composite.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
                        }
                        
                        context.layout();
                    }
                }
            }
        };
        
        attach( partListener );
        
        composite.addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    detach( partListener );
                }
            }
        );
    }

    protected Composite createOuterComposite( final SapphireRenderingContext context )
    {
        return context.getComposite();
    }
    
    public int getWidth( final int defaultValue )
    {
        final Integer width = definition().getWidth().getContent();
        return ( width == null || width < 1 ? defaultValue : width );
    }
    
    public int getHeight( final int defaultValue )
    {
        final Integer height = definition().getHeight().getContent();
        return ( height == null || height < 1 ? defaultValue : height );
    }
    
    public boolean getPreferFormStyle()
    {
        return this.definition.getHint( PartDef.HINT_PREFER_FORM_STYLE, false );
    }
    
}
