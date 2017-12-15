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

import static org.eclipse.sapphire.modeling.xml.XmlUtil.PI_XML_DATA;
import static org.eclipse.sapphire.modeling.xml.XmlUtil.PI_XML_TARGET;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.FileResourceStore;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ResourceStore;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class XmlResourceStore

    extends ResourceStore
    
{
    private static final String UTF8_ENCODING = "UTF-8";
    
    private final ByteArrayResourceStore base;
    private Document document;
    
    public XmlResourceStore( final ByteArrayResourceStore baseModelStore )
    {
        this.base = baseModelStore;
        this.document = null;
        
        if( this.base != null )
        {
            try
            {
                byte[] contents = this.base.getContents();
                
                if ( contents.length > 0 ) 
                {
                    final InputStream in = new ByteArrayInputStream( contents );
    
                    try
                    {
                        final Reader reader = new InputStreamReader( in, UTF8_ENCODING );
                        this.document = doc( reader );
                    }
                    finally
                    {
                        try
                        {
                            in.close();
                        }
                        catch( IOException e ) {}
                    }
                }
            }
            catch( Exception e )
            {
                // Do nothing.
            }
            
            if( this.document == null )
            {
                this.document = doc();
            }
            
            this.document.setStrictErrorChecking( false );
        }
    }
    
    public XmlResourceStore( final byte[] contents )
    {
        this( new ByteArrayResourceStore( contents ) );
    }
    
    public XmlResourceStore( final InputStream contents )
    
        throws ResourceStoreException
        
    {
        this( new ByteArrayResourceStore( contents ) );
    }
    
    public XmlResourceStore( final File file )
    
        throws ResourceStoreException
        
    {
        this( new FileResourceStore( file ) );
    }
    
    public XmlResourceStore()
    {
        this( new byte[ 0 ] );
    }
    
    public Document getDomDocument()
    {
        return this.document;
    }
    
    protected void setDomDocument( final Document document )
    {
        this.document = document;
    }
    
    public boolean isXmlDeclarationNeeded()
    {
        return false;
    }
    
    @Override
    public <A> A adapt( final Class<A> adapterType )
    {
        A adapter = null;
        
        if( this.base != null )
        {
            adapter = this.base.adapt( adapterType );
        }
        
        return adapter;
    }
    
    @Override
    public void save()
    
        throws ResourceStoreException
        
    {
        validateSave();
        
        try
        {
            if( this.document.getDocumentElement() != null )
            {
                final NodeList nodes = this.document.getChildNodes();
                
                for( int i = 0, n = nodes.getLength(); i < n; i++ )
                {
                    final Node node = nodes.item( i );
                    
                    if( node != null && node.getNodeType() == Node.TEXT_NODE && 
                        node.getNodeValue().trim().length() == 0 )
                    {
                        this.document.removeChild( node );
                    }
                    else
                    {
                        break;
                    }
                }
                
                addXmlProcessingInstruction( this.document );
                
                final StringWriter sw = new StringWriter();
                
                final DOMSource source = new DOMSource( this.document );
                final StreamResult result = new StreamResult( sw );
                
                final TransformerFactory factory = TransformerFactory.newInstance();
                final Transformer transformer = factory.newTransformer();
                transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
                
                final DocumentType doctype = this.document.getDoctype();
                if (doctype != null) 
                {
                	if (doctype.getPublicId() != null) 
                	{
                		transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
                	}
                	if (doctype.getSystemId() != null) 
                	{
                		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
                	}
                }
                
                transformer.transform( source, result );
                
                this.base.setContents( sw.toString().getBytes( UTF8_ENCODING ) );
            }
            else
            {
                this.base.setContents( new byte[ 0 ] );
            }
            
            this.base.save();
        }
        catch( Exception e )
        {
            throw new ResourceStoreException( e );
        }
    }
    
    @Override
    public void validateEdit()
    {
        this.base.validateEdit();
    }
    
    @Override
    public void validateSave()
    {
        this.base.validateSave();
    }

    @Override
    public boolean isOutOfDate()
    {
        return this.base.isOutOfDate();
    }
    
    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof XmlResourceStore )
        {
            return this.base.equals( ( (XmlResourceStore) obj ).base );
        }
        
        return false;
    }

    @Override
    public int hashCode()
    {
        return this.base.hashCode();
    }
    
    @Override
    protected LocalizationService initLocalizationService( final Locale locale )
    {
        return this.base.getLocalizationService( locale );
    }

    public void registerRootModelElement( final IModelElement rootModelElement )
    {
        // The default implementation doesn't do anything.
    }

    public void registerModelElement( final Node xmlNode,
                                      final IModelElement modelElement )
    {
        // The default implementation doesn't do anything.
    }

    private static Document doc( final Reader r )
    {
        final DocumentBuilder docbuilder = docbuilder();
        
        try
        {
            return docbuilder.parse( new InputSource( r ) );
        }
        catch( IOException e )
        {
            throw new RuntimeException( e );
        }
        catch( SAXParseException e )
        {
            throw new RuntimeException( e );
        }
        catch( SAXException e )
        {
            throw new RuntimeException( e );
        }
    }

    private static Document doc()
    {
        return docbuilder().newDocument();
    }
    
    private static DocumentBuilder docbuilder()
    {
        try
        {
            final DocumentBuilderFactory factory 
                = DocumentBuilderFactory.newInstance();
            
            factory.setValidating( false );
            factory.setNamespaceAware( true );
            factory.setIgnoringComments( false );
            
            final DocumentBuilder builder = factory.newDocumentBuilder();
            
            builder.setEntityResolver
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
            
            return builder;
        }
        catch( ParserConfigurationException e )
        {
            throw new RuntimeException( e );
        }
    }
    
    private static void addXmlProcessingInstruction( final Document document )
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
    
}
