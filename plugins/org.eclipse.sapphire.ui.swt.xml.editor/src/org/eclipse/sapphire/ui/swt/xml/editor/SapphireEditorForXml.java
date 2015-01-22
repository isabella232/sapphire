/******************************************************************************
 * Copyright (c) 2015 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [363258] Allow dynamic model types for SapphireEditorForXml
 *    Gregory Amerson - [444202] Lazy loading of editor pages
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.xml.editor;

import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.sapphire.Context;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.osgi.BundleBasedContext;
import org.eclipse.sapphire.ui.SapphireEditor;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.def.DefinitionLoader.Reference;
import org.eclipse.sapphire.ui.def.EditorPageDef;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public class SapphireEditorForXml extends SapphireEditor implements IExecutableExtension
{
    @Text( "Source" )
    private static LocalizableText sourcePageTitle;
    
    static 
    {
        LocalizableText.init( SapphireEditorForXml.class );
    }

    private ElementType type;
    private DefinitionLoader.Reference<EditorPageDef> definition;
    private StructuredTextEditor sourcePage;
    private Context context;
    private String sdef;
    private String pageName;
    private String pageDefinitionId;

    public SapphireEditorForXml( final ElementType type,
                                 final DefinitionLoader.Reference<EditorPageDef> definition )
    {
        super();

        if( type == null )
        {
            throw new IllegalArgumentException();
        }

        this.type = type;
        this.definition = definition;
    }

    public SapphireEditorForXml()
    {
    }
    
    @Override
    public void setInitializationData( final IConfigurationElement config,
                                       final String propertyName,
                                       final Object data )
    {
        super.setInitializationData( config, propertyName, data );
        
        if( this.definition == null )
        {
            final Map<?,?> properties = (Map<?,?>) data;
            
            this.context = BundleBasedContext.adapt( config.getContributor().getName() );
            this.sdef = (String) properties.get( "sdef" );
            this.pageName = (String) properties.get( "pageName" );
            this.pageDefinitionId = (String) properties.get( "pageDefinitionId" );
        }
    }
    
    @Override
    protected DefinitionLoader getDefinitionLoader()
    {
        return DefinitionLoader.context( this.context ).sdef( this.sdef );
    }

    @Override
    protected Reference<EditorPageDef> getDefinition( String id )
    {
        if( this.definition != null )
        {
            return this.definition;
        }
        else
        {
            return super.getDefinition( id );
        }
    }

    public final StructuredTextEditor getXmlEditor()
    {
        return this.sourcePage;
    }
    
    protected Element createModel()
    {
        ElementType type = this.type;
        
        if( type == null )
        {
            final EditorPageDef def = getDefinition( this.pageDefinitionId ).resolve();
            
            if( def == null )
            {
                throw new IllegalStateException();
            }
            
            final JavaType elementJavaType = def.getElementType().target();
            type = ElementType.read( (Class<?>) elementJavaType.artifact(), true );
        }
        
        final XmlEditorResourceStore store = createResourceStore( this.sourcePage );
        return type.instantiate( new RootXmlResource( store ) );
    }
    
    protected XmlEditorResourceStore createResourceStore( final StructuredTextEditor sourceEditor )
    {
        return new XmlEditorResourceStore( this, this.sourcePage );
    }
    
    @Override
    protected void createEditorPages() throws PartInitException
    {
        createSourcePages();
        
        // For backwards compatibility, if createModel() is overridden, we need to ensure that the method is
        // called before createFormPages() and createDiagramPages(). A concrete example of why a subclass may
        // depend on this order is to store the model in a field as part of createModel() and then later use
        // it when creating pages. Since this approach does not invoke getModelElement(), the lazy creation of
        // the model is not triggered.
        
        if( isCreateModelOverridden() )
        {
            getModelElement();
        }
        
        createFormPages();
        createDiagramPages();
    }
    
    private boolean isCreateModelOverridden()
    {
        try
        {
            if( getClass().getDeclaredMethod( "createModel" ).getDeclaringClass() != SapphireEditorForXml.class )
            {
                return true;
            }
        }
        catch( final Exception e ) {}
        
        return false;
    }

    @Override
    protected final void createSourcePages() throws PartInitException
    {
        this.sourcePage = new StructuredTextEditor();
        this.sourcePage.setEditorPart( this );
        
        final int index = addPage( this.sourcePage, getEditorInput() );
        setPageText( index, sourcePageTitle.text() );
    }

    @Override
    protected void createFormPages() throws PartInitException
    {
        if( this.pageName == null )
        {
            final IEditorPart page = createPage( getDefinition( null ) );
            
            if( page instanceof IFormPage )
            {
                addPage( 0, (IFormPage) page );
            }
            else
            {
                addPage( 0, page, getEditorInput() );
            }
        }
        else
        {
            addDeferredPage( 0, this.pageName, this.pageDefinitionId );
        }
    }

    @Override
    public IContentOutlinePage getContentOutline( final Object page )
    {
        if( page == this.sourcePage )
        {
            return (IContentOutlinePage) this.sourcePage.getAdapter( IContentOutlinePage.class );
        }
        
        return super.getContentOutline( page );
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        this.type = null;
        this.definition = null;
        this.sourcePage = null;
    }

}
