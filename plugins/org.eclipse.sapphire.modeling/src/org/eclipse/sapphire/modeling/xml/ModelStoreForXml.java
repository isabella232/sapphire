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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.sapphire.modeling.ByteArrayModelStore;
import org.eclipse.sapphire.modeling.EclipseFileModelStore;
import org.eclipse.sapphire.modeling.FileModelStore;
import org.eclipse.sapphire.modeling.IEclipseFileModelStore;
import org.eclipse.sapphire.modeling.IFileModelStore;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelStore;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class ModelStoreForXml

    extends ModelStore
    implements IEclipseFileModelStore
    
{
    private static final String UTF8_ENCODING = "UTF-8"; //$NON-NLS-1$
    
    private final ByteArrayModelStore baseModelStore;
    protected Document document;
    
    public ModelStoreForXml( final ByteArrayModelStore baseModelStore )
    {
        this.baseModelStore = baseModelStore;
        this.document = null;
    }
    
    public ModelStoreForXml( final byte[] contents )
    {
        this( new ByteArrayModelStore( contents ) );
    }
    
    public ModelStoreForXml( final InputStream contents )
    
        throws IOException
        
    {
        this( new ByteArrayModelStore( contents ) );
    }
    
    public ModelStoreForXml( final File file )
    {
        this( createFileModelStore( file ) );
    }
    
    public ModelStoreForXml( final IFile file )
    {
        this( new EclipseFileModelStore( file ) );
    }
    
    public IFile getEclipseFile()
    {
        if( this.baseModelStore instanceof IEclipseFileModelStore )
        {
            return ( (IEclipseFileModelStore) this.baseModelStore ).getEclipseFile();
        }
        
        return null;
    }

    public File getFile()
    {
        if( this.baseModelStore instanceof IFileModelStore )
        {
            return ( (IFileModelStore) this.baseModelStore ).getFile();
        }
        
        return null;
    }
    
    public Document getDocument()
    {
        return this.document;
    }
    
    public boolean isXmlDeclarationNeeded()
    {
        return false;
    }
    
    public void registerRootModelElement( final IModelElement rootModelElement )
    {
        // The default implementation doesn't do anything on this call.
    }
    
    public void registerModelElement( final Node xmlNode,
                                      final IModelElement modelElement )
    {
        // The default implementation doesn't do anything on this call.
    }
    
    @Override
    public void open() throws IOException
    {
        try
        {
            this.baseModelStore.open();
            
            byte[] contents = this.baseModelStore.getContents();
            
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

    @Override
    public void save()
    
        throws IOException
        
    {
        if( validateEdit() )
        {
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
	                
	                XmlDocument.addXmlProcessingInstruction( this.document );
	                
	                final StringWriter sw = new StringWriter();
	                
	                final DOMSource source = new DOMSource( this.document );
	                final StreamResult result = new StreamResult( sw );
	                
	                final TransformerFactory factory = TransformerFactory.newInstance();
	                final Transformer transformer = factory.newTransformer();
	                transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
	                
	                transformer.transform( source, result );
	                
	                this.baseModelStore.setContents( sw.toString().getBytes( UTF8_ENCODING ) );
            	}
            	else
            	{
            		this.baseModelStore.setContents( new byte[ 0 ] );
            	}
            	
                this.baseModelStore.save();
            }
            catch( Exception e )
            {
                final IOException ioe = new IOException();
                ioe.initCause( e );
                
                throw ioe;
            }
        }
    }
    
    @Override
    public boolean validateEdit()
    {
        return this.baseModelStore.validateEdit();
    }
    
    @Override
    public boolean isOutOfDate()
    {
        return this.baseModelStore.isOutOfDate();
    }
    
    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof ModelStoreForXml )
        {
            return this.baseModelStore.equals( ( (ModelStoreForXml) obj ).baseModelStore );
        }
        
        return false;
    }

    @Override
    public int hashCode()
    {
        return this.baseModelStore.hashCode();
    }

    @Override
    public String getLocalizedText( final String text,
                                    final Locale locale )
    {
        return this.baseModelStore.getLocalizedText( text, locale );
    }

    private static IFile getWorkspaceFile( final File f )
    {
        final IWorkspace ws = ResourcesPlugin.getWorkspace();
        final IWorkspaceRoot wsroot = ws.getRoot();
        
        final IFile[] wsFiles = wsroot.findFilesForLocationURI( f.toURI() );
        
        if( wsFiles.length > 0 )
        {
            return wsFiles[ 0 ];
        }
        
        return null;
    }
    
    private static ByteArrayModelStore createFileModelStore( final File file )
    {
        final IFile ifile = getWorkspaceFile( file );
        
        if( ifile != null )
        {
            return new EclipseFileModelStore( ifile );
        }
        else
        {
            return new FileModelStore( file );
        }
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
                        return new InputSource( new StringReader( "" ) ); //$NON-NLS-1$
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
    
}
