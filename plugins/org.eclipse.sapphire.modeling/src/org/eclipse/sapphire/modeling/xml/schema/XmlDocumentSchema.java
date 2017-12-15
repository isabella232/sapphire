/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [338605] The include directive not handled in XML Schema parsing (regression)
 ******************************************************************************/

package org.eclipse.sapphire.modeling.xml.schema;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolver;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolverPlugin;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class XmlDocumentSchema
{
    private String namespace;
    private String schemaLocation;
    private final Map<String,String> importedNamespaces;
    private final Map<String,XmlContentModel> contentModels;
    private final Map<String,XmlElementDefinition> topLevelElements;
    
    public XmlDocumentSchema( final String schemaLocation, 
                              final String baseLocation )
    {
        this.schemaLocation = schemaLocation;
        this.importedNamespaces = new HashMap<String,String>();
        this.contentModels = new HashMap<String,XmlContentModel>();
        this.topLevelElements = new HashMap<String,XmlElementDefinition>();
        
        parseSchema( schemaLocation, baseLocation );
    }
    
    public String getNamespace()
    {
        return this.namespace;
    }
    
    public String getSchemaLocation()
    {
        return this.schemaLocation;
    }
    
    public String getSchemaLocation( final String namespace )
    {
        if( namespace.equals( this.namespace ) )
        {
            return this.schemaLocation;
        }
        else
        {
            return this.importedNamespaces.get( namespace );
        }
    }
    
    public Map<String,String> getSchemaLocations()
    {
        final Map<String,String> schemaLocations = new HashMap<String,String>();
        
        if( this.namespace != null && this.schemaLocation != null )
        {
            schemaLocations.put( this.namespace, this.schemaLocation );
        }
        
        schemaLocations.putAll( this.importedNamespaces );
        
        return Collections.unmodifiableMap( schemaLocations );
    }
    
    public XmlElementDefinition getElement( final String name )
    {
        return this.topLevelElements.get( name );
    }
    
    public XmlContentModel getContentModel( final String name )
    {
        return this.contentModels.get( name );
    }
    
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        
        for( XmlElementDefinition xmlElementSchema : this.topLevelElements.values() )
        {
            xmlElementSchema.toString( buf, "" );
            buf.append( "\n\n" );
        }
        
        for( Map.Entry<String,XmlContentModel> entry : this.contentModels.entrySet() )
        {
            buf.append( entry.getKey() );
            buf.append( " = " );
            entry.getValue().toString( buf, "" );
            buf.append( "\n\n" );
        }

        return buf.toString();
    }
    
    private QName createContentModelName()
    {
        int counter = 1;
        String contentModelName = null;
        
        do
        {
            contentModelName = "##@" + String.valueOf( counter );
            counter++;
        }
        while( this.contentModels.containsKey( contentModelName ) );
        
        return new QName( this.namespace, contentModelName );
    }

    private void parseSchema( final String schemaLocation, 
                              final String baseLocation )
    {
        final URIResolver idResolver = URIResolverPlugin.createResolver();

        String resolvedSchemaLocation = null;
        
        try
        {
            resolvedSchemaLocation = idResolver.resolve(baseLocation, null, schemaLocation);
        }
        catch( Exception e )
        {
            SapphireModelingFrameworkPlugin.log( e );
        }
         
        if( resolvedSchemaLocation == null )
        {
            resolvedSchemaLocation = schemaLocation;
        }
        
        if (resolvedSchemaLocation.endsWith("dtd"))
        {
            this.namespace = "";
            DTDParser.parse(resolvedSchemaLocation, this, this.contentModels, this.topLevelElements);
            return;
        }
        
        final Element root = parse( resolvedSchemaLocation, baseLocation );
        
        if( root != null )
        {
            final String ns = root.getAttribute( "targetNamespace" );
            
            if( ns.length() > 0 )
            {
                this.namespace = ns;
            }
        }
        else
        {
            return;
        }

        final Map<String,String> prefixToNamespaceMap = new HashMap<String,String>();
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
        
        if( ! prefixToNamespaceMap.containsValue( this.namespace ) )
        {
            prefixToNamespaceMap.put( "", this.namespace );
        }
        
        for( Element el : elements( root ) )
        {
            final String elname = el.getLocalName();
            
            if( elname.equals( "import" ) )
            {
                final String importedNamespace = el.getAttribute( "namespace" );
                final String importedSchemaLocation = el.getAttribute( "schemaLocation" );
                
                if( ! this.importedNamespaces.containsKey( importedNamespace ) )
                {
                    this.importedNamespaces.put( importedNamespace, importedSchemaLocation );
                }
            }
            else if( elname.equals( "include" ) || elname.equals( "redefine" ) )
            {
                String includedSchemaLocation = el.getAttribute( "schemaLocation" ).trim();
                
                if( ! includedSchemaLocation.startsWith( "http://" ) )
                {
                    final int lastSlash = this.schemaLocation.lastIndexOf( '/' );
                    final String baseUrl;
                    
                    if( lastSlash == -1 )
                    {
                        baseUrl = this.schemaLocation;
                    }
                    else
                    {
                        baseUrl = this.schemaLocation.substring( 0, lastSlash );
                    }
                    
                    includedSchemaLocation = baseUrl + "/" + includedSchemaLocation;
                }
                
                parseSchema( includedSchemaLocation, baseLocation );
                
                if( elname.equals( "redefine" ) )
                {
                    for( Element child : elements( el ) )
                    {
                        if( child.getLocalName().equals( "complexType" ) )
                        {
                            final String name = child.getAttribute( "name" );
                            XmlContentModel contentModel = parseContentModel( prefixToNamespaceMap, child );
                            
                            if( contentModel != null )
                            {
                                contentModel = optimize( inlineContentModelReference( contentModel, name ) );
                                this.contentModels.put( name, contentModel );
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
                final XmlContentModel contentModel = parseContentModel( prefixToNamespaceMap, el );
                
                if( contentModel != null )
                {
                    this.contentModels.put( name, optimize( contentModel ) );
                }
            }
            else if( elname.equals( "element" ) )
            {
                final XmlElementDefinition xmlElementDefinition = parseElement( prefixToNamespaceMap, el );
                this.topLevelElements.put( xmlElementDefinition.getName().getLocalPart(), xmlElementDefinition );
            }
        }
    }
    
    private XmlContentModel parseContentModel( final Map<String,String> prefixToNamespaceMap,
                                               final Element el )
    {
        final String elname = el.getLocalName();
        
        if( elname.equals( "complexType" ) || 
            elname.equals( "complexContent" ) )
        {
            XmlContentModel contentModel = null;
            
            for( Element x : elements( el ) )
            {
                contentModel = parseContentModel( prefixToNamespaceMap, x );
                
                if( contentModel != null )
                {
                    return contentModel;
                }
            }
            
            return null;
        }
        else if( elname.equals( "sequence" ) ||
                 elname.equals( "choice" ) )
        {
            final String minOccursStr = el.getAttribute( "minOccurs" );
            final String maxOccursStr = el.getAttribute( "maxOccurs" );
            
            int minOccurs = 1;
            int maxOccurs = 1;
            
            if( minOccursStr.length() > 0 )
            {
                try
                {
                    minOccurs = Integer.parseInt( minOccursStr );
                }
                catch( NumberFormatException e ) {}
            }
            
            if( maxOccursStr.equalsIgnoreCase( "unbounded" ) )
            {
                maxOccurs = -1;
            }
            else if( maxOccursStr.length() > 0 )
            {
                try
                {
                    maxOccurs = Integer.parseInt( maxOccursStr );
                }
                catch( NumberFormatException e ) {}
            }
            
            final List<XmlContentModel> nestedContentModels = new ArrayList<XmlContentModel>();
            
            for( Element x : elements( el ) )
            {
                final XmlContentModel cm = parseContentModel( prefixToNamespaceMap, x );
                
                if( cm != null )
                {
                    nestedContentModels.add( cm );
                }
            }
            
            if( elname.equals( "sequence" ) )
            {
                return new XmlSequenceGroup( this, minOccurs, maxOccurs, nestedContentModels );
            }
            else
            {
                return new XmlChoiceGroup( this, minOccurs, maxOccurs, nestedContentModels );
            }
        }
        else if( elname.equals( "extension" ) )
        {
            final List<XmlContentModel> nestedContentModels = new ArrayList<XmlContentModel>();
            
            final String base = el.getAttribute( "base" );
            
            if( base != null )
            {
                final QName qname = parseQName( base, prefixToNamespaceMap );
                nestedContentModels.add( new XmlContentModelReference( this, qname, 1, 1 ) );
            }
            
            for( Element x : elements( el ) )
            {
                final XmlContentModel cm = parseContentModel( prefixToNamespaceMap, x );
                
                if( cm != null )
                {
                    nestedContentModels.add( cm );
                }
            }
            
            return new XmlSequenceGroup( this, 1, 1, nestedContentModels );
        }
        else if( elname.equals( "element" ) )
        {
            return parseElement( prefixToNamespaceMap, el );
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
                        return parseContentModel( prefixToNamespaceMap, x );
                    }
                }
                
                return null;
            }
            
            final String ref = el.getAttribute( "ref" );
            final QName qname = parseQName( ref, prefixToNamespaceMap );
            
            final String minOccursStr = el.getAttribute( "minOccurs" );
            final String maxOccursStr = el.getAttribute( "maxOccurs" );
            
            int minOccurs = 1;
            int maxOccurs = 1;
            
            if( minOccursStr.length() > 0 )
            {
                try
                {
                    minOccurs = Integer.parseInt( minOccursStr );
                }
                catch( NumberFormatException e ) {}
            }
            
            if( maxOccursStr.equalsIgnoreCase( "unbounded" ) )
            {
                maxOccurs = -1;
            }
            else if( maxOccursStr.length() > 0 )
            {
                try
                {
                    maxOccurs = Integer.parseInt( maxOccursStr );
                }
                catch( NumberFormatException e ) {}
            }

            return new XmlContentModelReference( this, qname, minOccurs, maxOccurs );
        }
        else if( elname.equals( "any" ) || 
                 elname.equals( "annotation" ) ||
                 elname.equals( "simpleContent" ) ||
                 elname.equals( "attribute" ) ||
                 elname.equals( "anyAttribute" ) )
        {
            return null;
        }
        else
        {
            throw new RuntimeException( elname );
        }
    }
    
    private XmlElementDefinition parseElement( final Map<String,String> prefixToNamespaceMap,
                                               final Element el )
    {
        QName qname = null;
        QName contentTypeName = null;
        boolean isReference = false;
        
        final String ref = el.getAttribute( "ref" );
        
        if( ref.length() > 0 )
        {
            final int colon = ref.indexOf( ':' );
            final String refNamespacePrefix;
            final String refLocalName;
            
            if( colon == -1 )
            {
                refNamespacePrefix = "";
                refLocalName = ref;
            }
            else
            {
                refNamespacePrefix = ref.substring( 0, colon );
                refLocalName = ref.substring( colon + 1 );
            }
            
            final String refNamespace = prefixToNamespaceMap.get( refNamespacePrefix );
            qname = new QName( refNamespace, refLocalName );
            isReference = true;
        }

        if( qname == null )
        {
            final String name = el.getAttribute( "name" );
            qname = new QName( this.namespace, name );
            
            final Element complexTypeElement = element( el, "complexType" );
            
            if( complexTypeElement != null )
            {
                contentTypeName = createContentModelName();
                this.contentModels.put( contentTypeName.getLocalPart(), null ); // reserve the name
                
                final XmlContentModel contentModel
                    = parseContentModel( prefixToNamespaceMap, complexTypeElement );
                
                if( contentModel == null )
                {
                    this.contentModels.remove( contentTypeName.getLocalPart() );
                    contentTypeName = null;
                }
                else if( ( contentModel instanceof XmlGroupContentModel ) &&
                         ( (XmlGroupContentModel) contentModel ).getNestedContent().isEmpty() )
                {
                    this.contentModels.remove( contentTypeName.getLocalPart() );
                    contentTypeName = null;
                }
                else
                {
                    this.contentModels.put( contentTypeName.getLocalPart(), optimize( contentModel ) );
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
        
        final String minOccursStr = el.getAttribute( "minOccurs" );
        final String maxOccursStr = el.getAttribute( "maxOccurs" );
        
        int minOccurs = 1;
        int maxOccurs = 1;
        
        if( minOccursStr.length() > 0 )
        {
            try
            {
                minOccurs = Integer.parseInt( minOccursStr );
            }
            catch( NumberFormatException e ) {}
        }
        
        if( maxOccursStr.equalsIgnoreCase( "unbounded" ) )
        {
            maxOccurs = -1;
        }
        else if( maxOccursStr.length() > 0 )
        {
            try
            {
                maxOccurs = Integer.parseInt( maxOccursStr );
            }
            catch( NumberFormatException e ) {}
        }

        if( isReference )
        {
            return new XmlElementDefinitionByReference( this, qname, minOccurs, maxOccurs );
        }
        else
        {
            return new XmlElementDefinition( this, qname, contentTypeName, minOccurs, maxOccurs );
        }
    }
    
    private static Element parse( final String schemaLocation,
                                  final String baseLocation )
    {
        final DocumentBuilder docbuilder;
        
        try
        {
            final DocumentBuilderFactory factory 
                = DocumentBuilderFactory.newInstance();
            
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
            URL schemaLocationUrl = null;
            
            try
            {
                schemaLocationUrl = new URL( schemaLocation );
            }
            catch( MalformedURLException e )
            {
                if( baseLocation != null )
                {
                    schemaLocationUrl = ( new File( new File( baseLocation ).getParentFile(), schemaLocation ) ).toURI().toURL();
                }
            }
            
            InputStream in = null;
            
            try
            {
                in = schemaLocationUrl.openStream();

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
            final String message = NLS.bind( Resources.parseFailed, schemaLocation );
            final IStatus st = SapphireModelingFrameworkPlugin.createWarningStatus( message, e );
            SapphireModelingFrameworkPlugin.log( st );
        }
        
        return null;
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
    
    private XmlContentModel inlineContentModelReference( final XmlContentModel contentModel,
                                                         final String contentModelName )
    {
        if( contentModel instanceof XmlContentModelReference )
        {
            final XmlContentModelReference ref = (XmlContentModelReference) contentModel;
            final QName name = ref.getContentModelName();
            
            if( name.getNamespaceURI().equals( this.namespace ) && name.getLocalPart().equals( contentModelName ) )
            {
                return ref.getContentModel();
            }
        }
        else if( contentModel instanceof XmlGroupContentModel )
        {
            final XmlGroupContentModel groupContentModel = (XmlGroupContentModel) contentModel;
            final List<XmlContentModel> nestedContent = new ArrayList<XmlContentModel>( groupContentModel.getNestedContent() );
            boolean changed = false;
            
            for( int i = 0, n = nestedContent.size(); i < n; i++ )
            {
                final XmlContentModel in = nestedContent.get( i );
                final XmlContentModel out = inlineContentModelReference( in, contentModelName );
                
                if( in != out )
                {
                    nestedContent.set( i, out );
                    changed = true;
                }
            }
            
            if( changed )
            {
                if( groupContentModel instanceof XmlSequenceGroup )
                {
                    return new XmlSequenceGroup( groupContentModel.getSchema(), groupContentModel.getMinOccur(), groupContentModel.getMaxOccur(), nestedContent );
                }
                else
                {
                    return new XmlChoiceGroup( groupContentModel.getSchema(), groupContentModel.getMinOccur(), groupContentModel.getMaxOccur(), nestedContent );
                }
            }
        }

        return contentModel;
    }
    
    private XmlContentModel optimize( final XmlContentModel contentModel )
    {
        if( contentModel instanceof XmlSequenceGroup )
        {
            final XmlSequenceGroup sequenceContentModel = (XmlSequenceGroup) contentModel;
            final List<XmlContentModel> nestedContent = new ArrayList<XmlContentModel>();
            boolean optimized = false;
            
            for( XmlContentModel child : sequenceContentModel.getNestedContent() )
            {
                boolean handled = false;
                
                if( child instanceof XmlSequenceGroup )
                {
                    final XmlSequenceGroup cs = (XmlSequenceGroup) child;
                    
                    if( cs.getMinOccur() == 1 && cs.getMaxOccur() == 1 )
                    {
                        for( XmlContentModel nested : cs.getNestedContent() )
                        {
                            nestedContent.add( optimize( nested ) );
                        }

                        handled = true;
                        optimized = true;
                    }
                }
                
                if( ! handled )
                {
                    final XmlContentModel optimizedChild = optimize( child );
                    
                    if( optimizedChild != child )
                    {
                        optimized = true;
                    }
                    
                    nestedContent.add( optimizedChild );
                }
            }
            
            if( optimized )
            {
                return new XmlSequenceGroup( sequenceContentModel.getSchema(), sequenceContentModel.getMinOccur(), sequenceContentModel.getMaxOccur(), nestedContent );
            }
        }
        else if( contentModel instanceof XmlChoiceGroup )
        {
            final XmlChoiceGroup choiceContentModel = (XmlChoiceGroup) contentModel;
            final List<XmlContentModel> nestedContent = new ArrayList<XmlContentModel>();
            boolean optimized = false;
            
            for( XmlContentModel child : choiceContentModel.getNestedContent() )
            {
                final XmlContentModel optimizedChild = optimize( child );
                
                if( optimizedChild != child )
                {
                    optimized = true;
                }
                
                nestedContent.add( optimizedChild );
            }
            
            if( optimized )
            {
                return new XmlChoiceGroup( choiceContentModel.getSchema(), choiceContentModel.getMinOccur(), choiceContentModel.getMaxOccur(), nestedContent );
            }
        }
        
        return contentModel;
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
    
    public static final class ElementsIterator
    
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

    private static final class Resources
    
        extends NLS
    
    {
        public static String parseFailed;
        
        static
        {
            initializeMessages( XmlDocumentSchema.class.getName(), Resources.class );
        }
    }

}
