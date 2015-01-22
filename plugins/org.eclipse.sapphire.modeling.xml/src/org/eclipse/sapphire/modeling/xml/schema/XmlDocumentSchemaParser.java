/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [344015] Insertion order lost if xsd includes another xsd (regression)
 *    Ling Hao - [337232] Certain schema causes elements to be out of order in corresponding xml files
 *    Ling Hao - [424278] XML Schema all construct is not supported
 ******************************************************************************/

package org.eclipse.sapphire.modeling.xml.schema;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Text;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Parses an XML Schema into XmlDocumentSchema representation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public final class XmlDocumentSchemaParser
{
    @Text( "Failed while parsing XML Schema located at \"{0}\"." )
    private static LocalizableText parseFailed;
    
    static
    {
        LocalizableText.init( XmlDocumentSchemaParser.class );
    }

    public static XmlDocumentSchema parse( final URL url,
                                           final Resolver resolver )
    {
        return parse( url, url.toString(), resolver );
    }
    
    public static XmlDocumentSchema parse( final URL url,
                                           final String location,
                                           final Resolver resolver )
    {
        final XmlDocumentSchema.Factory schema = new XmlDocumentSchema.Factory();
        schema.setSchemaLocation( location );
        parse( schema, url, resolver );
        return schema.create();
    }
    
    private static void parse( final XmlDocumentSchema.Factory schema,
                               final URL url,
                               final Resolver resolver )
    {
        final Element root = parseSchemaToDom( url );
        
        if( root != null )
        {
            final Map<String,String> prefixToNamespaceMap = new HashMap<String,String>();
            
            String ns = schema.getNamespace();
            
            if( ns == null )
            {
                ns = root.getAttribute( "targetNamespace" );
                
                if( ns.length() > 0 )
                {
                    schema.setNamespace( ns );
                }
            }

            final NamedNodeMap rootAttributes = root.getAttributes();
            
            for( int i = 0, n = rootAttributes.getLength(); i < n; i++ )
            {
                final Node attributeNode = rootAttributes.item( i );
                final String attributeNodeName = attributeNode.getNodeName();
                final String attributeNodeValue = ( (Attr) attributeNode ).getValue();
                
                if( attributeNodeName.equals( "xmlns" ) )
                {
                    prefixToNamespaceMap.put( "", attributeNodeValue );
                }
                else if( attributeNodeName.startsWith( "xmlns:" ) )
                {
                    prefixToNamespaceMap.put( attributeNodeName.substring( 6 ), attributeNodeValue );
                }
            }
            
            if( ! prefixToNamespaceMap.containsValue( ns ) )
            {
                prefixToNamespaceMap.put( "", ns );
            }
            
            for( Element el : elements( root ) )
            {
                final String elname = el.getLocalName();
                
                if( elname.equals( "import" ) )
                {
                    final String importedNamespace = el.getAttribute( "namespace" );
                    final String importedSchemaLocation = el.getAttribute( "schemaLocation" );
                    
                    schema.addImportedNamespace( importedNamespace, importedSchemaLocation );
                }
                else if( elname.equals( "include" ) || elname.equals( "redefine" ) )
                {
                    String includedSchemaLocation = el.getAttribute( "schemaLocation" ).trim();
                    
                    if( ! includedSchemaLocation.startsWith( "http://" ) )
                    {
                        final String location = url.toString();
                        final int lastSlash = location.lastIndexOf( '/' );
                        final String baseUrl;
                        
                        if( lastSlash == -1 )
                        {
                            baseUrl = location;
                        }
                        else
                        {
                            baseUrl = location.substring( 0, lastSlash );
                        }
                        
                        includedSchemaLocation = baseUrl + "/" + includedSchemaLocation;
                    }
                    
                    parse( schema, resolver.resolve( includedSchemaLocation ), resolver );
                    
                    if( elname.equals( "redefine" ) )
                    {
                        for( Element child : elements( el ) )
                        {
                            if( child.getLocalName().equals( "complexType" ) )
                            {
                                final String name = child.getAttribute( "name" );
                                XmlContentModel.Factory contentModel = parseContentModel( schema, prefixToNamespaceMap, child );
                                
                                if( contentModel != null )
                                {
                                    contentModel = inlineContentModelReference( schema, contentModel, name );
                                    schema.addContentModel( name, contentModel );
                                }
                            }
                        }
                    }
                }
            }
                
            for( Element el : elements( root ) )
            {
                final String elname = el.getLocalName();
                
                if( elname.equals( "complexType" ) || elname.equals( "group" ) )
                {
                    final String name = el.getAttribute( "name" );
                    final XmlContentModel.Factory contentModel = parseContentModel( schema, prefixToNamespaceMap, el );
                    
                    if( contentModel != null )
                    {
                        schema.addContentModel( name, contentModel );
                    }
                }
                else if( elname.equals( "element" ) )
                {
                    final XmlElementDefinition.Factory xmlElementDefinition = parseElement( schema, prefixToNamespaceMap, el );
                    schema.addTopLevelElement( xmlElementDefinition );
                }
            }
        }
    }
    
    private static XmlContentModel.Factory parseContentModel( final XmlDocumentSchema.Factory schema,
                                                              final Map<String,String> prefixToNamespaceMap,
                                                              final Element el )
    {
        final String elname = el.getLocalName();
        
        if( elname.equals( "complexType" ) || elname.equals( "complexContent" ) )
        {
            XmlContentModel.Factory contentModel = null;
            
            for( Element x : elements( el ) )
            {
                contentModel = parseContentModel( schema, prefixToNamespaceMap, x );
                
                if( contentModel != null )
                {
                    return contentModel;
                }
            }
            
            return null;
        }
        else if( elname.equals( "all" ) || elname.equals( "sequence" ) || elname.equals( "choice" ) )
        {
            final XmlGroupContentModel.Factory group;
            
            if( elname.equals( "all" ) )
            {
                group = new XmlAllGroup.Factory();
            }
            else if( elname.equals( "sequence" ) )
            {
                group = new XmlSequenceGroup.Factory();
            }
            else
            {
                group = new XmlChoiceGroup.Factory();
            }
            
            final String minOccursStr = el.getAttribute( "minOccurs" );
            final String maxOccursStr = el.getAttribute( "maxOccurs" );
            
            if( minOccursStr.length() > 0 )
            {
                try
                {
                    group.setMinOccur( Integer.parseInt( minOccursStr ) );
                }
                catch( NumberFormatException e ) {}
            }
            
            if( maxOccursStr.equalsIgnoreCase( "unbounded" ) )
            {
                group.setMaxOccur( -1 );
            }
            else if( maxOccursStr.length() > 0 )
            {
                try
                {
                    group.setMaxOccur( Integer.parseInt( maxOccursStr ) );
                }
                catch( NumberFormatException e ) {}
            }
            
            for( Element x : elements( el ) )
            {
                final XmlContentModel.Factory cm = parseContentModel( schema, prefixToNamespaceMap, x );
                
                if( cm != null )
                {
                    group.addNestedContent( cm );
                }
            }
            
            return group;
        }
        else if( elname.equals( "extension" ) )
        {
            final XmlSequenceGroup.Factory sequence = new XmlSequenceGroup.Factory();
            
            final String base = el.getAttribute( "base" );
            
            if( base != null )
            {
                final XmlContentModelReference.Factory ref = new XmlContentModelReference.Factory();
                ref.setContentModelName( parseQName( base, prefixToNamespaceMap ) );
                
                sequence.addNestedContent( ref );
            }
            
            for( Element x : elements( el ) )
            {
                final XmlContentModel.Factory cm = parseContentModel( schema, prefixToNamespaceMap, x );
                
                if( cm != null )
                {
                    sequence.addNestedContent( cm );
                }
            }
            
            return sequence;
        }
        else if( elname.equals( "element" ) )
        {
            return parseElement( schema, prefixToNamespaceMap, el );
        }
        else if( elname.equals( "group" ) )
        {
            if( el.getAttribute( "name" ).length() > 0 )
            {
                for( Element x : elements( el ) )
                {
                    final String xname = x.getLocalName();
                    
                    if( xname.equals( "sequence" ) || xname.equals( "choice" ) )
                    {
                        return parseContentModel( schema, prefixToNamespaceMap, x );
                    }
                }
                
                return null;
            }
            
            final XmlContentModelReference.Factory ref = new XmlContentModelReference.Factory();
            
            final String refstr = el.getAttribute( "ref" );
            ref.setContentModelName( parseQName( refstr, prefixToNamespaceMap ) );
            
            final String minOccursStr = el.getAttribute( "minOccurs" );
            final String maxOccursStr = el.getAttribute( "maxOccurs" );
            
            if( minOccursStr.length() > 0 )
            {
                try
                {
                    ref.setMinOccur( Integer.parseInt( minOccursStr ) );
                }
                catch( NumberFormatException e ) {}
            }
            
            if( maxOccursStr.equalsIgnoreCase( "unbounded" ) )
            {
                ref.setMaxOccur( -1 );
            }
            else if( maxOccursStr.length() > 0 )
            {
                try
                {
                    ref.setMaxOccur( Integer.parseInt( maxOccursStr ) );
                }
                catch( NumberFormatException e ) {}
            }
            
            return ref;
        }
        else
        {
            return null;
        }
    }
    
    private static QName getQName(final String name, final Map<String,String> prefixToNamespaceMap) {
        final int colon = name.indexOf( ':' );
        final String refNamespacePrefix;
        final String refLocalName;
        
        if( colon == -1 )
        {
            refNamespacePrefix = "";
            refLocalName = name;
        }
        else
        {
            refNamespacePrefix = name.substring( 0, colon );
            refLocalName = name.substring( colon + 1 );
        }
        
        final String refNamespace = prefixToNamespaceMap.get( refNamespacePrefix );
        return new QName( refNamespace, refLocalName );
    }
    
    private static XmlElementDefinition.Factory parseElement( final XmlDocumentSchema.Factory schema,
                                                              final Map<String,String> prefixToNamespaceMap,
                                                              final Element el )
    {
        QName qname = null;
        QName contentTypeName = null;
        boolean isReference = false;
        
        final String ref = el.getAttribute( "ref" );
        
        if( ref.length() > 0 )
        {
            qname = getQName( ref, prefixToNamespaceMap );
            isReference = true;
        }

        if( qname == null )
        {
            final String name = el.getAttribute( "name" );
            qname = new QName( schema.getNamespace(), name );
            
            final Element complexTypeElement = element( el, "complexType" );
            
            if( complexTypeElement != null )
            {
                contentTypeName = schema.createContentModelName();
                
                final XmlContentModel.Factory contentModel
                    = parseContentModel( schema, prefixToNamespaceMap, complexTypeElement );
                
                if( contentModel == null )
                {
                    schema.removeContentModel( contentTypeName.getLocalPart() );
                    contentTypeName = null;
                }
                else if( ( contentModel instanceof XmlGroupContentModel.Factory ) &&
                         ! ( (XmlGroupContentModel.Factory) contentModel ).hasNestedContent() )
                {
                    schema.removeContentModel( contentTypeName.getLocalPart() );
                    contentTypeName = null;
                }
                else
                {
                    schema.addContentModel( contentTypeName.getLocalPart(), contentModel );
                }
            }
            else
            {
                final String typeAttribute = el.getAttribute( "type" );
                
                if( typeAttribute != null )
                {
                    contentTypeName = parseQName( typeAttribute, prefixToNamespaceMap );
                }
            }
        }
        
        final XmlElementDefinition.Factory def;
        
        if( isReference )
        {
            def = new XmlElementDefinitionByReference.Factory();
        }
        else
        {
            def = new XmlElementDefinition.Factory();
            def.setContentModelName( contentTypeName );
        }
        
        def.setName( qname );
        
        final String minOccursStr = el.getAttribute( "minOccurs" );
        final String maxOccursStr = el.getAttribute( "maxOccurs" );
        
        if( minOccursStr.length() > 0 )
        {
            try
            {
                def.setMinOccur( Integer.parseInt( minOccursStr ) );
            }
            catch( NumberFormatException e ) {}
        }
        
        if( maxOccursStr.equalsIgnoreCase( "unbounded" ) )
        {
            def.setMaxOccur( -1 );
        }
        else if( maxOccursStr.length() > 0 )
        {
            try
            {
                def.setMaxOccur( Integer.parseInt( maxOccursStr ) );
            }
            catch( NumberFormatException e ) {}
        }
        
        final String isAbstract = el.getAttribute("abstract");
        if ("true".equals(isAbstract)) {
            def.setAbstract(true);
        }
        
        final String substitutionGroup = el.getAttribute("substitutionGroup");
        if (substitutionGroup != null && substitutionGroup.length() > 0) {
            def.setSubstitutionGroup(getQName(substitutionGroup, prefixToNamespaceMap));
        }
        
        return def;
    }
    
    
    private static QName parseQName( final String qname,
                                     final Map<String,String> prefixToNamespaceMap )
    {
        final int colon = qname.indexOf( ':' );
        final String refNamespacePrefix;
        final String refLocalName;
        
        if( colon == -1 )
        {
            refNamespacePrefix = "";
            refLocalName = qname;
        }
        else
        {
            refNamespacePrefix = qname.substring( 0, colon );
            refLocalName = qname.substring( colon + 1 );
        }
        
        final String refNamespace = prefixToNamespaceMap.get( refNamespacePrefix );
        return new QName( refNamespace, refLocalName );
    }
    
    private static Element parseSchemaToDom( final URL url )
    {
        final DocumentBuilder docbuilder;
        
        try
        {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            
            factory.setValidating( false );
            factory.setNamespaceAware( true );
            factory.setIgnoringComments( false );
            
            docbuilder = factory.newDocumentBuilder();
            
            docbuilder.setEntityResolver
            (
                new EntityResolver()
                {
                    public InputSource resolveEntity( final String publicID, 
                                                      final String systemID )
                    {
                        return new InputSource( new StringReader( "" ) );
                    }
                }
            );
        }
        catch( ParserConfigurationException e )
        {
            throw new RuntimeException( e );
        }

        try
        {
            InputStream in = null;
            
            try
            {
                in = url.openStream();

                final Document doc = docbuilder.parse( in );
                
                if( doc != null )
                {
                    return doc.getDocumentElement();
                }
            }
            finally
            {
                if( in != null )
                {
                    try
                    {
                        in.close();
                    }
                    catch( IOException e ) {}
                }
            }
        }
        catch( FileNotFoundException e )
        {
            return null;
        }
        catch( Exception e )
        {
            final String message = parseFailed.format( url.toString() );
            Sapphire.service( LoggingService.class ).logError( message, e );
        }
        
        return null;
    }
    
    private static Element element( final Element el,
                                    final String name )
    {
        final NodeList nodes = el.getChildNodes();
        
        for( int i = 0, n = nodes.getLength(); i < n; i++ )
        {
            final Node node = nodes.item( i );
            
            if( node.getNodeType() == Node.ELEMENT_NODE &&
                basename( node.getNodeName() ).equals( name ) )
            {
                return (Element) node;
            }
        }
        
        return null;
    }
    
    private static Iterable<Element> elements( final Element el )
    {
        return new ElementsIterator( el.getChildNodes() );
    }

    private static String basename( final String name )
    {
        final int colon = name.indexOf( ':' );
        
        if( colon != -1 )
        {
            return name.substring( colon + 1 );
        }
        else
        {
            return name;
        }
    }
    
    private static XmlContentModel.Factory inlineContentModelReference( final XmlDocumentSchema.Factory schema,
                                                                        final XmlContentModel.Factory contentModel,
                                                                        final String contentModelName )
    {
        if( contentModel instanceof XmlContentModelReference.Factory )
        {
            final XmlContentModelReference.Factory ref = (XmlContentModelReference.Factory) contentModel;
            final QName name = ref.getContentModelName();
            
            if( name.getNamespaceURI().equals( schema.getNamespace() ) && name.getLocalPart().equals( contentModelName ) )
            {
                return schema.getContentModel( contentModelName );
            }
        }
        else if( contentModel instanceof XmlGroupContentModel.Factory )
        {
            final XmlGroupContentModel.Factory groupContentModel = (XmlGroupContentModel.Factory) contentModel;
            
            final XmlGroupContentModel.Factory newContentModel
                = ( groupContentModel instanceof XmlSequenceGroup.Factory ? new XmlSequenceGroup.Factory() : new XmlChoiceGroup.Factory() );
            
            newContentModel.setMinOccur( contentModel.getMinOccur() );
            newContentModel.setMaxOccur( contentModel.getMaxOccur() );
            
            boolean changed = false;
            
            for( XmlContentModel.Factory in : ( (XmlGroupContentModel.Factory) contentModel ).getNestedContent() )
            {
                final XmlContentModel.Factory out = inlineContentModelReference( schema, in, contentModelName );
                newContentModel.addNestedContent( out );
                
                if( in != out )
                {
                    changed = true;
                }
            }
            
            if( changed )
            {
                return newContentModel;
            }
        }

        return contentModel;
    }
    
    public static abstract class Resolver
    {
        public abstract URL resolve( String location );
    }

    private static final class ElementsIterator
    
        implements Iterator<Element>, Iterable<Element>
        
    {
        private final NodeList nodes;
        private final int length;
        private final String name;
        private int position;
        private Element element;
        
        public ElementsIterator( final NodeList nodes )
        {
            this( nodes, null );
        }
        
        public ElementsIterator( final NodeList nodes,
                                 final String name )
        {
            this.nodes = nodes;
            this.length = nodes.getLength();
            this.position = -1;
            this.name = name;
            
            advance();
        }
        
        private void advance()
        {
            this.element = null;
            this.position++;
            
            for( ; this.position < this.length && this.element == null; 
                 this.position++ )
            {
                final Node node = this.nodes.item( this.position );
                
                if( node.getNodeType() == Node.ELEMENT_NODE &&
                    ( this.name == null || 
                      basename( node.getNodeName() ).equals( this.name ) ) )
                {
                    this.element = (Element) node;
                }
            }
        }

        public boolean hasNext() 
        {
            return ( this.element != null );
        }

        public Element next() 
        {
            final Element el = this.element;

            if( el == null ) 
            {
                throw new NoSuchElementException();
            }
            
            advance();
            
            return el;
        }

        public void remove() 
        {
            throw new UnsupportedOperationException();
        }

        public Iterator<Element> iterator() 
        {
            return this;
        }
    }

}
