/******************************************************************************
 * Copyright (c) 2013 Oracle and Other Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Greg Amerson - [343972] Support image in editor page header
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import java.util.Collections;
import java.util.Set;

import org.eclipse.help.IContext;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.ui.def.IEditorPageDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentation;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationRef;
import org.eclipse.sapphire.ui.util.SapphireHelpSystem;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class SapphireEditorPagePart

    extends SapphirePart
    implements IPropertiesViewContributorPart
    
{
    private PropertiesViewContributionPart propertiesViewContributionPart;
    private FunctionResult imageFunctionResult;
    
    @Override
    protected void init() 
    {
        super.init();
        
        final IModelElement element = getModelElement();
        final IEditorPageDef def = definition();
        
        this.imageFunctionResult = initExpression
        (
            element,
            def.getPageHeaderImage().getContent(),
            ImageData.class,
            null,
            new Runnable()
            {
                public void run()
                {
                    broadcast( new ImageChangedEvent( SapphireEditorPagePart.this ) );
                }
            }
        );
    }
    
    @Override
    public IEditorPageDef definition()
    {
        return (IEditorPageDef) super.definition();
    }

    @Override
    public Set<String> getActionContexts()
    {
        return Collections.singleton( SapphireActionSystem.CONTEXT_EDITOR_PAGE );
    }
    
    @Override
    public IContext getDocumentationContext()
    {
        final ISapphireDocumentation doc = this.definition.getDocumentation().element();
        
        if( doc != null )
        {
            ISapphireDocumentationDef docdef = null;
            
            if( doc instanceof ISapphireDocumentationDef )
            {
                docdef = (ISapphireDocumentationDef) doc;
            }
            else
            {
                docdef = ( (ISapphireDocumentationRef) doc ).resolve();
            }
            
            if( docdef != null )
            {
                SapphireHelpSystem.getContext( docdef );
            }
        }
        
        return null;
    }

    public final PropertiesViewContributionPart getPropertiesViewContribution()
    {
        return this.propertiesViewContributionPart;
    }
    
    public final void setPropertiesViewContribution( final PropertiesViewContributionPart propertiesViewContributionPart )
    {
        if( this.propertiesViewContributionPart != propertiesViewContributionPart )
        {
            this.propertiesViewContributionPart = propertiesViewContributionPart;
            broadcast( new PropertiesViewContributionChangedEvent( this, propertiesViewContributionPart ) );
        }
    }
    
    public ImageData getPageHeaderImage()
    {
        return (ImageData) this.imageFunctionResult.value();
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
    
    public static final class PropertiesViewContributionChangedEvent extends PartEvent
    {
        private final PropertiesViewContributionPart contribution;
        
        public PropertiesViewContributionChangedEvent( final SapphirePart part,
                                                       final PropertiesViewContributionPart contribution )
        {
            super( part );
            
            this.contribution = contribution;
        }
        
        public PropertiesViewContributionPart contribution()
        {
            return this.contribution;
        }
    }

    public static final class SelectionChangedEvent extends PartEvent
    {
        public SelectionChangedEvent( final SapphirePart part )
        {
            super( part );
        }
    }

    @Override
    public void render( final SapphireRenderingContext context )
    {
        throw new UnsupportedOperationException();
    }
    
}
