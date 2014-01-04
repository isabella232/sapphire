/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import java.util.List;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.PartValidationEvent;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.TabGroupPresentation;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TabGroupPart extends FormComponentPart
{
    private List<TabGroupPagePart> pages;
    private TabGroupPagePart selection;
    
    @Override
    protected void init()
    {
        super.init();
        
        final Element element = getModelElement();
        final ListFactory<TabGroupPagePart> pagesListFactory = ListFactory.start();

        final Listener tabPartListener = new FilteredListener<PartValidationEvent>()
        {
            @Override
            protected void handleTypedEvent( PartValidationEvent event )
            {
                refreshValidation();
            }
        };
        
        for( TabGroupPageDef pageDef : definition().getTabs() )
        {
            final TabGroupPagePart pagePart = new TabGroupPagePart();
            pagePart.init( this, element, pageDef, this.params );
            pagePart.initialize();
            pagePart.attach( tabPartListener );
            pagesListFactory.add( pagePart );
        }
        
        this.pages = pagesListFactory.result();
        this.selection = this.pages.get( 0 );
    }
    
    @Override
    public TabGroupDef definition()
    {
        return (TabGroupDef) super.definition();
    }
    
    public List<TabGroupPagePart> pages()
    {
        return this.pages;
    }
    
    public TabGroupPagePart selection()
    {
        return this.selection;
    }
    
    public void select( final TabGroupPagePart page )
    {
        if( page == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.selection = page;
        this.selection.setFocus();
    }

    @Override
    protected Status computeValidation()
    {
        final Status.CompositeStatusFactory factory = Status.factoryForComposite();

        for( TabGroupPagePart page : this.pages )
        {
            factory.merge( page.validation() );
        }
        
        return factory.create();
    }
    
    @Override
    public FormComponentPresentation createPresentation( final SwtPresentation parent, final Composite composite )
    {
        return new TabGroupPresentation( this, parent, composite );
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
