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
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class WizardPagePart extends CompositePart
{
    private FunctionResult imageFunctionResult;
    
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
    }

    @Override
    public WizardPageDef definition()
    {
        return (WizardPageDef) super.definition();
    }
    
    public String getLabel()
    {
        return definition().getLabel().localized( CapitalizationType.TITLE_STYLE, false );
    }
    
    public String getDescription()
    {
        return definition().getDescription().localized( CapitalizationType.NO_CAPS, false );
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
    }
    
}
