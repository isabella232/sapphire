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

import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.def.ISapphirePartDef;
import org.eclipse.sapphire.ui.def.ISapphireTabDef;
import org.eclipse.sapphire.ui.def.ISapphireTabGroupDef;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TabGroupPart

    extends SapphirePart
    
{
    private List<TabGroupPagePart> pages;
    
    @Override
    protected void init()
    {
        super.init();
        
        final IModelElement element = getModelElement();

        this.pages = new ArrayList<TabGroupPagePart>();
        
        for( ISapphireTabDef pageDef : ( (ISapphireTabGroupDef) this.definition ).getTabs() )
        {
            final TabGroupPagePart pagePart = new TabGroupPagePart();
            pagePart.init( this, element, pageDef, this.params );
            
            this.pages.add( pagePart );

            final SapphirePartListener tabPartListener = new SapphirePartListener()
            {
                @Override
                public void handleValidateStateChange( final Status oldValidateState,
                                                       final Status newValidationState )
                {
                    updateValidationState();
                }
            };
            
            pagePart.addListener( tabPartListener );
        }
    }

    @Override
    public void render( final SapphireRenderingContext context )
    {
        final boolean expandVertically 
            = ( Boolean.valueOf( this.definition.getHint( ISapphirePartDef.HINT_EXPAND_VERTICALLY ) ) == true );
    
        final TabFolder tabGroup = new TabFolder( context.getComposite(), SWT.TOP );
        tabGroup.setLayoutData( gdhspan( ( expandVertically ? gdfill() : gdhfill() ), 2 ) );
        context.adapt( tabGroup );
        
        for( final TabGroupPagePart page : this.pages )
        {
            final Composite tabControl = new Composite( tabGroup, SWT.NONE );
            tabControl.setLayout( glayout( 2, 0, 0 ) );

            final TabItem tab = new TabItem( tabGroup, SWT.NONE );
            tab.setText( page.getLabel() );
            tab.setControl( tabControl );
            
            final Map<ImageDescriptor,Image> images = new HashMap<ImageDescriptor,Image>();
            updateTabImage( tab, page, images );
            
            final SapphirePartListener tabPartListener = new SapphirePartListener()
            {
                @Override
                public void handleEvent( final SapphirePartEvent event )
                {
                    if( event instanceof SapphirePart.LabelChangedEvent )
                    {
                        tab.setText( page.getLabel() );
                    }
                    else if( event instanceof SapphirePart.ImageChangedEvent )
                    {
                        updateTabImage( tab, page, images );
                    }
                }
            };
            
            page.addListener( tabPartListener );
            
            tab.addDisposeListener
            (
                new DisposeListener()
                {
                    public void widgetDisposed( final DisposeEvent event )
                    {
                        page.removeListener( tabPartListener );
                        
                        for( Image image : images.values() )
                        {
                            image.dispose();
                        }
                    }
                }
            );
            
            page.render( new SapphireRenderingContext( page, context, tabControl ) );
        }
    }
    
    private void updateTabImage( final TabItem tab,
                                 final TabGroupPagePart tabPart,
                                 final Map<ImageDescriptor,Image> images )
    {
        final ImageDescriptor imageDescriptor = tabPart.getImage();
        Image image = images.get( imageDescriptor );
        
        if( image == null )
        {
            image = imageDescriptor.createImage();
            images.put( imageDescriptor, image );
        }
        
        tab.setImage( image );
    }
    
    @Override
    protected Status computeValidationState()
    {
        final Status.CompositeStatusFactory factory = Status.factoryForComposite();

        for( TabGroupPagePart page : this.pages )
        {
            factory.add( page.getValidationState() );
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
