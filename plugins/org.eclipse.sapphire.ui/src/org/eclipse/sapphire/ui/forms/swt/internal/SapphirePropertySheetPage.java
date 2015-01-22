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
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhalign;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdvfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.ui.PartVisibilityEvent;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.forms.ContainerPart.VisibleChildrenEvent;
import org.eclipse.sapphire.ui.forms.PropertiesViewPagePart;
import org.eclipse.sapphire.ui.forms.PropertiesViewContributionPart;
import org.eclipse.sapphire.util.MutableReference;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SapphirePropertySheetPage implements IPropertySheetPage
{
    @Text( "No Properties" )
    private static LocalizableText noProperties;
    
    static
    {
        LocalizableText.init( SapphirePropertySheetPage.class );
    }

    private PropertiesViewContributionPart part;
    private Composite composite;
    private Font noPropertiesFont;
    private Color noPropertiesFontColor;
    
    public PropertiesViewContributionPart getPart()
    {
        return this.part;
    }
    
    public void setPart( final PropertiesViewContributionPart part )
    {
        if( this.part != part )
        {
            this.part = part;
            refresh();
        }
    }
    
    public void createControl( final Composite parent )
    {
        this.composite = new Composite( parent, SWT.NONE );
        this.composite.setLayout( glayout( 1, 0, 0 ) );
        
        refresh();
    }
    
    private void refresh()
    {
        if( this.composite == null )
        {
            return;
        }
        
        for( Control child : this.composite.getChildren() )
        {
            child.dispose();
        }
        
        if( this.part == null )
        {
            final Composite labelComposite = new Composite( this.composite, SWT.NONE );
            labelComposite.setLayoutData( gdfill() );
            labelComposite.setLayout( glayout( 3 ) );
            
            final Label spacerLeft = new Label( labelComposite, SWT.NONE );
            spacerLeft.setLayoutData( gdvfill() );
            spacerLeft.setText( "" );
            
            final Label label = new Label( labelComposite, SWT.CENTER );
            label.setLayoutData( gdhalign( gdhfill(), SWT.CENTER ) );
            label.setText( noProperties.text() );
            
            final Label spacerRight = new Label( labelComposite, SWT.NONE );
            spacerRight.setLayoutData( gdvfill() );
            spacerRight.setText( "" );
            
            if( this.noPropertiesFont == null )
            {
                final Display display = label.getDisplay();
                
                final FontData fd = new FontData( label.getFont().getFontData()[ 0 ].getName(), 30, SWT.BOLD );
                this.noPropertiesFont = new Font( display, fd );
                this.noPropertiesFontColor = new Color( display, 230, 230, 230 );
            }
            
            label.setFont( this.noPropertiesFont );
            label.setForeground( this.noPropertiesFontColor );
        }
        else
        {
            final TabbedPropertyComposite tabbedPropertiesComposite = new TabbedPropertyComposite( this.composite );
            
            tabbedPropertiesComposite.setLayout( new FormLayout() );
            FormData formData = new FormData();
            formData.left = new FormAttachment( 0, 0 );
            formData.right = new FormAttachment( 100, 0 );
            formData.top = new FormAttachment( 0, 0 );
            formData.bottom = new FormAttachment( 100, 0 );
            
            tabbedPropertiesComposite.setLayoutData( gdfill() );
            
            final LocalizationService localizationService = this.part.definition().adapt( LocalizationService.class );
            final List<PropertiesViewPagePart> pages = this.part.getPages();
            final List<PropertiesViewPagePart> visiblePages = new ArrayList<PropertiesViewPagePart>();
            final List<TabbedPropertyList.Item> elements = new ArrayList<TabbedPropertyList.Item>( pages.size() );
            final TabbedPropertyList list = tabbedPropertiesComposite.getList();
            
            final Map<PropertiesViewPagePart,TabbedPropertyList.Item> partToTabbedPropertyListItem 
                = new HashMap<PropertiesViewPagePart,TabbedPropertyList.Item>();
            
            for( final PropertiesViewPagePart page : pages )
            {
                final int index;
                
                if( page.visible() )
                {
                    visiblePages.add( page );
                    
                    final MutableReference<ImageDescriptor> imageDescriptor = new MutableReference<ImageDescriptor>();
                    final MutableReference<Image> image = new MutableReference<Image>();
                    
                    final TabbedPropertyList.Item item = new TabbedPropertyList.Item()
                    {
                        public String getText()
                        {
                            return localizationService.transform( page.getLabel(), CapitalizationType.TITLE_STYLE, false );
                        }
    
                        public Image getImage()
                        {
                            final ImageDescriptor oldImageDescriptor = imageDescriptor.get();
                            final ImageDescriptor newImageDescriptor = page.getImage();
                            
                            if( newImageDescriptor != oldImageDescriptor )
                            {
                                if( oldImageDescriptor != null )
                                {
                                    image.get().dispose();
                                }
                                
                                imageDescriptor.set( newImageDescriptor );
                                image.set( newImageDescriptor.createImage() );
                            }
                            
                            return image.get();
                        }
    
                        public boolean isIndented()
                        {
                            return false;
                        }
                    };
                    
                    elements.add( item );
                    partToTabbedPropertyListItem.put( page, item );
                    
                    index = elements.size() - 1;
                }
                else
                {
                    index = -1;
                }

                final org.eclipse.sapphire.Listener listener = new org.eclipse.sapphire.Listener()
                {
                    @Override
                    public void handle( final org.eclipse.sapphire.Event event )
                    {
                        if( event instanceof SapphirePart.LabelChangedEvent || event instanceof SapphirePart.ImageChangedEvent )
                        {
                            if( index != -1 )
                            {
                                list.update( index );
                            }
                        }
                        else if( event instanceof PartVisibilityEvent || event instanceof VisibleChildrenEvent )
                        {
                            refresh();
                        }
                    }
                };
                
                page.attach( listener );
                
                tabbedPropertiesComposite.addDisposeListener
                (
                    new DisposeListener()
                    {
                        public void widgetDisposed( final DisposeEvent event )
                        {
                            page.detach( listener );
                        }
                    }
                );
            }

            list.setElements( elements.toArray( new Object[ elements.size() ] ) );

            final Composite[] pageComposites = new Composite[ elements.size() ];
            
            list.addListener
            (
                SWT.Selection,
                new Listener()
                {
                    public void handleEvent( final Event event )
                    {
                        final int oldSelectionIndex = elements.indexOf( partToTabbedPropertyListItem.get( SapphirePropertySheetPage.this.part.getSelectedPage() ) );
                        final int newSelectionIndex = list.getSelectionIndex();
                        
                        Composite newPageComposite = pageComposites[ newSelectionIndex ];
                        
                        if( newSelectionIndex != oldSelectionIndex || newPageComposite == null )
                        {
                            final Composite oldPageComposite = ( oldSelectionIndex != -1 ? pageComposites[ oldSelectionIndex ] : null );
                            
                            if( oldPageComposite != null )
                            {
                                oldPageComposite.setVisible( false );
                            }
                                    
                            final PropertiesViewPagePart pagePart = visiblePages.get( newSelectionIndex );
                            SapphirePropertySheetPage.this.part.setSelectedPage( pagePart );
                            
                            if( newPageComposite == null )
                            {
                                newPageComposite = new Composite( tabbedPropertiesComposite.getTabComposite(), SWT.NO_FOCUS );
                                newPageComposite.setVisible( false );
                                
                                final FormData data = new FormData();
                                data.top = new FormAttachment( 0, 0 );
                                data.bottom = new FormAttachment( 100, 0 );
                                data.left = new FormAttachment( 0, 0 );
                                data.right = new FormAttachment( 100, 0 );
                                newPageComposite.setLayoutData( data );
                                
                                newPageComposite.setLayout( glayout( 2, 5, 5, 10, 10 ) );
                                
                                pagePart.createPresentation( null, newPageComposite ).render();

                                pageComposites[ newSelectionIndex ] = newPageComposite;
                            }
                            
                            newPageComposite.moveAbove( null );
                            newPageComposite.setVisible( true );
                            
                            tabbedPropertiesComposite.resizeScrolledComposite( newPageComposite );
                            newPageComposite.getParent().layout( true, true );
                        }
                    }
                }
            );
            
            TabbedPropertyList.Item initialSelectedItem = partToTabbedPropertyListItem.get( this.part.getSelectedPage() );
            
            if( initialSelectedItem == null )
            {
                initialSelectedItem = elements.get( 0 );
            }
            
            list.select( initialSelectedItem );
        }
        
        this.composite.layout( true, true );
    }

    public Control getControl()
    {
        return this.composite;
    }
    
    public void setFocus()
    {
    }

    public void setActionBars( final IActionBars actionBars )
    {
    }

    public void selectionChanged( final IWorkbenchPart part,
                                  final ISelection selection )
    {
    }
    
    public void dispose()
    {
        if( this.composite != null )
        {
            this.composite.dispose();
            this.composite = null;
        }
        
        if( this.noPropertiesFont != null )
        {
            this.noPropertiesFont.dispose();
            this.noPropertiesFont = null;
            
            this.noPropertiesFontColor.dispose();
            this.noPropertiesFontColor = null;
        }
    }

}

