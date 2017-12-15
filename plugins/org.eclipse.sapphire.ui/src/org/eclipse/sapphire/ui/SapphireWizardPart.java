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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.ui.def.ISapphireWizardDef;
import org.eclipse.sapphire.ui.def.ISapphireWizardPageDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireWizardPart extends SapphirePart
{
    private FunctionResult imageFunctionResult;
    private List<SapphireWizardPagePart> pages;
    private List<SapphireWizardPagePart> pagesReadOnly;
    
    @Override
    protected void init()
    {
        super.init();
        
        final IModelElement element = getModelElement();
        final ISapphireWizardDef def = definition();
        
        this.imageFunctionResult = initExpression
        (
            element,
            def.getImage().getContent(),
            ImageData.class,
            null,
            new Runnable()
            {
                public void run()
                {
                    broadcast( new ImageChangedEvent( SapphireWizardPart.this ) );
                }
            }
        );
        
        this.pages = new ArrayList<SapphireWizardPagePart>();
        this.pagesReadOnly = Collections.unmodifiableList( this.pages );
        
        for( ISapphireWizardPageDef pageDef : def.getPages() )
        {
            final SapphireWizardPagePart pagePart = (SapphireWizardPagePart) SapphirePart.create( null, element, pageDef, this.params );
            this.pages.add( pagePart );
        }
    }

    @Override
    public ISapphireWizardDef definition()
    {
        return (ISapphireWizardDef) super.definition();
    }
    
    public String getLabel()
    {
        return definition().getLabel().getLocalizedText( CapitalizationType.TITLE_STYLE, false );
    }
    
    public String getDescription()
    {
        return definition().getDescription().getLocalizedText( CapitalizationType.NO_CAPS, false );
    }
    
    public ImageData getImage()
    {
        return (ImageData) this.imageFunctionResult.value();
    }
    
    public List<SapphireWizardPagePart> getPages()
    {
        return this.pagesReadOnly;
    }

    @Override
    public void render( final SapphireRenderingContext context )
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.imageFunctionResult != null )
        {
            this.imageFunctionResult.dispose();
        }
    }
    
}
