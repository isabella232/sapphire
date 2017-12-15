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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.sapphire.modeling.xml.schema.XmlContentModel;
import org.eclipse.sapphire.modeling.xml.schema.XmlDocumentSchema;
import org.eclipse.sapphire.modeling.xml.schema.XmlDocumentSchemasCache;
import org.eclipse.sapphire.modeling.xml.schema.XmlElementDefinition;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class XmlElement

    extends XmlNode
    
{
    private final XmlElement parent;
    private final Element domElement;
    private QName qname;
    private XmlContentModel contentModel;
    private boolean contentModelInitialized;
    
    public XmlElement( final XmlElement parent,
                       final Element domElement,
                       final ModelStoreForXml modelStoreForXml )
    {
        super( domElement, modelStoreForXml );

        this.parent = parent;
        this.domElement = domElement;
        this.qname = null;
        this.contentModel = null;
        this.contentModelInitialized = false;
    }
    
    public XmlElement getParent()
    {
        return this.parent;
    }
    
    @Override
    public Element getDomNode()
    {
        return this.domElement;
    }
    
    public String getLocalName()
    {
        return this.domElement.getLocalName();
    }
    
    public String getNamespace()
    {
        return this.domElement.getNamespaceURI();
    }
    
    public QName getQualifiedName()
    {
        if( this.qname == null )
        {
            this.qname = new QName( getNamespace(), getLocalName() );
        }
        
        return this.qname;
    }
    
    public String getSchemaLocation()
    {
        final String namespace = getNamespace();
        
        if( namespace != null )
        {
            final Element root = this.domElement.getOwnerDocument().getDocumentElement();
            final NamedNodeMap attributes = root.getAttributes();
            
            for( int i = 0, n = attributes.getLength(); i < n; i++ )
            {
                final Attr attr = (Attr) attributes.item( i );
                final String attrLocalName = attr.getLocalName();
                
                if( attrLocalName != null && attrLocalName.equals( "schemaLocation" ) )
                {
                    final String[] segments = attr.getValue().split( "[\\s]+" );
                    boolean grabNextSegment = false;
                    
                    for( int j = 0, m = segments.length; j < m; j++ )
                    {
                        if( j % 2 == 0 )
                        {
                            if( segments[ j ].equals( namespace ) )
                            {
                                grabNextSegment = true;
                            }
                        }
                        else
                        {
                            if( grabNextSegment )
                            {
                                return segments[ j ];
                            }
                        }
                    }
                    
                    return namespace;
                }
            }
            
            return namespace;
        }
                
        return null;
    }
    
    public XmlContentModel getContentModel()
    {
        if( ! this.contentModelInitialized )
        {
            if( this.parent == null )
            {
                String schemaLocation = getSchemaLocation();
                
                // Try to find baseLocation and systemId of the DTD 
            	String baseLocation = null;
                if (schemaLocation == null)
                {
                	final DocumentType type = this.domElement.getOwnerDocument().getDoctype();
                	if (type != null)
                	{
                		baseLocation = getModelStoreForXml().getFile().getAbsolutePath();
                		schemaLocation = type.getSystemId();
                	}
                }
                
                if( schemaLocation != null )
                {
                    final XmlDocumentSchema xmlDocumentSchema = XmlDocumentSchemasCache.getSchema( schemaLocation, baseLocation );
                    
                    if( xmlDocumentSchema != null )
                    {
                        final XmlElementDefinition xmlElementDefinition = xmlDocumentSchema.getElement( getLocalName() );
                        
                        if( xmlElementDefinition != null )
                        {
                            this.contentModel = xmlElementDefinition.getContentModel();
                        }
                    }
                }
            }
            else
            {
                final XmlContentModel parentXmlContentModel = this.parent.getContentModel();
                
                if( parentXmlContentModel != null )
                {
                    this.contentModel = parentXmlContentModel.findChildElementContentModel( getQualifiedName() );
                }
            }
            
            this.contentModelInitialized = true;
        }
        
        return this.contentModel;
    }
    
    @Override
    protected String getTextInternal()
    {
        final NodeList nodes = this.domElement.getChildNodes();
   
        String str = null;
        StringBuilder buf = null;
         
        for( int i = 0, n = nodes.getLength(); i < n; i++ )
        {
            final Node node = nodes.item( i );
            final int nodeType = node.getNodeType();
             
            if( nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE )
            {
                final String val = node.getNodeValue();
                 
                if( buf != null )
                {
                    buf.append( val );
                }
                else if( str != null )
                {
                    buf = new StringBuilder();
                    buf.append( str );
                    buf.append( val );
                    
                    str = null;
                }
                else
                {
                    str = val;
                }
            }
        }
         
        if( buf != null )
        {
            return buf.toString();
        }
        else
        {
            return ( str != null ? str : EMPTY_STRING );
        }
    }
    
    @Override
    public void setText( String elementText )
    {
        if( elementText == null )
        {
            elementText = EMPTY_STRING;
        }
         
        final NodeList elementChildren = this.domElement.getChildNodes();
        Text text = null;
         
        for( int i = 0, n = elementChildren.getLength(); i < n; i++ )
        {
            final Node child = elementChildren.item( i );
            
            if( text == null && child.getNodeType() == Node.TEXT_NODE )
            {
                text = (Text) child;
            }
            else
            {
                this.domElement.removeChild( elementChildren.item( i ) );
            }
        }
         
        if( text != null )
        {
            text.setData( elementText );
        }
        else
        {
            this.domElement.appendChild( this.domElement.getOwnerDocument().createTextNode( elementText ) );
        }
    }
    
    public XmlAttribute getAttribute( final String name,
                                      final boolean createIfNecessary )
    {
        Attr attrNode = this.domElement.getAttributeNode( name );
        
        if( attrNode == null && createIfNecessary )
        {
            attrNode = this.domElement.getOwnerDocument().createAttribute( name );
            this.domElement.setAttributeNode( attrNode );
        }
        
        if( attrNode == null )
        {
            return null;
        }
        else
        {
            return new XmlAttribute( attrNode, this.getModelStoreForXml() );
        }
    }
    
    public String getAttributeText( final String name )
    {
        final XmlAttribute attr = getAttribute( name, false );
        
        if( attr == null )
        {
            return EMPTY_STRING;
        }
        else
        {
            return attr.getText();
        }
    }
    
    public void setAttributeText( final String name,
                                  final String value,
                                  final boolean removeIfNullOrEmpty )
    {
        final String val = ( value == null ? EMPTY_STRING : value );
        
        if( val.length() == 0 )
        {
            final XmlAttribute attr = getAttribute( name, false );
            
            if( attr != null)
            {
                if (removeIfNullOrEmpty)
                {
                    attr.remove();
                }
                else
                {
                    getAttribute( name, true ).setText( val );
                }
            } 
        }
        else
        {
            getAttribute( name, true ).setText( val );
        }
    }
    
    public List<XmlElement> getChildElements()
    {
        Element firstElement = null;
        List<XmlElement> elements = null;
        final NodeList children = this.domElement.getChildNodes();
        
        for( int i = 0, n = children.getLength(); i < n; i++ )
        {
            final Node node = children.item( i );
    
            if( node.getNodeType() == Node.ELEMENT_NODE )
            {
                final Element element = (Element) node;
                
                if( elements != null )
                {
                    elements.add( new XmlElement( this, element, this.getModelStoreForXml() ) );
                }
                else if( firstElement != null )
                {
                    elements = new ArrayList<XmlElement>();
                    elements.add( new XmlElement( this, firstElement, this.getModelStoreForXml() ) );
                    elements.add( new XmlElement( this, element, this.getModelStoreForXml() ) );
                    firstElement = null;
                }
                else
                {
                    firstElement = element;
                }
            }
        }
         
        if( elements != null )
        {
            return elements;
        }
        else if( firstElement != null )
        {
            return Collections.singletonList( new XmlElement( this, firstElement, this.getModelStoreForXml() ) );
        }
        else
        {
            return Collections.emptyList();
        }
    }

    public List<XmlElement> getChildElements( final QName childElementName )
    {
        final String namespace = this.domElement.getNamespaceURI();
    
        Element firstElement = null;
        List<XmlElement> elements = null;
        final NodeList children = this.domElement.getChildNodes();
         
        for( int i = 0, n = children.getLength(); i < n; i++ )
        {
            final Node node = children.item( i );
    
            if( node.getNodeType() == Node.ELEMENT_NODE &&
                equal( node.getNamespaceURI(), namespace ) &&
                childElementName.equals( new QName( node.getNamespaceURI(), node.getLocalName() ) ) )
            {
                final Element element = (Element) node;
                
                if( elements != null )
                {
                    elements.add( new XmlElement( this, element, this.getModelStoreForXml() ) );
                }
                else if( firstElement != null )
                {
                    elements = new ArrayList<XmlElement>();
                    elements.add( new XmlElement( this, firstElement, this.getModelStoreForXml() ) );
                    elements.add( new XmlElement( this, element, this.getModelStoreForXml() ) );
                    firstElement = null;
                }
                else
                {
                    firstElement = element;
                }
            }
        }
        
        if( elements != null )
        {
            return elements;
        }
        else if( firstElement != null )
        {
            return Collections.singletonList( new XmlElement( this, firstElement, this.getModelStoreForXml() ) );
        }
        else
        {
            return Collections.emptyList();
        }
    }
    
    public List<XmlElement> getChildElements( final String childElementName )
    {
        return getChildElements( createQualifiedName( childElementName ) );
    }

    public XmlElement getChildElement( final QName qname,
                                       final boolean createIfNecessary )
    {
        final String namespace = qname.getNamespaceURI();
        final String localName = qname.getLocalPart();
        final NodeList children = this.domElement.getChildNodes();
         
        for( int i = 0, n = children.getLength(); i < n; i++ )
        {
            final Node node = children.item( i );
             
            if( node.getNodeType() == Node.ELEMENT_NODE &&
                equal( normalizeToNull( node.getNamespaceURI() ), normalizeToNull( namespace ) ) &&
                node.getLocalName().equals( localName ) )
            {
                return new XmlElement( this, (Element) node, this.getModelStoreForXml() );
            }
        }
         
        if( createIfNecessary )
        {
            return addChildElement( qname );
        }
        else
        {
            return null;
        }
    }
    
    public XmlElement getChildElement( final String name,
                                       final boolean createIfNecessary )
    {
        return getChildElement( createQualifiedName( name ), createIfNecessary );
    }
    
    public XmlElement addChildElement( final QName name )
    {
        final Document document = this.domElement.getOwnerDocument();
        final NodeList nodes = this.domElement.getChildNodes();
        
        // Find the insertion position.
        
        final XmlContentModel xmlContentModel = getContentModel();
        int position = ( xmlContentModel == null ? nodes.getLength() : xmlContentModel.findInsertionPosition( nodes, name ) );
        
        // Convert the insertion position into a reference node.
        
        Node refChild = ( position < nodes.getLength() ) ? nodes.item( position ) : null;
        
        int prevPosition = position - 1;
        Node prevChild = ( prevPosition < nodes.getLength()) ? nodes.item( prevPosition ) : null;
        
        if( prevChild != null && prevChild.getNodeType() == Node.TEXT_NODE && 
            prevChild.getNodeValue().trim().length() == 0 )
        {
            refChild = prevChild;
            position = prevPosition;
        }
        
        prevPosition = position - 1;
        prevChild = ( prevPosition < nodes.getLength()) ? nodes.item( prevPosition ) : null;
        
        // Create the new element and insert it in the correct spot.
        
        final String namespace = name.getNamespaceURI();
        final Element element;
        
        if( namespace == null || namespace.length() == 0 )
        {
            element = document.createElementNS( null, name.getLocalPart() );
        }
        else
        {
            final String prefix = findNamespacePrefix( namespace, name.getPrefix() );
    
            final String qname
                = ( ( prefix == null || prefix.length() == 0 ) 
                    ? name.getLocalPart() 
                    : prefix + ":" + name.getLocalPart() );
        
            element = document.createElementNS( namespace, qname );
        }
        
        this.domElement.insertBefore( element, refChild );
        
        final XmlElement wrappedElement = new XmlElement( this, element, this.getModelStoreForXml() );
        
        if( this.domElement.getNodeType() == Node.ELEMENT_NODE && this.domElement.getChildNodes().getLength() == 1 )
        {
            format();
        }
        else
        {
            wrappedElement.format();
        }
        
        return wrappedElement;
    }
    
    public XmlElement addChildElement( final String name )
    {
        return addChildElement( createQualifiedName( name ) );
    }
    
    private String findNamespacePrefix( final String namespace,
                                        final String defaultPrefix )
    {
        String prefix = null;
        
        if( namespace != null && namespace.length() > 0 )
        {
            final String ns = getNamespace();
            
            if( ns != null && ns.equals( namespace ) )
            {
                prefix = this.domElement.getPrefix();
            }
            else
            {
                Element el = this.domElement;
                boolean found = false;
                
                while( el != null && ! found )
                {
                    final NamedNodeMap attributes = el.getAttributes();
                    
                    for( int i = 0, n = attributes.getLength(); i < n; i++ )
                    {
                        final Attr attr = (Attr) attributes.item( i );
                        final String attrName = attr.getName();
                        final String attrValue = attr.getValue();
                        
                        if( attrName.equals( "xmlns" ) ) //$NON-NLS-1$
                        {
                            if( attrValue.equals( namespace ) )
                            {
                                found = true;
                                break;
                            }
                        }
                        else if( attrName.startsWith( "xmlns:" ) ) //$NON-NLS-1$
                        {
                            if( attrValue.equals( namespace ) )
                            {
                                prefix = attrName.substring( 6 );
                                found = true;
                                break;
                            }
                        }
                    }
                    
                    final Node parent = el.getParentNode();
                    
                    if( parent instanceof Element )
                    {
                        el = (Element) parent;
                    }
                    else
                    {
                        el = null;
                    }
                }
                
                if( ! found )
                {
                    prefix = defaultPrefix;
                    
                    final String xmlnsAttrName = "xmlns:" + defaultPrefix; //$NON-NLS-1$
                    final Element root = this.domElement.getOwnerDocument().getDocumentElement();
                    root.setAttribute( xmlnsAttrName, namespace );
                    
                    if( ! namespace.equals( "http://www.w3.org/2001/XMLSchema-instance" ) ) //$NON-NLS-1$
                    {
                        final XmlContentModel xmlContentModel = getContentModel();
                        String schemaLocation = null;
                        
                        if( xmlContentModel != null )
                        {
                            schemaLocation = xmlContentModel.getSchema().getSchemaLocation( namespace );
                        }
                        
                        addSchemaLocation( namespace, schemaLocation );
                    }
                }
            }
        }
        
        return prefix;
    }
    
    private void addSchemaLocation( final String namespace,
                                    final String schemaLocation )
    {
        if( schemaLocation != null )
        {
            final String xsiNamespacePrefix 
                = findNamespacePrefix( "http://www.w3.org/2001/XMLSchema-instance", "xsi" );
                
            final String schemaLocationAttrName 
                = ( xsiNamespacePrefix == null ? "schemaLocation" : xsiNamespacePrefix + ":schemaLocation" );
            
            final Element root = this.domElement.getOwnerDocument().getDocumentElement();
            final String existingSchemaLocations = root.getAttribute( schemaLocationAttrName );
            final Map<String,String> schemaLocations = new LinkedHashMap<String,String>();
            
            if( existingSchemaLocations != null && existingSchemaLocations.length() > 0 )
            {
                String ns = null;
                
                for( String segment : existingSchemaLocations.split( "[\\s]+" ) )
                {
                    if( ns == null )
                    {
                        ns = segment;
                    }
                    else
                    {
                        schemaLocations.put( ns, segment );
                        ns = null;
                    }
                }
            }
            
            schemaLocations.put( namespace, schemaLocation );
            
            final StringBuilder buf = new StringBuilder();
            
            for( Map.Entry<String,String> entry : schemaLocations.entrySet() )
            {
                if( buf.length() > 0 )
                {
                    buf.append( ' ' );
                }
                
                buf.append( entry.getKey() );
                buf.append( ' ' );
                buf.append( entry.getValue() );
            }
            
            root.setAttribute( schemaLocationAttrName, buf.toString() );
        }
    }
    
    public XmlNode getChildNode( final XmlPath path,
                                 final boolean createIfNecessary )
    {
        XmlElement el = this;
        
        for( XmlPath.Segment segment : path.getSegments() )
        {
            XmlNode node = el.getChildNode( segment, createIfNecessary );
            
            if( node instanceof XmlElement )
            {
                el = (XmlElement) node;
            }
            else
            {
                return node;
            }
        }
        
        return el;
    }

    public XmlNode getChildNode( final XmlPath.Segment pathSegment,
                                 final boolean createIfNecessary )
    {
        final XmlPath.Segment resolvedPathSegment = resolveXmlPathSegment( pathSegment );
        final QName qname = resolvedPathSegment.getQualifiedName();
        
        if( resolvedPathSegment.isAttribute() )
        {
            return getAttribute( qname.getLocalPart(), createIfNecessary );
        }
        else if( resolvedPathSegment.isComment() )
        {
            return getMetaComment( qname.getLocalPart(), createIfNecessary );
        }
        else
        {
            return getChildElement( qname, createIfNecessary );
        }
    }
    
    public String getChildNodeText( final XmlPath path )
    {
        return getChildNodeText( path, false );
    }
    
    public String getChildNodeText( final XmlPath path,
                                    final boolean removeExtraWhitespace )
    {
        final XmlNode node = getChildNode( path, false );
        
        if( node != null )
        {
            return node.getText( removeExtraWhitespace );
        }
        else
        {
            return EMPTY_STRING;
        }
    }
    
    public String getChildNodeText( final String path )
    {
        return getChildNodeText( new XmlPath( path ) );
    }

    public String getChildNodeText( final String path,
                                    final boolean removeExtraWhitespace )
    {
        return getChildNodeText( new XmlPath( path ), removeExtraWhitespace );
    }
    
    public void setChildNodeText( final XmlPath path,
                                  final String text,
                                  final boolean removeIfNullOrEmpty )
    {
        if( removeIfNullOrEmpty && ( text == null || text.trim().length() == 0 ) )
        {
            removeChildNode( path );
        }
        else
        {
            getChildNode( path, true ).setText( text );
        }
    }
    
    public void setChildNodeText( final String path,
                                  final String text,
                                  final boolean removeIfNullOrEmpty )
    {
        setChildNodeText( new XmlPath( path ), text, removeIfNullOrEmpty );
    }
    
    public void removeChildNode( final XmlPath path )
    {
        removeChildNode( this, path, 0 );
    }

    private static void removeChildNode( final XmlElement el,
                                         final XmlPath path,
                                         final int pathPosition )
    {
        final XmlPath.Segment segment = path.getSegment( pathPosition );
        final XmlNode child = el.getChildNode( segment, false );
        
        if( child != null )
        {
            if( pathPosition == path.getSegmentCount() - 1 )
            {
                child.remove();
            }
            else
            {
                if( child instanceof XmlElement )
                {
                    final XmlElement childElement = (XmlElement) child;
                    
                    removeChildNode( childElement, path, pathPosition + 1 );
                    
                    if( childElement.isEmpty() )
                    {
                        childElement.remove();
                    }
                }
            }
        }
    }
    
    public void swap( final XmlElement y )
    {
        // It should be possible to implement this more optimally, but the
        // bookmark approach yields a very simple implementation which 
        // is therefore easier to prove as correct in all cases.
        
        final Node parent = this.domElement.getParentNode();
        final Document document = parent.getOwnerDocument();
        
        final Node xBookmark = document.createTextNode( EMPTY_STRING );
        parent.insertBefore( xBookmark, this.domElement );
        
        final Node yBookmark = document.createTextNode( EMPTY_STRING );
        parent.insertBefore( yBookmark, y.domElement );
        
        parent.removeChild( this.domElement );
        parent.removeChild( y.domElement );
        
        parent.insertBefore( this.domElement, yBookmark );
        parent.insertBefore( y.domElement, xBookmark );
        
        parent.removeChild( xBookmark );
        parent.removeChild( yBookmark );
    }

    @Override
    public void remove()
    {
        final Node parentDomNode = this.domElement.getParentNode();
        
        if( parentDomNode != null )
        {
            final Node previousSibling = this.domElement.getPreviousSibling();
            
            parentDomNode.removeChild( this.domElement );
             
            if( previousSibling != null && previousSibling.getNodeType() == Node.TEXT_NODE &&
                previousSibling.getNodeValue().trim().length() == 0 )
            {
                parentDomNode.removeChild( previousSibling );
            }
        }
    }
    
    public boolean isEmpty()
    {
        final NodeList nodes = this.domElement.getChildNodes();
         
        for( int i = 0, n = nodes.getLength(); i < n; i++ )
        {
            final Node child = nodes.item( i );
             
            if( child.getNodeType() != Node.TEXT_NODE || child.getNodeValue().trim().length() > 0 )
            {
                return false;
            }
        }
        
        return true;
    }
    
    public XmlComment addComment( final String commentText )
    {
        final Document document = this.domElement.getOwnerDocument();
        final NodeList nodes = this.domElement.getChildNodes();
        int position = 0;
         
        for( int n = nodes.getLength(); position < n; position++ )
        {
            final Node child = nodes.item( position );
             
            if( child.getNodeType() != Node.COMMENT_NODE && child.getNodeType() != Node.TEXT_NODE )
            {
                break;
            }
        }
         
        Node refChild = ( position < nodes.getLength() ) ? nodes.item( position ) : null;
         
        int prevPosition = position - 1;
        Node prevChild = ( prevPosition < nodes.getLength()) ? nodes.item( prevPosition ) : null;
         
        if( prevChild != null && prevChild.getNodeType() == Node.TEXT_NODE && 
            prevChild.getNodeValue().trim().length() == 0 )
        {
            refChild = prevChild;
            position = prevPosition;
        }
         
        prevPosition = position - 1;
        prevChild = ( prevPosition < nodes.getLength()) ? nodes.item( prevPosition ) : null;
    
        final Comment comment = document.createComment( commentText );
        this.domElement.insertBefore( comment, refChild );
        
        final XmlComment wrappedComment = new XmlComment( comment, this.getModelStoreForXml() );
         
        if( this.domElement.getNodeType() == Node.ELEMENT_NODE && this.domElement.getChildNodes().getLength() == 1 )
        {
            format();
        }
        else
        {
            wrappedComment.format();
        }
         
        return wrappedComment;
    }
     
    public XmlMetaComment getMetaComment( final String name,
                                          final boolean createIfNecessary )
    {
        final String prefix = name + ":"; //$NON-NLS-1$
        final NodeList children = this.domElement.getChildNodes();
        Comment xmlMetaComment = null;
        
        for( int i = 0, n = children.getLength(); i < n; i++ )
        {
            final Node child = children.item( i );
             
            if( child.getNodeType() == Node.COMMENT_NODE )
            {
                final Comment comment = (Comment) child;
                 
                if( comment.getNodeValue().trim().startsWith( prefix ) )
                {
                    xmlMetaComment = comment;
                }
            }
        }
        
        if( xmlMetaComment == null && createIfNecessary )
        {
            xmlMetaComment = addComment( prefix ).getDomComment();
        }
        
        if( xmlMetaComment == null )
        {
            return null;
        }
        else
        {
            return new XmlMetaComment( xmlMetaComment, this.getModelStoreForXml() );
        }
    }
     
    public String getMetaCommentText( final String name )
    {
        final XmlMetaComment comment = getMetaComment( name, false );
        
        if( comment != null )
        {
            return comment.getText();
        }
        else
        {
            return null;
        }
    }
     
    public void setMetaCommentText( final String name,
                                    String value )
    {
        if( value != null )
        {
            if( value.length() == 0 )
            {
                value = null;
            }
        }
         
        XmlMetaComment comment = getMetaComment( name, true );
        
        if( value != null )
        {
            comment.setText( value );
        }
        else
        {
            comment.remove();
        }
    }
    
    private XmlPath.Segment resolveXmlPathSegment( final XmlPath.Segment pathSegment )
    {
        String namespace = pathSegment.getQualifiedName().getNamespaceURI();
        
        if( pathSegment.isAttribute() || pathSegment.isComment() || namespace.length() != 0 )
        {
            return pathSegment;
        }
        else
        {
            final String prefix = pathSegment.getQualifiedName().getPrefix();
            
            if( prefix.length() == 0 )
            {
                namespace = this.domElement.getNamespaceURI();
            }
            
            final QName newQualifiedName 
                = new QName( namespace, pathSegment.getQualifiedName().getLocalPart(), prefix );
            
            return new XmlPath.Segment( newQualifiedName, false, false );
        }
    }
    
    private QName createQualifiedName( final String localName )
    {
        final String namespace = this.domElement.getNamespaceURI();
        return new QName( namespace, localName );
    }
    
    private static final boolean equal( final Object obj1, 
                                        final Object obj2 )
    {
        boolean objectsAreEqual = false;
         
        if( obj1 == obj2 )
        {
            objectsAreEqual = true;
        }
        else if( obj1 != null && obj2 != null )
        {
            objectsAreEqual = obj1.equals( obj2 );
        }
    
        return objectsAreEqual;
    }
    
    private static final String normalizeToNull( final String str )
    {
        if( str != null && str.equals( "" ) )
        {
            return null;
        }
        else
        {
            return str;
        }
    }
    
}
