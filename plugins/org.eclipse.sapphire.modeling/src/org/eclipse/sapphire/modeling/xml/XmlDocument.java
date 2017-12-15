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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class XmlDocument

    extends XmlNode
    
{
    private static final String PI_XML_TARGET = "xml"; //$NON-NLS-1$
    private static final String PI_XML_DATA = "version=\"1.0\" encoding=\"UTF-8\""; //$NON-NLS-1$

    private final Document document;
    
    public XmlDocument( final Document document, final ModelStoreForXml modelStoreForXml )
    {
        super( document, modelStoreForXml );
        
        this.document = document;
    }
    
    public XmlElement getRootElement( final RootElementController rootElementController )
    {
        return getRootElement( rootElementController, false );
    }
                               
    public XmlElement getRootElement( final RootElementController rootElementController,
                                      final boolean createIfNecessary )
    {
        return getRootElement( rootElementController, createIfNecessary, null );
    }

    public XmlElement getRootElement( final RootElementController rootElementController,
                                      final boolean createIfNecessary,
                                      final CorruptedModelStoreExceptionInterceptor interceptor )
    {
        Element root = this.document.getDocumentElement();
    
        if( this.document.getChildNodes().getLength() == 0 )
        {
            if( createIfNecessary )
            {
                root = createRootElement( rootElementController );
            }
        }
        else
        {
            final boolean isRootValid 
                = ( root == null ? false : rootElementController.checkRootElement( this.document ) );
            
            if( isRootValid == false )
            {
                root = null;
                
                if( createIfNecessary )
                {
                    if( interceptor != null && interceptor.shouldAttemptRepair() == true )
                    {
                        root = createRootElement( rootElementController );
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
            return new XmlElement( null, root, getModelStoreForXml() );
        }
    }
    
    private Element createRootElement( final RootElementController rootElementController )
    {
        // Remove all of the existing top-level nodes. Note that we have to copy the
        // items from the node list before removing any as removal seems to alter the
        // node list.
        
        final NodeList topLevelNodes = this.document.getChildNodes();
        final Node[] nodes = new Node[ topLevelNodes.getLength() ];
        
        for( int i = 0, n = nodes.length; i < n; i++ )
        {
            nodes[ i ] = topLevelNodes.item( i );
        }
        
        for( Node node : nodes )
        {
            this.document.removeChild( node );
        }
        
        // Add a new XML processing instruction and the root element.
        
        addXmlProcessingInstruction( this.document );
        rootElementController.createRootElement( this.document );
        
        return this.document.getDocumentElement();
    }
    
    public static void addXmlProcessingInstruction( final Document document )
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

    @Override
    protected String getTextInternal()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setText( final String text )
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

}
