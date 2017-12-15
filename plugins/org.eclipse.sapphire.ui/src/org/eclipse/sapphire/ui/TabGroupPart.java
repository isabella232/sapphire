/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.def.TabGroupDef;
import org.eclipse.sapphire.ui.def.TabGroupPageDef;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TabGroupPart extends SapphirePart
{
    private List<TabGroupPagePart> pages;
    
    @Override
    protected void init()
    {
        super.init();
        
        final IModelElement element = getModelElement();

        this.pages = new ArrayList<TabGroupPagePart>();
        
        for( TabGroupPageDef pageDef : definition().getTabs() )
        {
            final TabGroupPagePart pagePart = new TabGroupPagePart();
            pagePart.init( this, element, pageDef, this.params );
            
            this.pages.add( pagePart );

            final Listener tabPartListener = new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof ValidationChangedEvent )
                    {
                        updateValidationState();
                    }
                }
            };
            
            pagePart.attach( tabPartListener );
        }
    }
    
    @Override
    public TabGroupDef definition()
    {
        return (TabGroupDef) super.definition();
    }

    @Override
    public void render( final SapphireRenderingContext context )
    {
        final boolean scaleVertically = definition().getScaleVertically().getContent();
    
        final TabFolder tabGroup = new TabFolder( context.getComposite(), SWT.TOP );
        tabGroup.setLayoutData( gdhindent( gdhspan( ( scaleVertically ? gdfill() : gdhfill() ), 2 ), 9 ) );
        context.adapt( tabGroup );
        
        for( final TabGroupPagePart page : this.pages )
        {
            final Composite tabControl = new Composite( tabGroup, SWT.NONE );
            tabControl.setLayout( glayout( 2, 1, 10, 10, 10 ) );

            final TabItem tab = new TabItem( tabGroup, SWT.NONE );
            tab.setText( page.getLabel() );
            tab.setControl( tabControl );
            
            final Map<ImageDescriptor,Image> images = new HashMap<ImageDescriptor,Image>();
            updateTabImage( tab, page, images );
            
            final Listener tabPartListener = new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof LabelChangedEvent )
                    {
                        tab.setText( page.getLabel() );
                    }
                    else if( event instanceof ImageChangedEvent )
                    {
                        updateTabImage( tab, page, images );
                    }
                }
            };
            
            page.attach( tabPartListener );
            
            tab.addDisposeListener
            (
                new DisposeListener()
                {
                    public void widgetDisposed( final DisposeEvent event )
                    {
                        page.detach( tabPartListener );
                        
                        for( Image image : images.values() )
                        {
                            image.dispose();
                        }
                    }
                }
            );
            
            page.render( new SapphireRenderingContext( page, context, tabControl ) );
        }
        
        tabGroup.addSelectionListener
        (
            new SelectionAdapter()
            {
                @Override
                public void widgetSelected( final SelectionEvent event )
                {
                    final int tabGroupPageIndex = tabGroup.getSelectionIndex();
                    final TabGroupPagePart tabGroupPagePart = TabGroupPart.this.pages.get( tabGroupPageIndex );;
                    tabGroupPagePart.setFocus();
                }
            }
        );
    }
    
    private void updateTabImage( final TabItem tab,
                                 final TabGroupPagePart tabPart,
                                 final Map<ImageDescriptor,Image> images )
    {
        Image image = null;

        final ImageDescriptor imageDescriptor = tabPart.getImage();
        
        if( imageDescriptor != null )
        {
            image = images.get( imageDescriptor );
        
            if( image == null )
            {
                image = imageDescriptor.createImage();
                images.put( imageDescriptor, image );
            }
        }
        
        tab.setImage( image );
    }
    
    @Override
    protected Status computeValidationState()
    {
        final Status.CompositeStatusFactory factory = Status.factoryForComposite();

        for( TabGroupPagePart page : this.pages )
        {
            factory.merge( page.getValidationState() );
        }
        
        return factory.create();
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        for( SapphirePart page : this.pages )
        {
            page.dispose();
        }
    }
    
}
