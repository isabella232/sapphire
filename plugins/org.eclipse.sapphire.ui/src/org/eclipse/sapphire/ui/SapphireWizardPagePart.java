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

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.ui.def.ISapphireWizardPageDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireWizardPagePart extends SapphireComposite
{
    private FunctionResult imageFunctionResult;
    private boolean visible;
    
    @Override
    protected void init()
    {
        super.init();
        
        final IModelElement element = getModelElement();
        final ISapphireWizardPageDef def = definition();
        
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
                    broadcast( new ImageChangedEvent( SapphireWizardPagePart.this ) );
                }
            }
        );
    }

    @Override
    public ISapphireWizardPageDef definition()
    {
        return (ISapphireWizardPageDef) super.definition();
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
    
    public boolean isVisible()
    {
        return this.visible;
    }
    
    public void setVisible( final boolean visible )
    {
        if( this.visible != visible )
        {
            this.visible = visible;
            broadcast( new VisibilityChangedEvent( this ) );
        }
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
