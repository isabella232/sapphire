/******************************************************************************
 * Copyright (c) 2015 Oracle and Other Contributors
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

import static org.eclipse.sapphire.modeling.util.MiscUtil.createStringDigest;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.help.IContext;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.modeling.CorruptedResourceExceptionInterceptor;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.ui.def.EditorPageDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentation;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationRef;
import org.eclipse.sapphire.ui.forms.PropertiesViewContributionPart;
import org.eclipse.sapphire.ui.forms.PropertiesViewContributorPart;
import org.eclipse.sapphire.ui.forms.swt.HelpSystem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IURIEditorInput;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class SapphireEditorPagePart

    extends SapphirePart
    implements PropertiesViewContributorPart
    
{
    private EditorPageState state;
    private PropertiesViewContributionPart propertiesViewContributionPart;
    private FunctionResult pageHeaderTextFunctionResult;
    private FunctionResult pageHeaderImageFunctionResult;
    
    @Override
    protected void init() 
    {
        super.init();
        
        final EditorPageDef def = definition();
        
        this.pageHeaderTextFunctionResult = initExpression
        (
            def.getPageHeaderText().content(),
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
            def.getPageHeaderImage().content(),
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
    
    public synchronized EditorPageState state()
    {
        if( this.state == null )
        {
            final StringBuilder key = new StringBuilder();
            
            final IEditorInput editorInput = adapt( SapphireEditor.class ).getEditorInput();
            
            key.append( editorInput.getClass().getName() );
            key.append( '#' );
            
            if( editorInput instanceof IURIEditorInput )
            {
                final URI uri = ( (IURIEditorInput) editorInput ).getURI();
                
                if( uri != null )
                {
                    key.append( ( (IURIEditorInput) editorInput ).getURI().toString() );
                }
                else
                {
                    key.append( "%$**invalid**$%" );
                }
                
                key.append( '#' );
            }
            
            key.append( definition().getPageName().content() );
            
            final String digest = createStringDigest( key.toString() );
            
            File file = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
            file = new File( file, ".metadata/.plugins/org.eclipse.sapphire.ui/state" );
            file = new File( file, digest );
            
            final JavaType persistedStateElementJavaType = definition().getPersistentStateElementType().target();
            
            if( persistedStateElementJavaType == null )
            {
                throw new IllegalStateException();
            }
            
            final ElementType persistedStateElementType = ElementType.read( (Class<?>) persistedStateElementJavaType.artifact() );
            
            try
            {
                final Resource resource = new RootXmlResource( new XmlResourceStore( file ) ) ;
                
                resource.setCorruptedResourceExceptionInterceptor
                (
                    new CorruptedResourceExceptionInterceptor()
                    {
                        @Override
                        public boolean shouldAttemptRepair()
                        {
                            return true;
                        }
                    }
                );
                
                this.state = persistedStateElementType.instantiate( resource );
            }
            catch( ResourceStoreException e )
            {
                this.state = persistedStateElementType.instantiate();
            }
        }
        
        return this.state;
    }

    @Override
    public Set<String> getActionContexts()
    {
        return Collections.singleton( SapphireActionSystem.CONTEXT_EDITOR_PAGE );
    }
    
    @Override
    public IContext getDocumentationContext()
    {
        final ISapphireDocumentation doc = this.definition.getDocumentation().content();
        
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
                HelpSystem.getContext( docdef );
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
        
        if( this.state != null )
        {
            try
            {
                this.state.resource().save();
            }
            catch( Exception e )
            {
                // Intentionally ignoring, since failing to persist the editor page state is just
                // not that critical.
            }
            
            this.state.dispose();
        }
        
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
    
}
