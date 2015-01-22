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
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphirePart.ImageChangedEvent;
import org.eclipse.sapphire.ui.SapphirePart.LabelChangedEvent;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.TabGroupPagePart;
import org.eclipse.sapphire.ui.forms.TabGroupPart;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.util.ListFactory;
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
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TabGroupPresentation extends FormComponentPresentation
{
    private List<Presentation> pages;
    
    public TabGroupPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
    }

    @Override
    public TabGroupPart part()
    {
        return (TabGroupPart) super.part();
    }
    
    @Override
    public void render()
    {
        final TabGroupPart part = part();
        
        final ListFactory<Presentation> pagesListFactory = ListFactory.start();
        
        final TabFolder tabFolderControl = new TabFolder( composite(), SWT.TOP );
        tabFolderControl.setLayoutData( gdhindent( gdhspan( ( part.getScaleVertically() ? gdfill() : gdhfill() ), 2 ), 9 ) );
        
        register( tabFolderControl );
        
        for( final TabGroupPagePart page : part.pages() )
        {
            final Composite tabControl = new Composite( tabFolderControl, SWT.NONE );
            tabControl.setLayout( glayout( 2, 1, 10, 10, 10 ) );

            final TabItem tab = new TabItem( tabFolderControl, SWT.NONE );
            tab.setText( page.getLabel() );
            tab.setControl( tabControl );
            
            final Map<ImageDescriptor,Image> images = new HashMap<ImageDescriptor,Image>();
            refreshTabImage( tab, page, images );
            
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
                        refreshTabImage( tab, page, images );
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
            
            final Presentation presentation = page.createPresentation( this, tabControl );
            pagesListFactory.add( presentation );
            
            presentation.render();
        }
        
        tabFolderControl.setSelection( part.pages().indexOf( part.selection() ) );
        
        tabFolderControl.addSelectionListener
        (
            new SelectionAdapter()
            {
                @Override
                public void widgetSelected( final SelectionEvent event )
                {
                    final int tabGroupPageIndex = tabFolderControl.getSelectionIndex();
                    part.select( part.pages().get( tabGroupPageIndex ) );
                }
            }
        );
        
        this.pages = pagesListFactory.result();
    }

    private static void refreshTabImage( final TabItem tab, final TabGroupPagePart tabPart, final Map<ImageDescriptor,Image> images )
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
    public void dispose()
    {
        super.dispose();
        
        if( this.pages != null )
        {
            for( final Presentation page : this.pages )
            {
                page.dispose();
            }
            
            this.pages = null;
        }
    }

}
