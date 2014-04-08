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

import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class WizardPagePart extends CompositePart
{
    private FunctionResult imageFunctionResult;
    private FunctionResult labelFunctionResult;
    private FunctionResult descriptionFunctionResult;
    
    @Override
    protected void init()
    {
        super.init();
        
        final WizardPageDef def = definition();
        
        this.imageFunctionResult = initExpression
        (
            def.getImage().content(),
            ImageData.class,
            null,
            new Runnable()
            {
                public void run()
                {
                    broadcast( new ImageChangedEvent( WizardPagePart.this ) );
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
                    broadcast( new LabelChangedEvent( WizardPagePart.this ) );
                }
            }
        );
        this.descriptionFunctionResult = initExpression
        (
            def.getDescription().content(),
            String.class,
            null,
            new Runnable()
            {
                public void run()
                {
                    broadcast( new DescriptionChangedEvent( WizardPagePart.this ) );
                }
            }
        );
    }

    @Override
    public WizardPageDef definition()
    {
        return (WizardPageDef) super.definition();
    }
    
    public String getLabel()
    {
    	final LocalizationService localizationService = definition().adapt( LocalizationService.class );
    	return localizationService.text( (String)this.labelFunctionResult.value(), CapitalizationType.TITLE_STYLE, false );
    }
    
    public String getDescription()
    {
    	final LocalizationService localizationService = definition().adapt( LocalizationService.class );
    	return localizationService.text( (String)this.descriptionFunctionResult.value(), CapitalizationType.NO_CAPS, false );
    }
    
    public ImageData getImage()
    {
        return (ImageData) this.imageFunctionResult.value();
    }
    
    @Override
    public FormComponentPresentation createPresentation( final SwtPresentation parent, final Composite composite )
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
        if( this.labelFunctionResult != null )
        {
            this.labelFunctionResult.dispose();
        }
        if( this.descriptionFunctionResult != null )
        {
            this.descriptionFunctionResult.dispose();
        }
    }
    
}
