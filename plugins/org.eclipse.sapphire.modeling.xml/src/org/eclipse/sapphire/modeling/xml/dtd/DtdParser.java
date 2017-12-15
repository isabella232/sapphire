/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [355432] Dtd parser error
 ******************************************************************************/

package org.eclipse.sapphire.modeling.xml.dtd;

import static org.eclipse.sapphire.modeling.util.MiscUtil.readTextContent;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.modeling.xml.dtd.internal.DtdParserImpl;
import org.eclipse.sapphire.modeling.xml.schema.XmlDocumentSchema;

/**
 * Parses a DTD into XmlDocumentSchema representation.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class DtdParser
{
    public static XmlDocumentSchema parse( final URL url )
    {
        InputStream in = null;
        
        try
        {
            in = url.openStream();
            return parse( readTextContent( in ) );
        }
        catch( Exception e )
        {
            final String message = NLS.bind( Resources.parseFailed, url );
            throw new RuntimeException( message, e );
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
    
    public static XmlDocumentSchema parse( final String dtd )
    {
        // The DTD parser is invoked twice. If entities are found in the first pass, then string substitution
        // is used to resolve them in the DTD and the DTD is re-parsed.
        
        try
        {
            String content = dtd;
            DtdParserImpl parser;
            
            parser = new DtdParserImpl( new StringReader( content ) );
            parser.Start();
            
            if( ! parser.entities.isEmpty() )
            {
                String substitutedContent = substituteEntity(content, parser.entities);
                while (!content.equals(substitutedContent)) {
                    content = substitutedContent;
                    substitutedContent = substituteEntity(content, parser.entities);
                }
                
                parser = new DtdParserImpl( new StringReader( content ) );
                parser.Start();
            }
            
            return parser.schema.create();
        }
        catch( Exception e )
        {
            final String message = NLS.bind( Resources.parseFailed, "##string##" );
            throw new RuntimeException( message, e );
        }
    }

    private static String substituteEntity(String content, Map<String,String> entities) 
    {
        for( Map.Entry<String,String> entity : entities.entrySet() )
        {
            content = content.replace( "%" + entity.getKey() + ";", entity.getValue() );
        }
        return content;
    }

    @Deprecated
    
    public static XmlDocumentSchema parseFromUrl( final String referer,
                                                  final String url )
    {
        final URL dtdLocationUrl;
        
        try
        {
            dtdLocationUrl = new URL( org.eclipse.sapphire.modeling.xml.schema.UrlResolver.resolve( referer, url ) );
        }
        catch( MalformedURLException e )
        {
            final String message = NLS.bind( Resources.parseFailed, url );
            throw new RuntimeException( message, e );
        }
        
        return parse( dtdLocationUrl );
    }
    
    @Deprecated
    
    public static XmlDocumentSchema parseFromString( final String dtd )
    {
        return parse( dtd );
    }

    private static final class Resources extends NLS
    {
        public static String parseFailed;
        
        static
        {
            initializeMessages( DtdParser.class.getName(), Resources.class );
        }
    }

}
