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

import java.util.List;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.def.IPropertiesViewContributionDef;
import org.eclipse.sapphire.ui.def.IPropertiesViewContributionPageDef;
import org.eclipse.sapphire.util.ListFactory;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PropertiesViewContributionPart extends SapphirePart
{
    private List<PropertiesViewContributionPagePart> pages;
    private PropertiesViewContributionPagePart selectedPage;
    
    @Override
    protected void init()
    {
        super.init();

        final IModelElement element = getModelElement();
        final ListFactory<PropertiesViewContributionPagePart> pagesListFactory = ListFactory.start();
        
        final Listener pagePartListener = new FilteredListener<PartValidationEvent>()
        {
            @Override
            protected void handleTypedEvent( PartValidationEvent event )
            {
                refreshValidation();
            }
        };
        
        for( IPropertiesViewContributionPageDef pageDef : ( (IPropertiesViewContributionDef) this.definition ).getPages() )
        {
            final PropertiesViewContributionPagePart pagePart = new PropertiesViewContributionPagePart();
            pagePart.init( this, element, pageDef, this.params );
            pagePart.attach( pagePartListener );
            pagesListFactory.add( pagePart );
        }
        
        this.pages = pagesListFactory.result();
        
        this.selectedPage = this.pages.get( 0 );
    }
    
    public List<PropertiesViewContributionPagePart> getPages()
    {
        return this.pages;
    }
    
    public PropertiesViewContributionPagePart getSelectedPage()
    {
        return this.selectedPage;
    }
    
    public void setSelectedPage( final PropertiesViewContributionPagePart selectedPage )
    {
        this.selectedPage = selectedPage;
    }

    @Override
    public void render( final SapphireRenderingContext context )
    {
        throw new IllegalStateException();
    }
    
    @Override
    protected Status computeValidation()
    {
        final Status.CompositeStatusFactory factory = Status.factoryForComposite();

        for( PropertiesViewContributionPagePart pagePart : this.pages )
        {
            factory.merge( pagePart.validation() );
        }
        
        return factory.create();
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        for( PropertiesViewContributionPagePart pagePart : this.pages )
        {
            pagePart.dispose();
        }
    }
    
}
