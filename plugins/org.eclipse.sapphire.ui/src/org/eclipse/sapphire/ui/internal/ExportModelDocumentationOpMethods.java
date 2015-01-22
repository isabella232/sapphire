/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.internal;

import static org.eclipse.sapphire.util.StringUtil.UTF8;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.docsys.DocumentationContent;
import org.eclipse.sapphire.modeling.docsys.HtmlFormatter;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlElementBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlValueBinding;
import org.eclipse.sapphire.services.PossibleTypesService;
import org.eclipse.sapphire.ui.IExportModelDocumentationOp;
import org.osgi.framework.Bundle;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ExportModelDocumentationOpMethods
{
    private static String STYLE;
    
    public static String execute( final IExportModelDocumentationOp op,
                                  final ElementType type,
                                  final IProgressMonitor monitor )
    {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter out = new PrintWriter( stringWriter );
        
        if( op.getCreateFinishedDocument().content() )
        {
            out.println( "<html>" );
            out.println();
            out.println( "<head>" );
            out.println( "  <title>" + op.getDocumentTitle().content() + "</title>" );
            
            if( op.getEmbedDefaultStyle().content() )
            {
                out.println( style() );
            }
            
            out.println( "</head>" );
            out.println();
            out.println( "<body>" );
        }
        
        execute( type, out );
        
        if( op.getCreateFinishedDocument().content() )
        {
            out.println( "<br/><br/>" );
            out.println( "</body>" );
            out.println();
            out.println( "</head>" );
        }
        
        out.flush();
        
        return stringWriter.getBuffer().toString();
    }
    
    private static void execute( final ElementType type,
                                 final PrintWriter out )
    {
        // Build a sorted map of XML path to property.
        
        final TreeMap<String,PropertyDef> properties = new TreeMap<String,PropertyDef>();
        
        for( PropertyDef property : type.properties() )
        {
            String xmlPath = null;
            
            final XmlBinding xmlBindingAnnotation = property.getAnnotation( XmlBinding.class );
            
            if( xmlBindingAnnotation != null )
            {
                xmlPath = xmlBindingAnnotation.path();
            }

            if( xmlPath == null )
            {
                final XmlValueBinding xmlValueBindingAnnotation = property.getAnnotation( XmlValueBinding.class );
                
                if( xmlValueBindingAnnotation != null )
                {
                    xmlPath = xmlValueBindingAnnotation.path();
                }
            }
            
            if( xmlPath == null )
            {
                final XmlElementBinding xmlElementBindingAnnotation = property.getAnnotation( XmlElementBinding.class );
                
                if( xmlElementBindingAnnotation != null )
                {
                    final int mappingsCount = xmlElementBindingAnnotation.mappings().length;
                    
                    if( mappingsCount == 0 )
                    {
                        xmlPath = xmlElementBindingAnnotation.path();
                    }
                    else if( mappingsCount == 1 )
                    {
                        xmlPath = xmlElementBindingAnnotation.mappings()[ 0 ].element();
                        
                        if( xmlElementBindingAnnotation.path().length() > 0 )
                        {
                            xmlPath = xmlElementBindingAnnotation.path() + "/" + xmlPath;
                        }
                    }
                    else
                    {
                        continue; // todo: report unsupported
                    }
                }
            }
            
            if( xmlPath == null )
            {
                final XmlListBinding xmlListBindingAnnotation = property.getAnnotation( XmlListBinding.class );
                
                if( xmlListBindingAnnotation != null )
                {
                    if( xmlListBindingAnnotation.mappings().length == 1 )
                    {
                        xmlPath = xmlListBindingAnnotation.mappings()[ 0 ].element();
                        
                        if( xmlListBindingAnnotation.path().length() > 0 )
                        {
                            xmlPath = xmlListBindingAnnotation.path() + "/" + xmlPath;
                        }
                    }
                    else
                    {
                        continue; // todo: report unsupported
                    }
                }
            }
            
            if( xmlPath != null )
            {
                properties.put( xmlPath, property );
            }
        }
        
        // Write the summary document fragment
        
        out.println( "<table>" );
        out.println( "  <tr>" );
        out.println( "    <th>Element</th>" );
        out.println( "    <th>Cardinality</th>" );
        out.println( "    <th>Description</th>" );
        out.println( "  </tr>" );
        
        for( Map.Entry<String,PropertyDef> entry : properties.entrySet() )
        {
            final String xmlPath = entry.getKey();
            final PropertyDef property = entry.getValue();
            
            final String cardinality;
            
            if( property instanceof ValueProperty )
            {
                if( property.hasAnnotation( Required.class ) )
                {
                    cardinality = "1";
                }
                else
                {
                    cardinality = "0 or 1";
                }
            }
            else if( property instanceof ElementProperty )
            {
                cardinality = "0 or 1";
            }
            else if( property instanceof ListProperty )
            {
                cardinality = "0 or more";
            }
            else
            {
                throw new IllegalStateException();
            }
            
            out.println( "  <tr>" );

            out.println( td( xmlPath ) );
            out.println( td( cardinality ) );
            
            out.println( "    <td>" );
            
            if( property.hasAnnotation( Documentation.class ) )
            {
                documentation( out, property.getAnnotation( Documentation.class ) );
            }
            else
            {
                out.println( "&nbsp;" );
            }
            
            if( property instanceof ElementProperty || property instanceof ListProperty )
            {
                boolean skip = false;
                
                final ElementType childType = property.service( PossibleTypesService.class ).types().first();
                final SortedSet<PropertyDef> childTypeProperties = childType.properties();
                
                if( childTypeProperties.size() == 1 )
                {
                    final PropertyDef childTypeProperty = childTypeProperties.first();
                    
                    if( childTypeProperty instanceof ValueProperty )
                    {
                        if( childTypeProperty.hasAnnotation( XmlBinding.class ) )
                        {
                            final XmlBinding b = childTypeProperty.getAnnotation( XmlBinding.class );
                            
                            if( b != null && b.path().length() == 0 )
                            {
                                skip = true;
                            }
                        }
                        else if( childTypeProperty.hasAnnotation( XmlValueBinding.class ) )
                        {
                            final XmlValueBinding b = childTypeProperty.getAnnotation( XmlValueBinding.class );
                            
                            if( b != null && b.path().length() == 0 )
                            {
                                skip = true;
                            }
                        }
                    }
                }
                
                if( ! skip )
                {
                    out.println( "<br/><br/>" );
                    execute( childType, out );
                    out.println( "<br/>" );
                }
            }
            
            out.println( "    </td>" );
            out.println( "  </tr>" );
        }
        
        out.println( "</table>" );
        out.println();
    }
    
    private static void documentation( final PrintWriter out,
                                       final Documentation doc )
    {
        final String text = doc.content();
        final DocumentationContent content = DocumentationContent.parse( text );
        final String html = HtmlFormatter.format( content );
        
        out.println( html );
    }
    
    private static String style()
    {
        if( STYLE == null )
        {
            final StringBuilder buf = new StringBuilder();
            
            buf.append( "<style type=\"text/css\">\n" );
            
            try
            {
                final Bundle bundle = Platform.getBundle( "org.eclipse.sapphire.doc" );
                
                if( bundle != null )
                {
                    final URL url = bundle.getEntry( "html/style.css" );
                    
                    if( url != null )
                    {
                        final InputStream in = url.openStream();
                        
                        try
                        {
                            final Reader reader = new InputStreamReader( in, UTF8 );
                            final char[] chars = new char[ 1024 ];
                            
                            for( int count = reader.read( chars ); count != -1; count = reader.read( chars ) )
                            {
                                buf.append( chars, 0, count );
                            }
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
            }
            catch( Exception e )
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
            
            buf.append( "\n</style>" );
        
            STYLE = buf.toString();
        }
        
        return STYLE;
    }

    private static String normalize( final String text,
                                     final String textForNull )
    {
        return ( text == null || text.trim().length() == 0 ? textForNull : text );
    }
    
    private static String td( final String value )
    {
        final StringBuilder buf = new StringBuilder();
        
        buf.append( "    <td>" );
        buf.append( normalize( value, "&nbsp;" ) );
        buf.append( "</td>" );
        
        return buf.toString();
    }

}
