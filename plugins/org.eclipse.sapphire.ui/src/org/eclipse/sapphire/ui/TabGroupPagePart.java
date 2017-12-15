/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.ui.def.ISapphireTabDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TabGroupPagePart

    extends SapphirePartContainer
    
{
    private static final ImageData IMG_TAB_GROUP_PAGE
        = ImageData.createFromClassLoader( TabGroupPagePart.class, "TabGroupPage.png" );
    
    private FunctionResult labelFunctionResult;
    private ImageManager imageManager;

    @Override
    protected void init()
    {
        super.init();

        final IModelElement element = getModelElement();
        final ISapphireTabDef def = getDefinition();
        
        this.labelFunctionResult = initExpression
        (
            element,
            def.getLabel().getContent(), 
            String.class,
            Literal.create( "tab" ),
            new Runnable()
            {
                public void run()
                {
                    notifyListeners( new LabelChangedEvent( TabGroupPagePart.this ) );
                }
            }
        );
        
        this.imageManager = new ImageManager( element, def.getImage().getContent(), Literal.create( IMG_TAB_GROUP_PAGE ) );
    }

    @Override
    public ISapphireTabDef getDefinition()
    {
        return (ISapphireTabDef) super.getDefinition();
    }
    
    public String getLabel()
    {
        String label = null;
        
        if( this.labelFunctionResult != null )
        {
            label = (String) this.labelFunctionResult.value();
        }
        
        if( label == null )
        {
            label = "#null#";
        }
        else
        {
            label = label.trim();
            label = this.definition.adapt( LocalizationService.class ).transform( label, CapitalizationType.TITLE_STYLE, false );
        }
        
        return label;
    }
    
    public ImageDescriptor getImage()
    {
        return this.imageManager.getImage();
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.labelFunctionResult != null )
        {
            this.labelFunctionResult.dispose();
        }
        
        if( this.imageManager != null )
        {
            this.imageManager.dispose();
        }
    }
    
}
