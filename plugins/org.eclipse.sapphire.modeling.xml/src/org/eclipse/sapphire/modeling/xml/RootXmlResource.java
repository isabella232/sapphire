/******************************************************************************
 * Copyright (c) 2014 Oracle and Accenture
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 *    Kamesh Sampath - [355457] Improve DTD doctype specification in XML binding
 *    Kamesh Sampath - [355751] General improvement of XML root binding API
 ******************************************************************************/

package org.eclipse.sapphire.modeling.xml;

import static org.eclipse.sapphire.modeling.xml.XmlUtil.createDefaultElementName;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.modeling.CorruptedResourceException;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlRootBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlDocumentType;
import org.eclipse.sapphire.modeling.xml.annotations.XmlSchema;
import org.eclipse.sapphire.modeling.xml.annotations.XmlSchemas;
import org.eclipse.sapphire.modeling.xml.internal.DocumentTypeRootElementController;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 */

public class RootXmlResource extends XmlResource
{
    private static final String PI_XML_TARGET = "xml";
    private static final String PI_XML_DATA = "version=\"1.0\" encoding=\"UTF-8\"";

    private final XmlResourceStore store;
    private final Document document;
    private RootElementController rootElementController;
    private XmlElement rootXmlElement;

    public RootXmlResource()
    {
        this( new XmlResourceStore() );
    }
    
    public RootXmlResource( final XmlResourceStore store )
    {
        super( null );
        
        this.store = store;
        this.document = store.getDomDocument();
    }
    
    public XmlResourceStore store()
    {
        return this.store;
    }
    
    @Override
    public void init( final Element modelElement )
    {
        super.init( modelElement );

        final ElementType modelElementType = modelElement.type();
        
        final CustomXmlRootBinding customXmlRootBindingAnnotation = modelElementType.getAnnotation( CustomXmlRootBinding.class );
        
        if( customXmlRootBindingAnnotation != null )
        {
            try
            {
                this.rootElementController = customXmlRootBindingAnnotation.value().newInstance();
            }
            catch( Exception e )
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
        }
        
        if( this.rootElementController == null )
        { 
            final XmlBinding xmlBindingAnnotation = modelElementType.getAnnotation( XmlBinding.class );
    
            if( xmlBindingAnnotation != null && xmlBindingAnnotation.path().length() != 0 )
            {
                final XmlPath path = new XmlPath( xmlBindingAnnotation.path(), getXmlNamespaceResolver() );
                final QName qualifiedName = path.getSegment( 0 ).getQualifiedName();
                final String localName = qualifiedName.getLocalPart();
                final String prefix = qualifiedName.getPrefix();
                final String namespace = qualifiedName.getNamespaceURI();
                
                final XmlDocumentType xmlDocumentTypeAnnotation = modelElementType.getAnnotation( XmlDocumentType.class );
               
                if( xmlDocumentTypeAnnotation != null && xmlDocumentTypeAnnotation.systemId().length() != 0 )
                {
                    this.rootElementController = new DocumentTypeRootElementController( localName );
                }
                else
                {
                    final Map<String,String> schemas = new HashMap<String,String>();
                    final XmlSchemas xmlSchemasAnnotation = modelElementType.getAnnotation( XmlSchemas.class );
                    
                    if( xmlSchemasAnnotation != null )
                    {
                        for( XmlSchema xmlSchemaAnnotation : xmlSchemasAnnotation.value() )
                        {
                            final String xmlSchemaNamespace = xmlSchemaAnnotation.namespace().trim();
                            final String xmlSchemaLocation = xmlSchemaAnnotation.location().trim();
                            
                            if( xmlSchemaNamespace.length() != 0 && xmlSchemaLocation.length() != 0 )
                            {
                                schemas.put( xmlSchemaNamespace, xmlSchemaLocation );
                            }
                        }
                    }
                    
                    final XmlSchema xmlSchemaAnnotation = modelElementType.getAnnotation( XmlSchema.class );
                    
                    if( xmlSchemaAnnotation != null )
                    {
                        final String xmlSchemaNamespace = xmlSchemaAnnotation.namespace().trim();
                        final String xmlSchemaLocation = xmlSchemaAnnotation.location().trim();
                        
                        if( xmlSchemaNamespace.length() != 0 && xmlSchemaLocation.length() != 0 )
                        {
                            schemas.put( xmlSchemaNamespace, xmlSchemaLocation );
                        }
                    }
                    
                    this.rootElementController = new StandardRootElementController( namespace, prefix, localName, schemas );
                }
            }
        }
        
        if( this.rootElementController == null )
        {
            this.rootElementController = new StandardRootElementController( createDefaultElementName( modelElementType ) );
        }
        
        this.rootElementController.init( this );
        
        store().registerRootModelElement( modelElement );
    }
    
    public final Document getDomDocument()
    {
        return this.document;
    }
    
    @Override
    public XmlElement getXmlElement( final boolean createIfNecessary )
    {
        org.w3c.dom.Element root = this.document.getDocumentElement();
    
        if( this.document.getChildNodes().getLength() == 0 )
        {
            if( createIfNecessary )
            {
                fixMalformedDescriptor();
                root = this.document.getDocumentElement();
            }
        }
        else
        {
            final boolean isRootValid 
                = ( root == null ? false : this.rootElementController.checkRootElement() );
            
            if( isRootValid == false )
            {
                root = null;
                
                if( createIfNecessary )
                {
                    if( validateCorruptedResourceRecovery() )
                    {
                        fixMalformedDescriptor();
                        root = this.document.getDocumentElement();
                    }
                    else
                    {
                        throw new CorruptedResourceException();
                    }
                }
            }
        }
        
        if( root == null )
        {
            this.rootXmlElement = null;
        }
        else if( this.rootXmlElement == null || root != this.rootXmlElement.getDomNode() )
        {
            this.rootXmlElement = new XmlElement( store(), root );
        }
        
        return this.rootXmlElement;
    }
    
    @Override
    public void save() throws ResourceStoreException
    {
        if( this.document.getChildNodes().getLength() == 0 )
        {
            fixMalformedDescriptor();
        }
        
        this.store.save();
    }

    @Override
    public <A> A adapt( final Class<A> adapterType )
    {
        A adapter = super.adapt( adapterType );
        
        if( adapter == null )
        {
            adapter = this.store.adapt( adapterType );
        }
        
        return adapter;
    }
    
    @Override
    public boolean isOutOfDate()
    {
        return this.store.isOutOfDate();
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        this.store.dispose();
    }

    @Override
    protected LocalizationService initLocalizationService( final Locale locale )
    {
        return this.store.getLocalizationService( locale );
    }

    private final void fixMalformedDescriptor()
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
        
        // Add a new XML declaration and the root element.
        
        if( store().isXmlDeclarationNeeded() )
        {
            final ProcessingInstruction xmlDeclarationNode = this.document.createProcessingInstruction( PI_XML_TARGET, PI_XML_DATA );
            this.document.insertBefore( xmlDeclarationNode, null );
            
            final Text newLineTextNode = this.document.createTextNode( "\n" );
            this.document.insertBefore( newLineTextNode, null );
        }
        
        this.rootElementController.createRootElement();
    }
    
}
