/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.modeling.xml;

import static org.eclipse.sapphire.modeling.util.MiscUtil.equal;
import static org.eclipse.sapphire.modeling.util.MiscUtil.normalizeToNull;

import java.util.Collections;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.sapphire.modeling.xml.schema.XmlDocumentSchema;
import org.eclipse.sapphire.modeling.xml.schema.XmlDocumentSchemasCache;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class StandardRootElementController extends RootElementController
{
    private final RootElementInfo info;
    
    public StandardRootElementController()
    {
        this.info = null;
    }
    
    public StandardRootElementController( final String elementName )
    {
        this( null, null, elementName, Collections.<String,String>emptyMap() );
    }
    
    public StandardRootElementController( final QName elementName )
    {
        this( elementName.getNamespaceURI(), elementName.getPrefix(), elementName.getLocalPart(), Collections.<String,String>emptyMap() );
    }
    
    public StandardRootElementController( final String namespace,
                                          final String defaultPrefix,
                                          final String elementName,
                                          final String schemaLocation )
    {
        final Map<String,String> schemas;
        
        if( schemaLocation == null || schemaLocation.length() == 0 )
        {
            schemas = Collections.emptyMap();
        }
        else
        {
            schemas = Collections.singletonMap( namespace, schemaLocation );
        }
        
        this.info = new RootElementInfo( namespace, defaultPrefix, elementName, schemas );
    }

    public StandardRootElementController( final String namespace,
                                          final String defaultPrefix,
                                          final String elementName,
                                          final Map<String,String> schemas )
    {
        this.info = new RootElementInfo( namespace, defaultPrefix, elementName, schemas );
    }

    protected RootElementInfo getRootElementInfo()
    {
        return this.info;
    }
    
    @Override
    public void createRootElement()
    {
        createRootElement( resource().adapt( RootXmlResource.class ).getDomDocument(), getRootElementInfo() );
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
    
            final StringBuilder buf = new StringBuilder();
            
            for( String schemaLocation : rinfo.schemas.values() )
            {
                final XmlDocumentSchema xmlDocumentSchema = XmlDocumentSchemasCache.getSchema( schemaLocation );
                
                if( xmlDocumentSchema != null )
                {
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
                }
            }
            
            if( buf.length() > 0 )
            {
                root.setAttributeNS( XSI_NAMESPACE, XSI_SCHEMA_LOCATION_ATTR, buf.toString() );
            }
        }
        
        document.appendChild( root );
    }

    @Override
    public boolean checkRootElement()
    {
        return checkRootElement( resource().adapt( RootXmlResource.class ).getDomDocument(), getRootElementInfo() );
    }
    
    protected boolean checkRootElement( final Document document,
                                        final RootElementInfo rinfo )
    {
        final Element root = document.getDocumentElement();
        
        final String localName = root.getLocalName();
        final String namespace = root.getNamespaceURI();
        
        return equal( localName, rinfo.elementName ) && equal( namespace, rinfo.namespace );
    }

    protected static final class RootElementInfo
    {
        public final String namespace;
        public final String defaultPrefix;
        public final String elementName;
        public final Map<String,String> schemas;
        
        public RootElementInfo( final String namespace,
                                final String defaultPrefix,
                                final String elementName,
                                final Map<String,String> schemas )
        {
            this.namespace = normalizeToNull( namespace );
            this.defaultPrefix = normalizeToNull( defaultPrefix );
            
            if( elementName == null || elementName.length() == 0 )
            {
                throw new IllegalArgumentException();
            }
            
            this.elementName = elementName.trim();
            this.schemas = schemas;
        }
    }

}
