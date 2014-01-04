/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.xml.schema;

import java.io.File;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.modeling.xml.dtd.DtdParser;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolverPlugin;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@SuppressWarnings( "restriction" )

public final class XmlDocumentSchemasCache
{
    private static final XmlDocumentSchema EMPTY_SCHEMA = ( new XmlDocumentSchema.Factory() ).create();
    
    private static final Map<String,SoftReference<XmlDocumentSchema>> cache 
        = new HashMap<String,SoftReference<XmlDocumentSchema>>();

    public static XmlDocumentSchema getSchema( final String location )
    {
        return getSchema( null, null, location );
    }

    public static XmlDocumentSchema getSchema( final String referer,
                                               final String publicId,
                                               final String systemId )
    {
        if( publicId == null && systemId == null )
        {
            throw new IllegalArgumentException();
        }
        
        String location = URIResolverPlugin.createResolver().resolve( referer, publicId, systemId );
        
        if( location == null )
        {
            if( systemId != null )
            {
                location = systemId;
            }
            else if( publicId != null && ! publicId.startsWith( "-//" ) )
            {
                location = publicId;
            }
        }
        
        if( location == null )
        {
            return EMPTY_SCHEMA;
        }
        
        URL url = null;
        
        try
        {
            url = new URL( location );
        }
        catch( MalformedURLException e )
        {
            if( referer != null )
            {
                try
                {
                    url = ( new File( new File( referer ).getParentFile(), location ) ).toURI().toURL();
                    location = url.toString();
                }
                catch( MalformedURLException ex )
                {
                    // We tried. 
                }
            }
        }

        if( url == null )
        {
            return EMPTY_SCHEMA;
        }
        
        XmlDocumentSchema schema = null;
        
        synchronized( XmlDocumentSchemasCache.class )
        {
            final SoftReference<XmlDocumentSchema> ref = cache.get( location );
            
            if( ref != null )
            {
                schema = ref.get();
                
                if( schema == null )
                {
                    cache.remove( location );
                }
            }
        }
        
        if( schema == null )
        {
            try
            {
                if( location.endsWith( "dtd" ) )
                {
                    schema = DtdParser.parse( url );
                }
                else
                {
                    final XmlDocumentSchemaParser.Resolver resolver = new XmlDocumentSchemaParser.Resolver()
                    {
                        @Override
                        public URL resolve( final String location )
                        {
                            URL schemaLocationUrl = null;
                            
                            try
                            {
                                schemaLocationUrl = new URL( URIResolverPlugin.createResolver().resolve( referer, null, location ) );
                            }
                            catch( MalformedURLException e )
                            {
                                if( referer != null )
                                {
                                    try
                                    {
                                        schemaLocationUrl = ( new File( new File( referer ).getParentFile(), location ) ).toURI().toURL();
                                    }
                                    catch( MalformedURLException ex )
                                    {
                                        throw new RuntimeException( ex );
                                    }
                                }
                            }
                            
                            if( schemaLocationUrl == null )
                            {
                                throw new IllegalArgumentException();
                            }
                            
                            return schemaLocationUrl;
                        }
                    };
                    
                    schema = XmlDocumentSchemaParser.parse( url, systemId, resolver );
                }
            }
            catch( Exception e )
            {
                Sapphire.service( LoggingService.class ).log( e );
                schema = EMPTY_SCHEMA;
            }
            
            synchronized( XmlDocumentSchemasCache.class )
            {
                final SoftReference<XmlDocumentSchema> ref = new SoftReference<XmlDocumentSchema>( schema );
                cache.put( location, ref );
            }
        }
        
        return schema;
    }

}