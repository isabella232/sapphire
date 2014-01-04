/******************************************************************************
 * Copyright (c) 2014 Oracle and Other Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [363258] Allow dynamic model types for SapphireEditorForXml
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
import org.eclipse.sapphire.ui.def.EditorPageDef;
import org.eclipse.sapphire.ui.forms.FormEditorPageDef;
import org.eclipse.sapphire.ui.forms.MasterDetailsEditorPageDef;
import org.eclipse.sapphire.ui.forms.swt.FormEditorPage;
import org.eclipse.sapphire.ui.forms.swt.MasterDetailsEditorPage;
import org.eclipse.sapphire.ui.forms.swt.SapphireEditorFormPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
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
    private SapphireEditorFormPage formPage;
    
    public SapphireEditorForXml( final ElementType type,
                                 final DefinitionLoader.Reference<EditorPageDef> definition )
    {
        if( type == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( definition == null )
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
            final String bundleId = config.getContributor().getName();
            final Context context = BundleBasedContext.adapt( bundleId );
            final Map<?,?> properties = (Map<?,?>) data;
    
            final String sdef = (String) properties.get( "sdef" );
            this.definition = DefinitionLoader.context( context ).sdef( sdef ).page();
            
            final JavaType elementJavaType = this.definition.resolve().getElementType().resolve();
            this.type = ElementType.read( (Class<?>) elementJavaType.artifact(), true );
        }
    }
    
    public final StructuredTextEditor getXmlEditor()
    {
        return this.sourcePage;
    }
    
    protected Element createModel()
    {
        final XmlEditorResourceStore store = createResourceStore( this.sourcePage );
        return this.type.instantiate( new RootXmlResource( store ) );
    }
    
    protected XmlEditorResourceStore createResourceStore( final StructuredTextEditor sourceEditor )
    {
        return new XmlEditorResourceStore( this, this.sourcePage );
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
        final EditorPageDef def = this.definition.resolve();
        
        if( def instanceof MasterDetailsEditorPageDef )
        {
            this.formPage = new MasterDetailsEditorPage( this, getModelElement(), this.definition );
        }
        else if( def instanceof FormEditorPageDef )
        {
            this.formPage = new FormEditorPage( this, getModelElement(), this.definition );
        }
        
        addPage( 0, this.formPage );
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
        this.formPage = null;
    }

}
