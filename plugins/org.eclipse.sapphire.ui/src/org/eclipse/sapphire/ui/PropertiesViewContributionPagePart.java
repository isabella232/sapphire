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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.ui.def.IPropertiesViewContributionPageDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PropertiesViewContributionPagePart extends SapphirePartContainer
{
    private FunctionResult labelFunctionResult;
    private ImageManager imageManager;
    private FunctionResult visibleWhenFunctionResult;

    @Override
    protected void init()
    {
        super.init();

        final IModelElement element = getModelElement();
        final IPropertiesViewContributionPageDef def = definition();
        
        this.labelFunctionResult = initExpression
        (
            element,
            def.getLabel().getContent(), 
            String.class,
            null,
            new Runnable()
            {
                public void run()
                {
                    broadcast( new LabelChangedEvent( PropertiesViewContributionPagePart.this ) );
                }
            }
        );
        
        this.imageManager = new ImageManager( element, def.getImage().getContent() );

        this.visibleWhenFunctionResult = initExpression
        (
            element,
            def.getVisibleWhen().getContent(), 
            Boolean.class,
            Literal.create( Boolean.TRUE ),
            new Runnable()
            {
                public void run()
                {
                    broadcast( new VisibilityChangedEvent( PropertiesViewContributionPagePart.this ) );
                }
            }
        );
    }

    @Override
    public IPropertiesViewContributionPageDef definition()
    {
        return (IPropertiesViewContributionPageDef) super.definition();
    }
    
    public String getLabel()
    {
        return (String) this.labelFunctionResult.value();
    }
    
    public ImageDescriptor getImage()
    {
        return this.imageManager.getImage();
    }

    public boolean visible()
    {
        return (Boolean) this.visibleWhenFunctionResult.value();
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
        
        if( this.visibleWhenFunctionResult != null )
        {
            this.visibleWhenFunctionResult.dispose();
        }
    }
    
}
