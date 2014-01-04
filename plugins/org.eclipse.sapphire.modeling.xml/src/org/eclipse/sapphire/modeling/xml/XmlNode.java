/******************************************************************************
 * Copyright (c) 2014 Oracle
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

import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.modeling.ValidateEditException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class XmlNode
{
    private final XmlResourceStore store;
    private final XmlElement parent;
    private final Node domNode;
    private List<Listener> listeners;
    
    public XmlNode( final XmlResourceStore store,
                    final XmlElement parent,
                    final Node domNode )
    {
        if( store == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( domNode == null )
        {
            throw new IllegalArgumentException();
        }

        this.store = store;
        this.parent = parent;
        this.domNode = domNode;
    }
    
    public final XmlResourceStore getResourceStore()
    {
        return this.store;
    }
    
    public final XmlElement getParent()
    {
        return this.parent;
    }
    
    public Node getDomNode()
    {
        return this.domNode;
    }
    
    public final void validateEdit()
    
        throws ValidateEditException
        
    {
        this.store.validateEdit();
    }
    
    @Override
    public final boolean equals( final Object obj )
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
    public final int hashCode()
    {
        return this.domNode.hashCode();
    }
    
    public abstract String getText();
    
    public abstract void setText( String text );
    
    @Override
    public final String toString()
    {
        try
        {
            final StringWriter sw = new StringWriter();
            
            final DOMSource source = new DOMSource( this.domNode );
            final StreamResult result = new StreamResult( sw );
            
            final TransformerFactory factory = TransformerFactory.newInstance();
            final Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
            
            transformer.transform( source, result );
            
            return sw.toString();
        }
        catch( Exception e )
        {
            e.printStackTrace();
            return super.toString();
        }
    }
    
    public final void format()
    {
        validateEdit();
        
        removeFormatting();
         
        int depth = 0;
         
        if( this.domNode.getParentNode() != null ) 
        {
            for( Node n = this.domNode.getParentNode(); n.getNodeType() == Node.ELEMENT_NODE; n = n.getParentNode() )
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
            buf.append( "    " );
        }
         
        final String formatting = buf.toString();
         
        final Document document = this.domNode.getOwnerDocument();
        final Node parent = this.domNode.getParentNode();
         
        if( parent.getNodeType() == Node.ELEMENT_NODE ||
            ( parent.getNodeType() == Node.DOCUMENT_NODE && this.domNode.getPreviousSibling() != null ) )
        {
            final Text textBeforeOpeningTag = document.createTextNode( formatting );
            parent.insertBefore( textBeforeOpeningTag, this.domNode );
        }
        
        if( this.domNode.getNodeType() == Node.ELEMENT_NODE && this.domNode.getChildNodes().getLength() > 0 )
        {
            final Text textBeforeClosingTag = document.createTextNode( formatting );
            this.domNode.appendChild( textBeforeClosingTag );
        }
        
        if( this instanceof XmlElement )
        {
            final int depthPlusOne = depth + 1;
            final XmlElement element = (XmlElement) this;
            
            for( XmlNode child : element.getChildElements() )
            {
                child.format( depthPlusOne );
            }
            
            for( XmlNode child : element.getComments() )
            {
                child.format( depthPlusOne );
            }
        }
    }
     
    public final void removeFormatting()
    {
        validateEdit();
        
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
        
        if( this instanceof XmlElement )
        {
            final XmlElement element = (XmlElement) this;
            
            for( XmlNode child : element.getChildElements() )
            {
                child.removeFormatting();
            }
            
            for( XmlNode child : element.getComments() )
            {
                child.removeFormatting();
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
    
    public final void addListener( final Listener listener )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        final List<Listener> listeners = new ArrayList<Listener>();
        
        if( this.listeners != null )
        {
            listeners.addAll( this.listeners );
        }
        
        listeners.add( listener );
        this.listeners = listeners;
    }
    
    public final void removeListener( final Listener listener )
    {
        if( this.listeners != null && this.listeners.contains( listener ) )
        {
            if( this.listeners.size() == 1 )
            {
                this.listeners = null;
            }
            else
            {
                final List<Listener> listeners = new ArrayList<Listener>();
                
                for( Listener x : this.listeners )
                {
                    if( ! x.equals( listener ) )
                    {
                        listeners.add( x );
                    }
                }
                
                this.listeners = listeners;
            }
        }
    }
    
    protected final void notifyListeners( final Event event )
    {
        if( this.listeners != null )
        {
            for( Listener listener : this.listeners )
            {
                try
                {
                    listener.handle( event );
                }
                catch( Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                }
            }
        }
    }
    
    public enum EventType
    {
        PRE_CHILD_ELEMENT_ADD,
        POST_CHILD_ELEMENT_ADD,
        PRE_CHILD_ELEMENT_REMOVE,
        POST_CHILD_ELEMENT_REMOVE
    }
    
    public static class Event
    {
        private final EventType type;
        private final XmlNode node;
        
        public Event( final EventType type,
                      final XmlNode node )
        {
            this.type = type;
            this.node = node;
        }
        
        public EventType getType()
        {
            return this.type;
        }
        
        public XmlNode getNode()
        {
            return this.node;
        }
    }
    
    public static abstract class Listener
    {
        public abstract void handle( Event event );
    }
    
}
