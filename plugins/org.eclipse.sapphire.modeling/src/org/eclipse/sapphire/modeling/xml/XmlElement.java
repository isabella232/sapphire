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

import static org.eclipse.sapphire.modeling.xml.XmlUtil.EMPTY_STRING;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.sapphire.modeling.IdentityCache;
import org.eclipse.sapphire.modeling.util.ListFactory;
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
    private final IdentityCache<Element,XmlElement> elementsCache = new IdentityCache<Element,XmlElement>();
    private final IdentityCache<Attr,XmlAttribute> attributesCache = new IdentityCache<Attr,XmlAttribute>();
    private final IdentityCache<Comment,XmlComment> commentsCache = new IdentityCache<Comment,XmlComment>();
    private final IdentityCache<Comment,XmlMetaComment> metaCommentsCache = new IdentityCache<Comment,XmlMetaComment>();

    private QName qname;
    private XmlContentModel contentModel;
    private boolean contentModelInitialized;
    
    public XmlElement( final XmlResourceStore store,
                       final Element domElement )
    {
        this( store, null, domElement );
    }
    
    public XmlElement( final XmlElement parent,
                       final Element domElement )
    {
        this( parent.getResourceStore(), parent, domElement );
    }

    private XmlElement( final XmlResourceStore store,
                        final XmlElement parent,
                        final Element domElement )
    {
        super( store, parent, domElement );

        this.qname = null;
        this.contentModel = null;
        this.contentModelInitialized = false;
    }
    
    @Override
    public Element getDomNode()
    {
        return (Element) super.getDomNode();
    }
    
    public String getLocalName()
    {
        return getDomNode().getLocalName();
    }
    
    public String getNamespace()
    {
        return getDomNode().getNamespaceURI();
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
            final Element root = getDomNode().getOwnerDocument().getDocumentElement();
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
            final XmlElement parent = getParent();
            
            if( parent == null )
            {
                String schemaLocation = getSchemaLocation();
                String baseLocation = null;
                
                final File file = getResourceStore().adapt( File.class );
                
                if( file != null )
                {
                    baseLocation = file.getAbsolutePath();
                }
                        
                if( schemaLocation == null )
                {
                    final DocumentType type = getDomNode().getOwnerDocument().getDoctype();
                    
                    if( type != null )
                    {
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
                final XmlContentModel parentXmlContentModel = parent.getContentModel();
                
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
        final NodeList nodes = getDomNode().getChildNodes();
   
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
        validateEdit();
        
        if( elementText == null )
        {
            elementText = EMPTY_STRING;
        }
         
        final Element domElement = getDomNode();
        final NodeList elementChildren = domElement.getChildNodes();
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
                domElement.removeChild( elementChildren.item( i ) );
            }
        }
         
        if( text != null )
        {
            text.setData( elementText );
        }
        else
        {
            domElement.appendChild( domElement.getOwnerDocument().createTextNode( elementText ) );
        }
    }
    
    public List<XmlAttribute> getAttributes()
    {
        final ListFactory<XmlAttribute> result = new ListFactory<XmlAttribute>();
        final NamedNodeMap attributes = getDomNode().getAttributes();
        
        this.attributesCache.track();
        
        for( int i = 0, count = attributes.getLength(); i < count; i++ )
        {
            final Attr attribute = (Attr) attributes.item( i );
            XmlAttribute xmlAttribute = this.attributesCache.get( attribute );
            
            if( xmlAttribute == null )
            {
                xmlAttribute = new XmlAttribute( this, attribute );
                this.attributesCache.put( attribute, xmlAttribute );
            }

            result.add( xmlAttribute );
        }
        
        this.attributesCache.purge();
        
        return result.create();
    }
    
    public XmlAttribute getAttribute( final String name,
                                      final boolean createIfNecessary )
    {
        XmlAttribute attribute = null;
        
        for( XmlAttribute attr : getAttributes() )
        {
            if( equal( attr.getLocalName(), name ) )
            {
                attribute = attr;
                break;
            }
        }
        
        if( attribute == null && createIfNecessary )
        {
            validateEdit();
            
            final Element domElement = getDomNode();
            final Attr attr = domElement.getOwnerDocument().createAttribute( name );
            domElement.setAttributeNode( attr );
            attribute = new XmlAttribute( this, attr );
            this.attributesCache.put( attr, attribute );
        }
        
        return attribute;
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
        validateEdit();
        
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
        final ListFactory<XmlElement> result = new ListFactory<XmlElement>();
        final NodeList children = getDomNode().getChildNodes();
        
        this.elementsCache.track();
        
        for( int i = 0, count = children.getLength(); i < count; i++ )
        {
            final Node n = children.item( i );
            
            if( n.getNodeType() == Node.ELEMENT_NODE )
            {
                final Element element = (Element) n;
                XmlElement xmlElement = this.elementsCache.get( element );
            
                if( xmlElement == null )
                {
                    xmlElement = new XmlElement( this, element );
                    this.elementsCache.put( element, xmlElement );
                }
    
                result.add( xmlElement );
            }
        }
        
        this.elementsCache.purge();
        
        return result.create();
    }

    public List<XmlElement> getChildElements( final QName name )
    {
        final ListFactory<XmlElement> result = new ListFactory<XmlElement>();
        
        for( XmlElement element : getChildElements() )
        {
            if( equal( normalizeToNull( element.getNamespace() ), normalizeToNull( name.getNamespaceURI() ) ) &&
                equal( element.getLocalName(), name.getLocalPart() ) )
            {
                result.add( element );
            }
        }
        
        return result.create();
    }
    
    public List<XmlElement> getChildElements( final String name )
    {
        return getChildElements( createQualifiedName( name ) );
    }

    public XmlElement getChildElement( final QName name,
                                       final boolean createIfNecessary )
    {
        for( XmlElement element : getChildElements() )
        {
            if( equal( normalizeToNull( element.getNamespace() ), normalizeToNull( name.getNamespaceURI() ) ) &&
                equal( element.getLocalName(), name.getLocalPart() ) )
            {
                return element;
            }
        }
         
        if( createIfNecessary )
        {
            return addChildElement( name );
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
        validateEdit();
        
        final Element domElement = getDomNode();
        final Document document = domElement.getOwnerDocument();
        final NodeList nodes = domElement.getChildNodes();
        
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
        
        domElement.insertBefore( element, refChild );
        
        final XmlElement wrappedElement = new XmlElement( this, element );
        this.elementsCache.put( element, wrappedElement );
        
        if( domElement.getNodeType() == Node.ELEMENT_NODE && domElement.getChildNodes().getLength() == 1 )
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
        final Element domElement = getDomNode();
        String prefix = null;
        
        if( namespace != null && namespace.length() > 0 )
        {
            final String ns = getNamespace();
            
            if( ns != null && ns.equals( namespace ) )
            {
                prefix = domElement.getPrefix();
            }
            else
            {
                Element el = domElement;
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
                    final Element root = domElement.getOwnerDocument().getDocumentElement();
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
            
            final Element root = getDomNode().getOwnerDocument().getDocumentElement();
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
        validateEdit();
        
        if( removeIfNullOrEmpty && ( text == null || text.trim().length() == 0 ) && path.getSegmentCount() > 0 )
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

    private void removeChildNode( final XmlElement el,
                                  final XmlPath path,
                                  final int pathPosition )
    {
        validateEdit();
        
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
        validateEdit();
        
        // It should be possible to implement this more optimally, but the
        // bookmark approach yields a very simple implementation which 
        // is therefore easier to prove as correct in all cases.
        
        final Element domElement = getDomNode();
        final Node parent = domElement.getParentNode();
        final Document document = parent.getOwnerDocument();
        
        final Node xBookmark = document.createTextNode( EMPTY_STRING );
        parent.insertBefore( xBookmark, domElement );
        
        final Node yBookmark = document.createTextNode( EMPTY_STRING );
        parent.insertBefore( yBookmark, y.getDomNode() );
        
        parent.removeChild( domElement );
        parent.removeChild( y.getDomNode() );
        
        parent.insertBefore( domElement, yBookmark );
        parent.insertBefore( y.getDomNode(), xBookmark );
        
        parent.removeChild( xBookmark );
        parent.removeChild( yBookmark );
    }

    @Override
    public void remove()
    {
        validateEdit();
        
        final Element domElement = getDomNode();
        final Node parentDomNode = domElement.getParentNode();
        
        if( parentDomNode != null )
        {
            final Node previousSibling = domElement.getPreviousSibling();
            
            parentDomNode.removeChild( domElement );
             
            if( previousSibling != null && previousSibling.getNodeType() == Node.TEXT_NODE &&
                previousSibling.getNodeValue().trim().length() == 0 )
            {
                parentDomNode.removeChild( previousSibling );
            }
        }
    }
    
    public boolean isEmpty()
    {
        final NodeList nodes = getDomNode().getChildNodes();
         
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
    
    public List<XmlComment> getComments()
    {
        final ListFactory<XmlComment> result = new ListFactory<XmlComment>();
        final NodeList children = getDomNode().getChildNodes();
        
        this.commentsCache.track();
        
        for( int i = 0, count = children.getLength(); i < count; i++ )
        {
            final Node n = children.item( i );
            
            if( n.getNodeType() == Node.COMMENT_NODE )
            {
                final Comment comment = (Comment) n;
                XmlComment xmlComment = this.commentsCache.get( comment );
                
                if( xmlComment == null )
                {
                    xmlComment = new XmlComment( this, comment );
                    this.commentsCache.put( comment, xmlComment );
                }
                
                result.add( xmlComment );
            }
        }
        
        this.commentsCache.purge();
        
        return result.create();
    }
    
    public XmlComment addComment( final String commentText )
    {
        validateEdit();
        
        final Element domElement = getDomNode();
        final Document document = domElement.getOwnerDocument();
        final NodeList nodes = domElement.getChildNodes();
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
        domElement.insertBefore( comment, refChild );
        
        final XmlComment wrappedComment = new XmlComment( this, comment );
        this.commentsCache.put( comment, wrappedComment );
         
        if( domElement.getNodeType() == Node.ELEMENT_NODE && domElement.getChildNodes().getLength() == 1 )
        {
            format();
        }
        else
        {
            wrappedComment.format();
        }
         
        return wrappedComment;
    }
    
    public List<XmlMetaComment> getMetaComments()
    {
        final ListFactory<XmlMetaComment> result = new ListFactory<XmlMetaComment>();
        final NodeList children = getDomNode().getChildNodes();
        
        this.metaCommentsCache.track();
        
        for( int i = 0, count = children.getLength(); i < count; i++ )
        {
            final Node n = children.item( i );
            
            if( n.getNodeType() == Node.COMMENT_NODE && n.getNodeValue().indexOf( ':' ) != -1 )
            {
                final Comment metaComment = (Comment) n;
                XmlMetaComment xmlMetaComment = this.metaCommentsCache.get( metaComment );
                
                if( xmlMetaComment == null )
                {
                    xmlMetaComment = new XmlMetaComment( this, metaComment );
                    this.metaCommentsCache.put( metaComment, xmlMetaComment );
                }
                
                result.add( xmlMetaComment );
            }
        }
        
        this.metaCommentsCache.purge();
        
        return result.create();
    }
     
    public XmlMetaComment getMetaComment( final String name,
                                          final boolean createIfNecessary )
    {
        XmlMetaComment xmlMetaComment = null;
        
        for( XmlMetaComment x : getMetaComments() )
        {
            if( equal( x.getName(), name ) )
            {
                xmlMetaComment = x;
                break;
            }
        }
        
        if( xmlMetaComment == null && createIfNecessary )
        {
            final Comment comment = addComment( name + ":" ).getDomNode();
            xmlMetaComment = new XmlMetaComment( this, comment );
            this.metaCommentsCache.put( comment, xmlMetaComment );
        }
        
        return xmlMetaComment;
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
        validateEdit();
        
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
                namespace = getDomNode().getNamespaceURI();
            }
            
            final QName newQualifiedName 
                = new QName( namespace, pathSegment.getQualifiedName().getLocalPart(), prefix );
            
            return new XmlPath.Segment( newQualifiedName, false, false );
        }
    }
    
    private QName createQualifiedName( final String localName )
    {
        final String namespace = getDomNode().getNamespaceURI();
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
