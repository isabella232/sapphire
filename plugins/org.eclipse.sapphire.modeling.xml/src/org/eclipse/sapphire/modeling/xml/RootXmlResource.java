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

import java.util.Locale;

import org.eclipse.sapphire.modeling.CorruptedResourceException;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlRootBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlRootBinding;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
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
    public void init( final IModelElement modelElement )
    {
        super.init( modelElement );

        final ModelElementType modelElementType = modelElement.getModelElementType();
        
        final XmlRootBinding xmlRootBindingAnnotation = modelElementType.getAnnotation( XmlRootBinding.class );
        
        if( xmlRootBindingAnnotation != null )
        {
            this.rootElementController 
                = new StandardRootElementController( xmlRootBindingAnnotation.namespace(), xmlRootBindingAnnotation.schemaLocation(), 
                                                     xmlRootBindingAnnotation.defaultPrefix(), xmlRootBindingAnnotation.elementName() );
        }
        
        if( this.rootElementController == null )
        {
            final CustomXmlRootBinding customXmlRootBindingAnnotation = modelElementType.getAnnotation( CustomXmlRootBinding.class );
            
            if( customXmlRootBindingAnnotation != null )
            {
                try
                {
                    this.rootElementController = customXmlRootBindingAnnotation.value().newInstance();
                }
                catch( Exception e )
                {
                    LoggingService.log( e );
                }
            }
        }
        
        if( this.rootElementController == null )
        {
            this.rootElementController = new StandardRootElementController( modelElementType.getSimpleName().substring( 1 ) );
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
        Element root = this.document.getDocumentElement();
    
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
    
    /**
     * @throws ResourceStoreException  
     */

    @Override
    
    public void save() 
    
        throws ResourceStoreException
        
    {
        this.store.save();
    }

    @Override
    
    public <A> A adapt( final Class<A> adapterType )
    {
        A adapter = this.store.adapt( adapterType );
        
        if( adapter == null )
        {
            adapter = super.adapt( adapterType );
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
        
        // Add a new XML processing instruction and the root element.
        
        if( store().isXmlDeclarationNeeded() )
        {
            addXmlProcessingInstruction( this.document );
        }
        
        this.rootElementController.createRootElement();
    }
    
    private void addXmlProcessingInstruction( final Document document )
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
