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
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.util.ListFactory;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class WizardPart extends SapphirePart
{
    private FunctionResult imageFunctionResult;
    private FunctionResult labelFunctionResult;
    private List<WizardPagePart> pages;
    
    @Override
    protected void init()
    {
        super.init();
        
        final Element element = getModelElement();
        final WizardDef def = definition();
        
        this.imageFunctionResult = initExpression
        (
            def.getImage().content(),
            ImageData.class,
            null,
            new Runnable()
            {
                public void run()
                {
                    broadcast( new ImageChangedEvent( WizardPart.this ) );
                }
            }
        );
        
        this.labelFunctionResult = initExpression
        (
            def.getLabel().content(),
            String.class,
            null,
            new Runnable()
            {
                public void run()
                {
                    broadcast( new LabelChangedEvent( WizardPart.this ) );
                }
            }
        );
        
        final ListFactory<WizardPagePart> pagesListFactory = ListFactory.start();
        
        for( final WizardPageDef pageDef : def.getPages() )
        {
            pagesListFactory.add( (WizardPagePart) create( this, element, pageDef, this.params ) );
        }
        
        this.pages = pagesListFactory.result();
    }

    @Override
    public WizardDef definition()
    {
        return (WizardDef) super.definition();
    }
        
    public String getLabel()
    {
        final LocalizationService localizationService = definition().adapt( LocalizationService.class );
        return localizationService.text( (String) this.labelFunctionResult.value(), CapitalizationType.TITLE_STYLE, false );
    }
    
    public ImageData getImage()
    {
        return (ImageData) this.imageFunctionResult.value();
    }
    
    public List<WizardPagePart> getPages()
    {
        return this.pages;
    }
    
    @Override
    public void dispose()
    {
        if( this.imageFunctionResult != null )
        {
            this.imageFunctionResult.dispose();
        }
        
        if( this.labelFunctionResult != null )
        {
            this.labelFunctionResult.dispose();
        }
        
        for( final WizardPagePart page : this.pages )
        {
            page.dispose();
        }
        
        super.dispose();
    }
    
}
