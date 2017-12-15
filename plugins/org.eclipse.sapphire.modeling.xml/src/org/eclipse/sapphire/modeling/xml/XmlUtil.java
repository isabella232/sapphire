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

import static org.eclipse.sapphire.modeling.xml.RootElementController.XSI_NAMESPACE;
import static org.eclipse.sapphire.modeling.xml.RootElementController.XSI_NAMESPACE_PREFIX;
import static org.eclipse.sapphire.modeling.xml.RootElementController.XSI_SCHEMA_LOCATION_ATTR;

import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.sapphire.modeling.xml.schema.XmlDocumentSchema;
import org.eclipse.sapphire.modeling.xml.schema.XmlDocumentSchemasCache;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class XmlUtil
{
    public static final String EMPTY_STRING = "";
    
    public static final String PI_XML_TARGET = "xml"; //$NON-NLS-1$
    public static final String PI_XML_DATA = "version=\"1.0\" encoding=\"UTF-8\""; //$NON-NLS-1$

    private static final String XMLNS = "xmlns"; //$NON-NLS-1$
    private static final String XMLNS_COLON = "xmlns:"; //$NON-NLS-1$
    
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
                else if( attr.getLocalName() != null && attr.getLocalName().equals( "schemaLocation" ) )
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
        element.setAttributeNS( XSI_NAMESPACE, XSI_SCHEMA_LOCATION_ATTR, schemaLocationAttrValue );
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
