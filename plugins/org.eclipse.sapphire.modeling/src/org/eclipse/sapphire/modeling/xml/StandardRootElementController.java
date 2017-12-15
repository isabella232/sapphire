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

import static org.eclipse.sapphire.modeling.util.internal.MiscUtil.equal;

import java.util.Map;

import org.eclipse.sapphire.modeling.xml.schema.XmlDocumentSchema;
import org.eclipse.sapphire.modeling.xml.schema.XmlDocumentSchemasCache;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class StandardRootElementController

    extends RootElementController
    
{
    private final RootElementInfo info;
    
    public StandardRootElementController()
    {
        this.info = null;
    }
    
    public StandardRootElementController( final String namespace,
                                          final String schemaLocation,
                                          final String defaultPrefix,
                                          final String elementName )
    {
        this.info = new RootElementInfo( namespace, schemaLocation, defaultPrefix, elementName );
    }

    public StandardRootElementController( final String elementName )
    {
        this( null, null, null, elementName );
    }
    
    protected RootElementInfo getRootElementInfo()
    {
        return this.info;
    }
    
    @Override
    public void createRootElement( final Document document )
    {
        createRootElement( document, getRootElementInfo() );
    }
    
    protected void createRootElement( final Document document,
                                      final RootElementInfo rinfo )
    {
        final Element root;
        
        if( rinfo.namespace == null )
        {
            root = document.createElementNS( null, rinfo.elementName );
        }
        else
        {
            if( rinfo.defaultPrefix == null )
            {
                root = document.createElementNS( rinfo.namespace, rinfo.elementName );
                root.setAttribute( XMLNS, rinfo.namespace );
            }
            else
            {
                root = document.createElementNS( rinfo.namespace, rinfo.defaultPrefix + ":" + rinfo.elementName ); //$NON-NLS-1$
                root.setAttribute( XMLNS_COLON + rinfo.defaultPrefix, rinfo.namespace );
            }
    
            root.setAttribute( XMLNS_COLON + XSI_NAMESPACE_PREFIX, XSI_NAMESPACE );
    
            if( rinfo.schemaLocation != null )
            {
                final XmlDocumentSchema xmlDocumentSchema = XmlDocumentSchemasCache.getSchema( rinfo.schemaLocation );
                
                if( xmlDocumentSchema != null )
                {
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
                    
                    if( buf.length() > 0 )
                    {
                        root.setAttributeNS( XSI_NAMESPACE, XSI_SCHEMA_LOCATION_ATTR, buf.toString() );
                    }
                }
            }
        }
        
        document.appendChild( root );
    }

    @Override
    public boolean checkRootElement( final Document document )
    {
        return checkRootElement( document, getRootElementInfo() );
    }
    
    protected boolean checkRootElement( final Document document,
                                        final RootElementInfo rinfo )
    {
        final Element root = document.getDocumentElement();
        
        final String localName = root.getLocalName();
        final String namespace = root.getNamespaceURI();
        
        return equal( localName, rinfo.elementName ) && 
               equal( namespace, rinfo.namespace );
    }

    protected static final class RootElementInfo
    {
        public final String namespace;
        public final String schemaLocation;
        public final String defaultPrefix;
        public final String elementName;
        
        public RootElementInfo( final String namespace,
                                final String schemaLocation,
                                final String defaultPrefix,
                                final String elementName )
        {
            this.namespace = namespace;
            this.schemaLocation = schemaLocation;
            this.defaultPrefix = defaultPrefix;
            this.elementName = elementName;
        }
    }

}
