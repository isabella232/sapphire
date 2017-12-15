/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.xml;

import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.ui.SapphireEditor;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class SapphireEditorForXml

    extends SapphireEditor
    
{
    public static final String PAGE_MAIN_SOURCE = "main.source"; //$NON-NLS-1$

    private MasterDetailsPage mainPage;
    private StructuredTextEditor sourceEditor;
    private ModelElementType rootModelElementType;
    private String editorDefinitionPath;
    
    public SapphireEditorForXml( final String pluginId )
    {
        super( pluginId );
    }
    
    public final String getEditorDefinitionPath()
    {
        return this.editorDefinitionPath;
    }
    
    public final void setEditorDefinitionPath( final String editorDefinitionPath )
    {
        this.editorDefinitionPath = editorDefinitionPath;
    }
    
    protected final ModelElementType getRootModelElementType()
    {
        return this.rootModelElementType;
    }
    
    protected final void setRootModelElementType( final ModelElementType rootModelElementType )
    {
        this.rootModelElementType = rootModelElementType;
    }
    
    protected final IModelElement createModel()
    {
        final XmlEditorResourceStore store = createResourceStore( this.sourceEditor );
        return this.rootModelElementType.instantiate( new RootXmlResource( store ) );
    }
    
    protected XmlEditorResourceStore createResourceStore( final StructuredTextEditor sourceEditor )
    {
        return new XmlEditorResourceStore( this, this.sourceEditor );
    }
    
    @Override
    protected final void createSourcePages()
    
        throws PartInitException
        
    {
        this.sourceEditor = new StructuredTextEditor();
        this.sourceEditor.setEditorPart(this);
        
        final int index = addPage( this.sourceEditor, getEditorInput() );
        setPageText( index, Resources.sourcePageTitle );
        setPageId( this.sourceEditor, PAGE_MAIN_SOURCE );
    }
    
    @Override
    protected void createFormPages()
    
        throws PartInitException
        
    {
        this.mainPage = new MasterDetailsPage( this, getModelElement(), new Path( getEditorDefinitionPath() ) );
        addPage( 0, this.mainPage );
    }

    @Override
    public IContentOutlinePage getContentOutline( final Object page )
    {
        if( page == this.sourceEditor )
        {
            return (IContentOutlinePage) this.sourceEditor.getAdapter( IContentOutlinePage.class );
        }
        
        return super.getContentOutline( page );
    }
    
    private static final class Resources extends NLS
    {
        public static String sourcePageTitle;
    
        static 
        {
            initializeMessages( SapphireEditorForXml.class.getName(), Resources.class );
        }
    }

}
