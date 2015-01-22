/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.xml;

import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.schema.XmlDocumentSchema;
import org.eclipse.sapphire.modeling.xml.schema.XmlDocumentSchemasCache;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class XmlUtil
{
    public static final String EMPTY_STRING = "";
    
    public static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    public static final String XMLNS = "xmlns";
    public static final String XMLNS_COLON = "xmlns:";
    
    public static final String XSI_NAMESPACE_PREFIX = "xsi";
    public static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String XSI_SCHEMA_LOCATION_ATTR = "schemaLocation";
    public static final String XSI_SCHEMA_LOCATION_ATTR_QUALIFIED = XSI_NAMESPACE_PREFIX + ":" + XSI_SCHEMA_LOCATION_ATTR;
    
    public static void changeNamespace( final Document document,
                                        final String oldNamespace,
                                        final String newNamespace,
                                        final String newSchemaLocation )
    {
        final Element oldRootElement = document.getDocumentElement();
        
        if( oldRootElement != null )
        {
            final Node nodeAfterRootElement = oldRootElement.getNextSibling();
            final Node newRootElement = changeNamespace( oldRootElement, oldNamespace, newNamespace, newSchemaLocation );
            document.removeChild( oldRootElement );
            document.insertBefore( newRootElement, nodeAfterRootElement );
        }
    }
    
    private static Node changeNamespace( final Node node,
                                         final String oldNamespace,
                                         final String newNamespace,
                                         final String newSchemaLocation )
    {
        if( node instanceof Element )
        {
            String elementNameSpaceUri = node.getNamespaceURI();
            String elementQualifiedName = node.getNodeName();
            String nameSpaceDeclarationAttr = null;
            
            if( elementNameSpaceUri != null && elementNameSpaceUri.equals( oldNamespace ) )
            {
                elementNameSpaceUri = newNamespace;
                
                final String prefix = node.getPrefix();
                nameSpaceDeclarationAttr = ( prefix == null ? XMLNS : XMLNS_COLON + prefix );
            }
            
            final Document document = node.getOwnerDocument();
            final Element newElement = document.createElementNS( elementNameSpaceUri, elementQualifiedName );
            final NodeList children = node.getChildNodes();
            
            for( int i = 0, n = children.getLength(); i < n; i++ )
            {
                final Node oldChildNode = children.item( i );
                
                final Node newChildNode
                    = changeNamespace( oldChildNode, oldNamespace, newNamespace, newSchemaLocation );
                
                newElement.appendChild( newChildNode );
            }
            
            final NamedNodeMap attributes = node.getAttributes();
            
            boolean updatedNamespace = false;
            boolean updatedSchemaLocation = false;
            
            for( int i = 0, n = attributes.getLength(); i < n; i++ )
            {
                final Attr attr = (Attr) attributes.item( i );
                final String attrNameSpaceUri = attr.getNamespaceURI();
                final String attrQualifiedName = attr.getNodeName();
                String attrValue = attr.getValue();
                
                if( attrQualifiedName.equals( nameSpaceDeclarationAttr ) )
                {
                    attrValue = newNamespace;
                    updatedNamespace = true;
                }
                else if( attr.getLocalName() != null && attr.getLocalName().equals( XSI_SCHEMA_LOCATION_ATTR ) )
                {
                    attrValue = createSchemaLocationAttrValue( newSchemaLocation );
                    updatedSchemaLocation = true;
                }
                
                newElement.setAttributeNS( attrNameSpaceUri, attrQualifiedName, attrValue );
            }
            
            if( updatedNamespace && ! updatedSchemaLocation )
            {
                configSchemaLocation( newElement, newSchemaLocation );
            }
            
            return newElement;
        }
        else
        {
            return node.cloneNode( true );
        }
    }
    
    public static void configSchemaLocation( final Element element,
                                             final String primarySchemaLocation )
    {
        final String schemaLocationAttrValue = createSchemaLocationAttrValue( primarySchemaLocation );
        
        element.setAttributeNS( null, XMLNS_COLON + XSI_NAMESPACE_PREFIX, XSI_NAMESPACE );
        element.setAttributeNS( XSI_NAMESPACE, XSI_SCHEMA_LOCATION_ATTR_QUALIFIED, schemaLocationAttrValue );
    }

    private static String createSchemaLocationAttrValue( final String primarySchemaLocation )
    {
        final XmlDocumentSchema xmlDocumentSchema = XmlDocumentSchemasCache.getSchema( primarySchemaLocation );
        final StringBuilder buf = new StringBuilder();
        
        for( Map.Entry<String,String> entry : xmlDocumentSchema.getSchemaLocations().entrySet() )
        {
            if( buf.length() > 0 )
            {
                buf.append( ' ' );
            }
            
            buf.append( entry.getKey() );
            buf.append( ' ' );
            buf.append( entry.getValue() );
        }
        
        return buf.toString();
    }
    
    /**
     * Converts the document into namespace-qualified form.
     * 
     * @param document the document to convert
     * @param namespace the namespace to use
     */
    
    public static void convertToNamespaceForm( final Document document,
                                               final String namespace )
    {
        convertToNamespaceForm( document, namespace, null );
    }
    
    /**
     * Converts the document into namespace-qualified form.
     * 
     * @param document the document to convert
     * @param namespace the namespace to use
     * @param schemaLocation the schema location to use
     */
    
    public static void convertToNamespaceForm( final Document document,
                                               final String namespace,
                                               final String schemaLocation )
    {
        final Element oldRootElement = document.getDocumentElement();
        
        if( oldRootElement != null )
        {
            final Node nodeAfterRootElement = oldRootElement.getNextSibling();
            final Node newRootElement = convertToNamespaceForm( oldRootElement, namespace, schemaLocation );
            document.removeChild( oldRootElement );
            document.insertBefore( newRootElement, nodeAfterRootElement );
        }
    }
    
    private static Node convertToNamespaceForm( final Node node,
                                                final String namespace,
                                                final String schemaLocation )
    {
        if( node instanceof Element )
        {
            final Document document = node.getOwnerDocument();
            final Element newElement = document.createElementNS( namespace, node.getLocalName() );
            final NodeList children = node.getChildNodes();
            
            if( node.getParentNode() instanceof Document )
            {
                newElement.setAttributeNS( null, XMLNS, namespace );
                
                if( schemaLocation != null && ! schemaLocation.equals( namespace ) )
                {
                    configSchemaLocation( newElement, schemaLocation );
                }
            }
            
            for( int i = 0, n = children.getLength(); i < n; i++ )
            {
                final Node oldChildNode = children.item( i );
                final Node newChildNode = convertToNamespaceForm( oldChildNode, namespace, schemaLocation );
                
                newElement.appendChild( newChildNode );
            }
            
            final NamedNodeMap attributes = node.getAttributes();
            
            for( int i = 0, n = attributes.getLength(); i < n; i++ )
            {
                final Attr attr = (Attr) attributes.item( i );
                newElement.setAttributeNS( null, attr.getNodeName(), attr.getValue() );
            }
            
            return newElement;
        }
        else
        {
            return node.cloneNode( true );
        }
    }
    
    /**
     * Converts the document from namespace-qualified form into unqualified form by removing all 
     * namespace information.
     * 
     * @param document the document to convert
     */
    
    public static void convertFromNamespaceForm( final Document document )
    {
        final Element oldRootElement = document.getDocumentElement();
        
        if( oldRootElement != null )
        {
            final Node nodeAfterRootElement = oldRootElement.getNextSibling();
            final Node newRootElement = convertFromNamespaceForm( oldRootElement );
            document.removeChild( oldRootElement );
            document.insertBefore( newRootElement, nodeAfterRootElement );
        }
    }
    
    private static Node convertFromNamespaceForm( final Node node )
    {
        if( node instanceof Element )
        {
            final Document document = node.getOwnerDocument();
            final Element newElement = document.createElementNS( null, node.getLocalName() );
            final NodeList children = node.getChildNodes();
            
            for( int i = 0, n = children.getLength(); i < n; i++ )
            {
                final Node oldChildNode = children.item( i );
                final Node newChildNode = convertFromNamespaceForm( oldChildNode );
                
                newElement.appendChild( newChildNode );
            }
            
            final NamedNodeMap attributes = node.getAttributes();
            
            for( int i = 0, n = attributes.getLength(); i < n; i++ )
            {
                final Attr attr = (Attr) attributes.item( i );
                final String attrQualifiedName = attr.getNodeName();
                final String attrLocalName = attr.getLocalName();
                
                if( ! attrQualifiedName.equals( XMLNS ) && ! attrQualifiedName.startsWith( XMLNS_COLON ) && ! attrLocalName.equals( XSI_SCHEMA_LOCATION_ATTR ) )
                {
                    newElement.setAttributeNS( null, attrLocalName, attr.getValue() );
                }
            }
            
            return newElement;
        }
        else
        {
            return node.cloneNode( true );
        }
    }
    
    public static QName createQualifiedName( final String name,
                                             final XmlNamespaceResolver xmlNamespaceResolver )
    {
        final QName qname;
        
        if( xmlNamespaceResolver == null )
        {
            qname = new QName( null, name, "" );
        }
        else
        {
            qname = xmlNamespaceResolver.createQualifiedName( name );
        }
        
        return qname;
    }
    
    public static QName createQualifiedName( final Node node )
    {
        final String namespace = node.getNamespaceURI();
        final String localName = node.getLocalName();
        return new QName( namespace, localName );
    }
    
    public static QName createDefaultElementName( final ElementType type )
    {
        QName name = null;
        
        final XmlBinding xmlBindingAnnotation = type.getAnnotation( XmlBinding.class );
        final XmlNamespaceResolver xmlNamespaceResolver = new StandardXmlNamespaceResolver( type );
        
        if( xmlBindingAnnotation != null )
        {
            final XmlPath path = new XmlPath( xmlBindingAnnotation.path(), xmlNamespaceResolver );
            
            if( path.getSegmentCount() == 1 )
            {
                final XmlPath.Segment firstSegment = path.getSegment( 0 );
                
                if( ! firstSegment.isAttribute() && ! firstSegment.isComment() )
                {
                    name = firstSegment.getQualifiedName();
                }
            }
        }
        
        if( name == null )
        {
            String xmlElementName = type.getSimpleName();
            
            if( xmlElementName.charAt( 0 ) == 'I' && xmlElementName.length() > 1 && Character.isUpperCase( xmlElementName.charAt( 1 ) ) )
            {
                xmlElementName = xmlElementName.substring( 1 );
            }

            name = createQualifiedName( xmlElementName, xmlNamespaceResolver );
        }
        
        return name;
    }
    
    public static boolean equal( final QName a,
                                 final QName b,
                                 final String defaultNamespace )
    {
        if( a.getLocalPart().equals( b.getLocalPart() ) )
        {
            String nsa = a.getNamespaceURI();
            String nsb = b.getNamespaceURI();
            
            if( nsa == null || nsa.length() == 0 )
            {
                nsa = defaultNamespace;
            }
            
            if( nsb == null || nsb.length() == 0 )
            {
                nsb = defaultNamespace;
            }
            
            return nsa.equals( nsb );
        }
        
        return false;
    }
    
    public static boolean contains( final QName[] qnames,
                                    final QName qname,
                                    final String defaultNamespace )
    {
        for( QName a : qnames )
        {
            if( equal( a, qname, defaultNamespace ) )
            {
                return true;
            }
        }
        
        return false;
    }
    
}
