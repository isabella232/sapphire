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

package org.eclipse.sapphire.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.sapphire.modeling.IModel;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelStore;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.xml.IModelElementForXml;
import org.eclipse.sapphire.modeling.xml.XmlElement;
import org.eclipse.sapphire.modeling.xml.XmlNode;
import org.eclipse.sapphire.ui.actions.Action;
import org.eclipse.sapphire.ui.xml.ModelStoreForXmlEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class SapphireEditorFormPage

    extends FormPage
    implements ISapphirePart
    
{
    private final SapphireEditor editor;
    private final IModelElement rootModelElement;
    
    public SapphireEditorFormPage( final SapphireEditor editor,
                                   final IModelElement rootModelElement ) 
    {
        super( editor, null, null );

        this.editor = editor;
        this.rootModelElement = rootModelElement;
    }
    
	public SapphireEditor getEditor()
    {
        return this.editor;
    }
    
    public IModelElement getRootModelElement()
    {
        return this.rootModelElement;
    }
    
    public abstract String getId();
    
    public IModel getModel()
    {
        return this.editor.getModel();
    }
    
    public final Preferences getGlobalPreferences( final boolean createIfNecessary )
    
        throws BackingStoreException
        
    {
        Preferences prefs = this.editor.getGlobalPreferences( createIfNecessary );
        final String pageName = getPartName();
        
        if( prefs != null && ( prefs.nodeExists( pageName ) || createIfNecessary ) )
        {
            return prefs.node( pageName );
        }
        
        return null;
    }
    
    public final Preferences getInstancePreferences( final boolean createIfNecessary )
    
        throws BackingStoreException
        
    {
        Preferences prefs = this.editor.getInstancePreferences( createIfNecessary );
        final String pageName = getPartName();
        
        if( prefs != null && ( prefs.nodeExists( pageName ) || createIfNecessary ) )
        {
            return prefs.node( pageName );
        }
        
        return null;
    }
    
    public ITextEditor getSourceView()
    {
        final ModelStore modelStore = this.rootModelElement.getModel().getModelStore();
        
        if( modelStore instanceof ModelStoreForXmlEditor )
        {
            return ( (ModelStoreForXmlEditor) modelStore ).getXmlEditor();
        }
        
        return null;
    }
    
    @SuppressWarnings( "restriction" )
    
    public void showInSourceView( final IModelElement element,
                                  final ModelProperty property )
    {
        final ITextEditor sourceView = getSourceView();
        
        if( sourceView != null )
        {
            final Range range = new Range();
            
            if( property != null )
            {
                final List<XmlNode> xmlNodes = getXmlNodes( (IModelElementForXml) element, property );
                
                if( ! xmlNodes.isEmpty() )
                {
                    if( property instanceof ValueProperty )
                    {
                        final IDOMNode domNode = (IDOMNode) xmlNodes.get( 0 ).getDomNode();
                        
                        if( domNode instanceof IDOMElement )
                        {
                            final IDOMElement domElement = (IDOMElement) domNode;
                            
                            if( domElement.hasEndTag() )
                            {
                                range.merge( domElement.getStartEndOffset(), domElement.getEndStartOffset() );
                            }
                        }
                        else if( domNode instanceof IDOMAttr )
                        {
                            final IDOMAttr domAttr = (IDOMAttr) domNode;
                            final int start = domAttr.getValueRegionStartOffset();
                            range.merge( start + 1, start + domAttr.getValueRegionText().length() - 1 );
                        }
                        else
                        {
                            range.merge( domNode.getStartOffset(), domNode.getEndOffset() );
                        }
                    }
                    else
                    {
                        for( XmlNode xmlNode : xmlNodes )
                        {
                            final IDOMNode domNode = (IDOMNode) xmlNode.getDomNode();
                            range.merge( domNode.getStartOffset(), domNode.getEndOffset() );
                        }
                    }
                }
            }
            
            if( ! range.isInitialized() )
            {
                IModelElementForXml modElement = (IModelElementForXml) element;
                XmlElement xmlElement = modElement.getXmlElement();
                
                while( xmlElement == null && modElement != null )
                {
                    modElement = (IModelElementForXml) modElement.getParent();
                    
                    if( modElement != null )
                    {
                        xmlElement = modElement.getXmlElement();
                    }
                }
                    
                if( xmlElement != null )
                {
                    final IDOMNode domNode = (IDOMNode) xmlElement.getDomNode();
                    range.merge( domNode.getStartOffset(), domNode.getEndOffset() );
                }
            }
            
            sourceView.getSelectionProvider().setSelection( range.toTextSelection() );
            getEditor().showPage( sourceView );
        }
    }
    
    public boolean isOptimalConversionPossible( final IModelElement element,
                                                final ModelProperty property )
    {
        if( element != null )
        {
            if( property != null )
            {
                if( ! getXmlNodes( (IModelElementForXml) element, property ).isEmpty() )
                {
                    return true;
                }
            }
            else
            {
                if( ( (IModelElementForXml) element ).getXmlElement() != null )
                {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private static List<XmlNode> getXmlNodes( final IModelElementForXml modelElement,
                                              final ModelProperty property )
    {
        if( property instanceof ListProperty )
        {
            final ModelElementList<?> list = (ModelElementList<?>) property.invokeGetterMethod( modelElement );
            final List<XmlNode> xmlNodes = new ArrayList<XmlNode>();
            
            for( IModelElement element : list )
            {
                if( element instanceof IModelElementForXml )
                {
                    final XmlNode xmlNode = ( (IModelElementForXml) element ).getXmlElement();
                    
                    if( xmlNode != null )
                    {
                        xmlNodes.add( xmlNode );
                    }
                }
            }
            
            return xmlNodes;
        }
        else
        {
            final XmlNode xmlNode = modelElement.getXmlNode( property );
            
            if( xmlNode != null )
            {
                return Collections.singletonList( xmlNode );
            }
            else
            {
                return Collections.emptyList();
            }
        }
    }
    
    private static final class Range
    {
        private boolean initialized = false;
        private int start;
        private int end;
        
        public Range()
        {
            this.initialized = false;
        }
        
        public boolean isInitialized()
        {
            return this.initialized;
        }
        
        public void merge( final int start,
                           final int end )
        {
            if( this.initialized )
            {
                this.start = ( start < this.start ? start : this.start );
                this.end = ( end > this.end ? end : this.end );
            }
            else
            {
                this.start = start;
                this.end = end;
                this.initialized = true;
            }
        }
        
        public TextSelection toTextSelection()
        {
            return ( this.initialized ? new TextSelection( this.start, this.end - this.start ) : null );
        }
    }

	// *********************
	// ISapphirePart Methods
	// *********************
	
    public ISapphirePart getParentPart()
    {
        return this.editor;
    }
    
    @SuppressWarnings( "unchecked" )
    public final <T> T getNearestPart( final Class<T> partType )
    {
        if( partType.isAssignableFrom( getClass() ) )
        {
            return (T) this;
        }
        else
        {
            if( this.editor != null )
            {
                return this.editor.getNearestPart( partType );
            }
            else
            {
                return null;
            }
        }
    }
    
    public IModelElement getModelElement()
    {
        return this.rootModelElement;
    }
    
    public Action getAction( String id )
    {
        if( this.editor != null )
        {
            return this.editor.getAction( id );
        }
        else
        {
            return null;
        }
    }
    
    public IStatus getValidationState()
    {
        throw new UnsupportedOperationException();
    }
    
    public String getHelpContextId()
    {
        return null;
    }
    
    public SapphireImageCache getImageCache()
    {
        return this.editor.getImageCache();
    }
    
    public void collectAllReferencedProperties( final Set<ModelProperty> collection )
    {
        throw new UnsupportedOperationException();
    }
    
    public void addListener( final SapphirePartListener listener )
    {
        throw new UnsupportedOperationException();
    }
    
    public void removeListener( final SapphirePartListener listener )
    {
        throw new UnsupportedOperationException();
    }
    
    public void dispose()
    {
    }

}