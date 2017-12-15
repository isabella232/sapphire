/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [bugzilla 329114] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.help.IContext;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.IModelParticle;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Resource;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.xml.XmlElement;
import org.eclipse.sapphire.modeling.xml.XmlNode;
import org.eclipse.sapphire.modeling.xml.XmlResource;
import org.eclipse.sapphire.modeling.xml.XmlValueBindingImpl;
import org.eclipse.sapphire.ui.internal.SapphireActionManager;
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
    private final SapphireActionManager actionsManager;
    
    public SapphireEditorFormPage( final SapphireEditor editor,
                                   final IModelElement rootModelElement ) 
    {
        super( editor, null, null );

        this.editor = editor;
        this.rootModelElement = rootModelElement;
        this.actionsManager = new SapphireActionManager( this, getActionContexts() );
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
        return this.rootModelElement.adapt( ITextEditor.class );
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
                final List<XmlNode> xmlNodes = getXmlNodes( element, property );
                
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
                            else
                            {
                                range.merge( domNode.getStartOffset(), domNode.getEndOffset() );
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
                IModelElement modElement = element;
                Resource resource = modElement.resource();
                XmlElement xmlElement = null;
                
                if( resource != null )
                {
                    xmlElement = ( (XmlResource) resource ).getXmlElement();
                }
                
                while( xmlElement == null && modElement != null )
                {
                    final IModelParticle parent = modElement.parent();
                    
                    if( parent instanceof ModelElementList )
                    {
                        modElement = (IModelElement) parent.parent();
                    }
                    else
                    {
                        modElement = (IModelElement) parent;
                    }
                    
                    if( modElement != null )
                    {
                        resource = modElement.resource();
                        
                        if( resource != null )
                        {
                            xmlElement = ( (XmlResource) resource ).getXmlElement();
                        }
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
    
    private static List<XmlNode> getXmlNodes( final IModelElement modelElement,
                                              final ModelProperty property )
    {
        if( property instanceof ListProperty )
        {
            final ModelElementList<?> list = modelElement.read( (ListProperty) property );
            final List<XmlNode> xmlNodes = new ArrayList<XmlNode>();
            
            for( IModelElement element : list )
            {
                final Resource resource = element.resource();
                
                if( resource instanceof XmlResource )
                {
                    final XmlNode xmlNode = ( (XmlResource) resource ).getXmlElement();
                    
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
            final Resource resource = modelElement.resource();
            
            if( resource instanceof XmlResource )
            {
                final XmlResource r = (XmlResource) resource;
                final XmlNode xmlNode = ( (XmlValueBindingImpl) r.binding( (ValueProperty ) property ) ).getXmlNode();
                
                if( xmlNode != null )
                {
                    return Collections.singletonList( xmlNode );
                }
            }
            
            return Collections.emptyList();
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
    
    public IStatus getValidationState()
    {
        throw new UnsupportedOperationException();
    }
    
    public IContext getDocumentationContext()
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
    
    public Set<String> getActionContexts()
    {
        return Collections.singleton( SapphireActionSystem.CONTEXT_EDITOR_PAGE );
    }
    
    public final String getMainActionContext()
    {
        return this.actionsManager.getMainActionContext();
    }
    
    public final SapphireActionGroup getActions()
    {
        return this.actionsManager.getActions();
    }
    
    public final SapphireActionGroup getActions( final String context )
    {
        return this.actionsManager.getActions( context );
    }
    
    public final SapphireAction getAction( final String id )
    {
        return this.actionsManager.getAction( id );
    }
    
    public void dispose()
    {
        this.actionsManager.dispose();
    }

}