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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class XmlNode
{
    protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
    
    private final Node domNode;
    private final ModelStoreForXml modelStoreForXml;
    
    public XmlNode( final Node domNode, final ModelStoreForXml modelStoreForXml )
    {
        if( domNode == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.domNode = domNode;
        this.modelStoreForXml = modelStoreForXml;
    }
    
    public ModelStoreForXml getModelStoreForXml() 
    {
    	return this.modelStoreForXml;
    }
    
    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof XmlNode )
        {
            return ( this.domNode == ( (XmlNode) obj ).domNode );
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public int hashCode()
    {
        return this.domNode.hashCode();
    }
    
    public Node getDomNode()
    {
        return this.domNode;
    }
    
    public final String getText()
    {
        return getText( false );
    }
    
    public final String getText( final boolean removeExtraWhitespace )
    {
        // The leading and trailing spaces are always removed. Internal spaces are only
        // collapsed if requested.
        
        String text = getTextInternal().trim();
        
        if( removeExtraWhitespace )
        {
            final StringBuilder buf = new StringBuilder();
            boolean skipNextWhitespace = true;
            
            for( int i = 0, n = text.length(); i < n; i++ )
            {
                final char ch = text.charAt( i );
                
                if( Character.isWhitespace( ch ) )
                {
                    if( ! skipNextWhitespace )
                    {
                        buf.append( ' ' );
                        skipNextWhitespace = true;
                    }
                }
                else
                {
                    buf.append( ch );
                    skipNextWhitespace = false;
                }
            }
            
            final int length = buf.length();
            
            if( length > 0 && buf.charAt( length - 1 ) == ' ' )
            {
                buf.deleteCharAt( length - 1 );
            }
            
            text = buf.toString();
        }
        
        return text;
    }
    
    protected abstract String getTextInternal();
    
    public abstract void setText( String text );
    
    @Override
    public String toString()
    {
        try
        {
            final StringWriter sw = new StringWriter();
            
            final DOMSource source = new DOMSource( this.domNode );
            final StreamResult result = new StreamResult( sw );
            
            final TransformerFactory factory = TransformerFactory.newInstance();
            final Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" ); //$NON-NLS-1$
            
            transformer.transform( source, result );
            
            return sw.toString();
        }
        catch( Exception e )
        {
            e.printStackTrace();
            return super.toString();
        }
    }
    
    public void format()
    {
        removeFormatting();
         
        int depth = 0;
         
        if (this.domNode.getParentNode() != null) {
        for( Node n = this.domNode.getParentNode(); n.getNodeType() == Node.ELEMENT_NODE;
             n = n.getParentNode() )
        {
            depth++;
        }
        }
         
        format( depth );
    }
     
    private void format( final int depth )
    {
        final StringBuilder buf = new StringBuilder();
         
        buf.append( '\n' );
         
        for( int i = 0; i < depth; i++ )
        {
            buf.append( "    " ); //$NON-NLS-1$
        }
         
        final String formatting = buf.toString();
         
        final Document document = this.domNode.getOwnerDocument();
        final Node parent = this.domNode.getParentNode();
         
        if( parent.getNodeType() == Node.ELEMENT_NODE ||
            parent.getNodeType() == Node.DOCUMENT_NODE )
        {
            final Text textBeforeOpeningTag = document.createTextNode( formatting );
            parent.insertBefore( textBeforeOpeningTag, this.domNode );
        }
        
        if( this.domNode.getNodeType() == Node.ELEMENT_NODE && this.domNode.getChildNodes().getLength() > 0 )
        {
            final Text textBeforeClosingTag = document.createTextNode( formatting );
            this.domNode.appendChild( textBeforeClosingTag );
        }
        
        final NodeList childNodeList = this.domNode.getChildNodes();
        final List<XmlNode> childNodesToFormat = new ArrayList<XmlNode>();
        
        for( int i = 0, n = childNodeList.getLength(); i < n; i++ )
        {
            final Node child = childNodeList.item( i );

            if( child.getNodeType() == Node.ELEMENT_NODE )
            {
                childNodesToFormat.add( new XmlElement( (XmlElement) this, (Element) child, this.modelStoreForXml ) );
            }
            else if( child.getNodeType() == Node.COMMENT_NODE )
            {
                childNodesToFormat.add( new XmlComment( child, this.getModelStoreForXml() ) );
            }
        }
        
        final int depthPlusOne = depth + 1;
        
        for( XmlNode childNode : childNodesToFormat )
        {
            childNode.format( depthPlusOne );
        }
    }
     
    public void removeFormatting()
    {
        final NodeList nodes = this.domNode.getChildNodes();
        final List<Node> textNodesToRemove = new ArrayList<Node>();
         
        for( int i = 0, n = nodes.getLength(); i < n; i++ )
        {
            final Node child = nodes.item( i );
             
            if( child.getNodeType() == Node.TEXT_NODE )
            {
                if( child.getNodeValue().trim().length() == 0 )
                {
                    textNodesToRemove.add( child );
                }
            }
        }
         
        for( Node n : textNodesToRemove )
        {
            this.domNode.removeChild( n );
        }
        
        for( int i = 0, n = nodes.getLength(); i < n; i++ )
        {
            final Node child = nodes.item( i );
            
            if( child.getNodeType() == Node.ELEMENT_NODE )
            {
                ( new XmlElement( (XmlElement) this, (Element) child, this.modelStoreForXml ) ).removeFormatting();
            }
        }
         
        final Node prevSibling = this.domNode.getPreviousSibling();
         
        if( prevSibling != null && prevSibling.getNodeType() == Node.TEXT_NODE )
        {
            if( prevSibling.getNodeValue().trim().length() == 0 )
            {
                this.domNode.getParentNode().removeChild( prevSibling );
            }
        }
    }

    public abstract void remove();
    
}
