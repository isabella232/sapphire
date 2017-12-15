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
import org.eclipse.sapphire.ui.def.EditorPageDef;
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
    private FunctionResult pageHeaderTextFunctionResult;
    private FunctionResult pageHeaderImageFunctionResult;
    
    @Override
    protected void init() 
    {
        super.init();
        
        final IModelElement element = getModelElement();
        final EditorPageDef def = definition();
        
        this.pageHeaderTextFunctionResult = initExpression
        (
            element,
            def.getPageHeaderText().getContent(),
            String.class,
            null,
            new Runnable()
            {
                public void run()
                {
                    broadcast( new PageHeaderTextEvent( SapphireEditorPagePart.this ) );
                }
            }
        );

        this.pageHeaderImageFunctionResult = initExpression
        (
            element,
            def.getPageHeaderImage().getContent(),
            ImageData.class,
            null,
            new Runnable()
            {
                public void run()
                {
                    broadcast( new PageHeaderImageEvent( SapphireEditorPagePart.this ) );
                }
            }
        );
    }
    
    @Override
    public EditorPageDef definition()
    {
        return (EditorPageDef) super.definition();
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
    
    public String getPageHeaderText()
    {
        return (String) this.pageHeaderTextFunctionResult.value();
    }
    
    public ImageData getPageHeaderImage()
    {
        return (ImageData) this.pageHeaderImageFunctionResult.value();
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.pageHeaderTextFunctionResult != null )
        {
            this.pageHeaderTextFunctionResult.dispose();
        }
        
        if( this.pageHeaderImageFunctionResult != null )
        {
            this.pageHeaderImageFunctionResult.dispose();
        }
    }
    
    public static final class PageHeaderTextEvent extends PartEvent
    {
        public PageHeaderTextEvent( final SapphireEditorPagePart part )
        {
            super( part );
        }
    }
    
    public static final class PageHeaderImageEvent extends PartEvent
    {
        public PageHeaderImageEvent( final SapphireEditorPagePart part )
        {
            super( part );
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
