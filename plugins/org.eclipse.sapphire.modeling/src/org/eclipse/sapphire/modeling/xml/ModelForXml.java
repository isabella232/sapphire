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

package org.eclipse.sapphire.modeling.xml;

import org.eclipse.sapphire.modeling.CorruptedModelStoreException;
import org.eclipse.sapphire.modeling.CorruptedModelStoreExceptionInterceptor;
import org.eclipse.sapphire.modeling.IModel;
import org.eclipse.sapphire.modeling.Model;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ModelForXml

    extends Model
    implements IModel, IModelElementForXml
    
{
    private static final String PI_XML_TARGET = "xml"; //$NON-NLS-1$
    private static final String PI_XML_DATA = "version=\"1.0\" encoding=\"UTF-8\""; //$NON-NLS-1$
    
    protected final ModelStoreForXml modelStoreForXml;
    private RootElementController rootElementController;
    
    public ModelForXml( final ModelElementType type,
                        final ModelStoreForXml modelStore )
    {
        super( type, modelStore );
        
        this.modelStoreForXml = modelStore;
        this.rootElementController = createRootElementController();
        this.rootElementController.init( modelStore, type );
    }
    
    protected abstract RootElementController createRootElementController();
    
    @Override
    public final boolean isCorrupted()
    {
        final Document document = this.modelStoreForXml.getDocument();
        Element root = document.getDocumentElement();

        if( document.getChildNodes().getLength() == 0 )
        {
            return true;
        }
        else
        {
            if( root == null )
            {
                return true;
            }
            else
            {
                return ! this.rootElementController.checkRootElement( this.modelStoreForXml.getDocument() );
            }
        }
    }
    
    public final XmlElement getXmlElement()
    {
        return getXmlElement( false );
    }
    
    public final XmlElement getXmlElement( final boolean createIfNecessary )
    {
        final Document document = this.modelStoreForXml.getDocument();
        Element root = document.getDocumentElement();
    
        if( document.getChildNodes().getLength() == 0 )
        {
            if( createIfNecessary )
            {
                fixMalformedDescriptor();
                root = document.getDocumentElement();
            }
        }
        else
        {
            final boolean isRootValid 
                = ( root == null ? false : this.rootElementController.checkRootElement( this.modelStoreForXml.getDocument() ) );
            
            if( isRootValid == false )
            {
                root = null;
                
                if( createIfNecessary )
                {
                    final CorruptedModelStoreExceptionInterceptor interceptor = getCorruptedModelStoreExceptionInterceptor();
                    
                    if( interceptor != null && interceptor.shouldAttemptRepair() == true )
                    {
                        fixMalformedDescriptor();
                        root = document.getDocumentElement();
                    }
                    else
                    {
                        throw new CorruptedModelStoreException();
                    }
                }
            }
        }
        
        if( root == null )
        {
            return null;
        }
        else
        {
            this.modelStoreForXml.registerModelElement( root, this );
            return new XmlElement( null, root, this.modelStoreForXml );
        }
    }
    
    public XmlNode getXmlNode( final ModelProperty property )
    {
        return getXmlNode( property, false );
    }
    
    public XmlNode getXmlNode( final ModelProperty property, 
                               final boolean createIfNecessary )
    {
        return null;
    }
    
    private final void fixMalformedDescriptor()
    {
        final Document document = this.modelStoreForXml.getDocument();
        
        // Remove all of the existing top-level nodes. Note that we have to copy the
        // items from the node list before removing any as removal seems to alter the
        // node list.
        
        final NodeList topLevelNodes = document.getChildNodes();
        final Node[] nodes = new Node[ topLevelNodes.getLength() ];
        
        for( int i = 0, n = nodes.length; i < n; i++ )
        {
            nodes[ i ] = topLevelNodes.item( i );
        }
        
        for( Node node : nodes )
        {
            document.removeChild( node );
        }
        
        // Add a new XML processing instruction and the root element.
        
        if( this.modelStoreForXml.isXmlDeclarationNeeded() )
        {
            addXmlProcessingInstruction( document );
        }
        
        this.rootElementController.createRootElement( this.modelStoreForXml.getDocument() );
    }
    
    private void addXmlProcessingInstruction( final Document document )
    {
        final NodeList nodes = document.getChildNodes();
        
        for( int i = 0, n = nodes.getLength(); i < n; i++ )
        {
            final Node node = nodes.item( i );
            
            if( node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE )
            {
                final ProcessingInstruction pi = (ProcessingInstruction) node;
                
                if( pi.getTarget().equals( PI_XML_TARGET ) )
                {
                    pi.setData( PI_XML_DATA );
                    return;
                }
            }
        }
        
        final ProcessingInstruction pi 
            = document.createProcessingInstruction( PI_XML_TARGET, PI_XML_DATA );
        
        document.insertBefore( pi, document.getFirstChild() );
    }
    
}
