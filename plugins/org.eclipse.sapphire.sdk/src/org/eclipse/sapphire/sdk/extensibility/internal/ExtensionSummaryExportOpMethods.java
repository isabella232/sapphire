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

package org.eclipse.sapphire.sdk.extensibility.internal;

import static org.eclipse.sapphire.util.StringUtil.UTF8;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Filter;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.sdk.extensibility.ExtensionSummaryExportOp;
import org.eclipse.sapphire.sdk.extensibility.ExtensionSummarySectionColumnDef;
import org.eclipse.sapphire.sdk.extensibility.ExtensionSummarySectionDef;
import org.eclipse.sapphire.sdk.extensibility.FunctionDef;
import org.eclipse.sapphire.sdk.extensibility.SapphireExtensionDef;
import org.eclipse.sapphire.sdk.extensibility.ServiceDef;
import org.eclipse.sapphire.ui.def.ActionContextRef;
import org.eclipse.sapphire.ui.def.ActionDef;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.def.ActionHandlerFactoryDef;
import org.eclipse.sapphire.ui.def.PresentationStyleDef;
import org.osgi.framework.Bundle;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ExtensionSummaryExportOpMethods
{
    private static String STYLE;
    
    public static String execute( final ExtensionSummaryExportOp op,
                                  final List<SapphireExtensionDef> extensions,
                                  final Filter<Element> filter )
    {
        // Write the summary document.
        
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
            
            final String documentBodyTitle = op.getDocumentBodyTitle().content();
            
            if( documentBodyTitle != null )
            {
                out.println( "<h1>" + documentBodyTitle + "</h1>" );
            }
        }
        
        final ElementList<ExtensionSummarySectionDef> sections = op.getSections();
        
        if( sections.isEmpty() )
        {
            ExtensionSummarySectionDef def;
            
            def = sections.insert();
            def.setExtensionType( SapphireExtensionDef.PROP_SERVICES.name() );
            
            def = sections.insert();
            def.setExtensionType( SapphireExtensionDef.PROP_FUNCTIONS.name() );

            def = sections.insert();
            def.setExtensionType( SapphireExtensionDef.PROP_ACTIONS.name() );

            def = sections.insert();
            def.setExtensionType( SapphireExtensionDef.PROP_ACTION_HANDLERS.name() );

            def = sections.insert();
            def.setExtensionType( SapphireExtensionDef.PROP_ACTION_HANDLER_FACTORIES.name() );

            def = sections.insert();
            def.setExtensionType( SapphireExtensionDef.PROP_PRESENTATION_STYLES.name() );
        }
        
        for( ExtensionSummarySectionDef def : sections )
        {
            final String extensionType = def.getExtensionType().text();
            final SectionWriter sectionWriter;
            
            if( extensionType.equals( SapphireExtensionDef.PROP_SERVICES.name() ) )
            {
                sectionWriter = new ServicesSectionWriter( out, extensions, filter, def );
            }
            else if( extensionType.endsWith( SapphireExtensionDef.PROP_FUNCTIONS.name() ) )
            {
                sectionWriter = new FunctionsSectionWriter( out, extensions, filter, def );
            }
            else if( extensionType.endsWith( SapphireExtensionDef.PROP_ACTIONS.name() ) )
            {
                sectionWriter = new ActionsSectionWriter( out, extensions, filter, def );
            }
            else if( extensionType.endsWith( SapphireExtensionDef.PROP_ACTION_HANDLERS.name() ) )
            {
                sectionWriter = new ActionHandlersSectionWriter( out, extensions, filter, def );
            }
            else if( extensionType.endsWith( SapphireExtensionDef.PROP_ACTION_HANDLER_FACTORIES.name() ) )
            {
                sectionWriter = new ActionHandlerFactoriesSectionWriter( out, extensions, filter, def );
            }
            else if( extensionType.endsWith( SapphireExtensionDef.PROP_PRESENTATION_STYLES.name() ) )
            {
                sectionWriter = new PresentationStylesSectionWriter( out, extensions, filter, def );
            }
            else
            {
                throw new IllegalStateException();
            }
            
            sectionWriter.write();
        }
        
        if( op.getCreateFinishedDocument().content() )
        {
            out.println( "<br/><br/>" );
            out.println();
            out.println( "</body>" );
            out.println();
            out.println( "</html>" );
        }
        
        out.flush();
        
        return stringWriter.getBuffer().toString();
    }
    
    private static final class ServicesSectionWriter extends SectionWriter
    {
        public ServicesSectionWriter( final PrintWriter out,
                                      final List<SapphireExtensionDef> extensions,
                                      final Filter<Element> filter,
                                      final ExtensionSummarySectionDef def )
        {
            super( out, extensions, filter, def );
        }
        
        @Override
        protected void sort( final List<Element> extElements )
        {
            Collections.sort
            ( 
                extElements, 
                new Comparator<Element>()
                {
                    public int compare( final Element a,
                                        final Element b )
                    {
                        final ServiceDef x = (ServiceDef) a;
                        final ServiceDef y = (ServiceDef) b;
                        
                        return comp( x.getId().content(), y.getId().content() );
                    }
                }
            );
        }

        @Override
        protected List<PropertyDef> getDefaultColumns()
        {
            final List<PropertyDef> columns = new ArrayList<PropertyDef>();
            columns.add( ServiceDef.PROP_ID );
            columns.add( ServiceDef.PROP_DESCRIPTION );
            columns.add( ServiceDef.PROP_CONTEXTS );
            return columns;
        }
    }
    
    private static final class FunctionsSectionWriter extends SectionWriter
    {
        public FunctionsSectionWriter( final PrintWriter out,
                                       final List<SapphireExtensionDef> extensions,
                                       final Filter<Element> filter,
                                       final ExtensionSummarySectionDef def )
        {
            super( out, extensions, filter, def );
        }
        
        @Override
        protected void sort( final List<Element> extElements )
        {
            Collections.sort
            ( 
                extElements, 
                new Comparator<Element>()
                {
                    public int compare( final Element a,
                                        final Element b )
                    {
                        final FunctionDef x = (FunctionDef) a;
                        final FunctionDef y = (FunctionDef) b;
                        
                        return comp( x.getName().content(), y.getName().content() );
                    }
                }
            );
        }

        @Override
        protected List<PropertyDef> getDefaultColumns()
        {
            final List<PropertyDef> columns = new ArrayList<PropertyDef>();
            columns.add( FunctionDef.PROP_NAME );
            columns.add( FunctionDef.PROP_DESCRIPTION );
            columns.add( FunctionDef.PROP_IMPL_CLASS );
            return columns;
        }
    }
    
    private static final class ActionsSectionWriter extends SectionWriter
    {
        public ActionsSectionWriter( final PrintWriter out,
                                     final List<SapphireExtensionDef> extensions,
                                     final Filter<Element> filter,
                                     final ExtensionSummarySectionDef def )
        {
            super( out, extensions, filter, def );
        }
        
        @Override
        protected void sort( final List<Element> extElements )
        {
            Collections.sort
            ( 
                extElements, 
                new Comparator<Element>()
                {
                    public int compare( final Element a,
                                        final Element b )
                    {
                        final ActionDef x = (ActionDef) a;
                        final ActionDef y = (ActionDef) b;
                        
                        return comp( x.getId().content(), y.getId().content() );
                    }
                }
            );
        }

        @Override
        protected List<PropertyDef> getDefaultColumns()
        {
            final List<PropertyDef> columns = new ArrayList<PropertyDef>();
            columns.add( ActionDef.PROP_ID );
            columns.add( ActionDef.PROP_DESCRIPTION );
            columns.add( ActionDef.PROP_KEY_BINDING );
            columns.add( ActionDef.PROP_CONTEXTS );
            return columns;
        }

        @Override
        protected String getCellText( final Element element,
                                      final PropertyDef property )
        {
            if( property == ActionDef.PROP_CONTEXTS )
            {
                final StringBuilder contexts = new StringBuilder();
                
                for( ActionContextRef context : ( (ActionDef) element ).getContexts() )
                {
                    if( contexts.length() > 0 )
                    {
                        contexts.append( "<br/>" );
                    }
                    
                    contexts.append( normalize( context.getContext().text() ) );
                }
                
                return contexts.toString();
            }
            
            return super.getCellText( element, property );
        }
    }
    
    private static final class ActionHandlersSectionWriter extends SectionWriter
    {
        public ActionHandlersSectionWriter( final PrintWriter out,
                                            final List<SapphireExtensionDef> extensions,
                                            final Filter<Element> filter,
                                            final ExtensionSummarySectionDef def )
        {
            super( out, extensions, filter, def );
        }
        
        @Override
        protected void sort( final List<Element> extElements )
        {
            Collections.sort
            ( 
                extElements, 
                new Comparator<Element>()
                {
                    public int compare( final Element a,
                                        final Element b )
                    {
                        final ActionHandlerDef x = (ActionHandlerDef) a;
                        final ActionHandlerDef y = (ActionHandlerDef) b;
                        
                        int res = comp( x.getAction().content(), y.getAction().content() );
                        
                        if( res == 0 )
                        {
                            res = comp( x.getId().content(), y.getId().content() );
                        }
                        
                        return res;
                    }
                }
            );
        }

        @Override
        protected List<PropertyDef> getDefaultColumns()
        {
            final List<PropertyDef> columns = new ArrayList<PropertyDef>();
            columns.add( ActionHandlerDef.PROP_ACTION );
            columns.add( ActionHandlerDef.PROP_ID );
            columns.add( ActionHandlerDef.PROP_DESCRIPTION );
            return columns;
        }
    }
    
    private static final class ActionHandlerFactoriesSectionWriter extends SectionWriter
    {
        public ActionHandlerFactoriesSectionWriter( final PrintWriter out,
                                                    final List<SapphireExtensionDef> extensions,
                                                    final Filter<Element> filter,
                                                    final ExtensionSummarySectionDef def )
        {
            super( out, extensions, filter, def );
        }
        
        @Override
        protected void sort( final List<Element> extElements )
        {
            Collections.sort
            ( 
                extElements, 
                new Comparator<Element>()
                {
                    public int compare( final Element a,
                                        final Element b )
                    {
                        final ActionHandlerFactoryDef x = (ActionHandlerFactoryDef) a;
                        final ActionHandlerFactoryDef y = (ActionHandlerFactoryDef) b;
                        
                        int res = comp( x.getAction().content(), y.getAction().content() );
                        
                        if( res == 0 )
                        {
                            res = comp( x.getImplClass().text(), y.getImplClass().text() );
                        }
                        
                        return res;
                    }
                }
            );
        }

        @Override
        protected List<PropertyDef> getDefaultColumns()
        {
            final List<PropertyDef> columns = new ArrayList<PropertyDef>();
            columns.add( ActionHandlerFactoryDef.PROP_ACTION );
            columns.add( ActionHandlerFactoryDef.PROP_DESCRIPTION );
            return columns;
        }
    }
    
    private static final class PresentationStylesSectionWriter extends SectionWriter
    {
        public PresentationStylesSectionWriter( final PrintWriter out,
                                                final List<SapphireExtensionDef> extensions,
                                                final Filter<Element> filter,
                                                final ExtensionSummarySectionDef def )
        {
            super( out, extensions, filter, def );
        }
        
        @Override
        protected void sort( final List<Element> extElements )
        {
            Collections.sort
            ( 
                extElements, 
                new Comparator<Element>()
                {
                    public int compare( final Element a,
                                        final Element b )
                    {
                        final PresentationStyleDef x = (PresentationStyleDef) a;
                        final PresentationStyleDef y = (PresentationStyleDef) b;
                        
                        return comp( x.getId().content(), y.getId().content() );
                    }
                }
            );
        }

        @Override
        protected List<PropertyDef> getDefaultColumns()
        {
            final List<PropertyDef> columns = new ArrayList<PropertyDef>();
            columns.add( PresentationStyleDef.PROP_ID );
            columns.add( PresentationStyleDef.PROP_PART_TYPE );
            columns.add( PresentationStyleDef.PROP_DESCRIPTION );
            return columns;
        }
    }
    
    private static abstract class SectionWriter
    {
        private final PrintWriter out;
        private final List<SapphireExtensionDef> extensions;
        private final Filter<Element> filter;
        private final ExtensionSummarySectionDef def;
        
        public SectionWriter( final PrintWriter out,
                              final List<SapphireExtensionDef> extensions,
                              final Filter<Element> filter,
                              final ExtensionSummarySectionDef def )
        {
            this.out = out;
            this.extensions = extensions;
            this.filter = filter;
            this.def = def;
        }
        
        public final void write()
        {
            final ListProperty extTypeListProperty = (ListProperty) SapphireExtensionDef.TYPE.property( this.def.getExtensionType().text() );
            final ElementType extType = extTypeListProperty.getType();
            
            final List<Element> extElements = new ArrayList<Element>();
            
            if( this.filter == null )
            {
                for( SapphireExtensionDef extension : this.extensions )
                {
                    extElements.addAll( extension.property( extTypeListProperty ) );
                }
            }
            else
            {
                for( SapphireExtensionDef extension : this.extensions )
                {
                    for( Element extElement : extension.property( extTypeListProperty ) )
                    {
                        if( this.filter.allows( extElement ) )
                        {
                            extElements.add( extElement );
                        }
                    }
                }
            }
            
            if( ! extElements.isEmpty() )
            {
                // Sort extensions.
                
                sort( extElements );
                
                // Write section header.
                
                if( this.def.getIncludeSectionHeader().content() )
                {
                    final ExtensionSummaryExportOp op = this.def.nearest( ExtensionSummaryExportOp.class );
                    
                    final String sectionHeaderLevel 
                        = ( ( op.getCreateFinishedDocument().content() == false || op.getDocumentBodyTitle().content() == null ) ? "h1" : "h2" );
                    
                    this.out.println();
                    this.out.print( "<a name=\"" );
                    this.out.print( extTypeListProperty.name() );
                    this.out.print( "\"><" );
                    this.out.print( sectionHeaderLevel );
                    this.out.print( '>' );
                    this.out.print( extTypeListProperty.getLabel( true, CapitalizationType.TITLE_STYLE, false ) );
                    this.out.print( "</" );
                    this.out.print( sectionHeaderLevel );
                    this.out.print( "></a>" );
                    this.out.println();
                }
                
                // Determine columns.
                
                final List<PropertyDef> columns = new ArrayList<PropertyDef>();
                
                for( ExtensionSummarySectionColumnDef cdef : this.def.getColumns() )
                {
                    final PropertyDef cprop = extType.property( cdef.getName().text() );
                    
                    if( cprop != null )
                    {
                        columns.add( cprop );
                    }
                }
                
                if( columns.isEmpty() )
                {
                    columns.addAll( getDefaultColumns() );
                }
                
                for( Iterator<PropertyDef> itr = columns.iterator(); itr.hasNext(); )
                {
                    final PropertyDef cprop = itr.next();
                    boolean empty = true;
                    
                    if( cprop instanceof ValueProperty )
                    {
                        final ValueProperty cvprop = (ValueProperty) cprop;
                        
                        for( Element element : extElements )
                        {
                            if( element.property( cvprop ).text() != null )
                            {
                                empty = false;
                                break;
                            }
                        }
                    }
                    else if( cprop instanceof ListProperty )
                    {
                        final ListProperty clprop = (ListProperty) cprop;
                        
                        for( Element element : extElements )
                        {
                            if( ! element.property( clprop ).empty() )
                            {
                                empty = false;
                                break;
                            }
                        }
                    }
                    else
                    {
                        empty = false;
                    }
                    
                    if( empty )
                    {
                        itr.remove();
                    }
                }
    
                // Write extensions table.
                
                this.out.println();
                this.out.println( "<table>" );
                this.out.println( "  <tr>" );
                
                for( PropertyDef column : columns )
                {
                    this.out.print( "    <th>" );
                    this.out.print( column.getLabel( true, CapitalizationType.TITLE_STYLE, true ) );
                    this.out.print( "</th>" );
                    this.out.println();
                }
                
                this.out.println( "  </tr>" );
                
                for( Element extElement : extElements )
                {
                    this.out.println( "  <tr>" );
                    
                    for( PropertyDef column : columns )
                    {
                        final String text = getCellText( extElement, column );
                        
                        this.out.print( "    <td>" );
                        this.out.print( normalize( text, "&nbsp;" ) );
                        this.out.print( "</td>" );
                        this.out.println();
                    }
                    
                    this.out.println( "  </tr>" );
                }
                
                this.out.println( "</table>" );
            }
        }
        
        protected void sort( final List<Element> extElements )
        {
            // Do nothing by default.
        }
        
        protected abstract List<PropertyDef> getDefaultColumns();
        
        protected String getCellText( final Element element, 
                                      final PropertyDef property )
        {
            String text = null;
            
            if( property instanceof ValueProperty )
            {
                text = element.property( (ValueProperty) property ).text();
                
                final Reference ref = property.getAnnotation( Reference.class );
                
                if( ref != null && ref.target() == Class.class )
                {
                    text = formatClassName( text );
                }
            }
            else if( property instanceof ListProperty )
            {
                final ListProperty listProperty = (ListProperty) property;
                final ValueProperty entryValueProperty = (ValueProperty) listProperty.getType().properties().first();
                final StringBuilder buf = new StringBuilder();
                
                for( Element entry : element.property( listProperty ) )
                {
                    final String entryValuePropertyText = entry.property( entryValueProperty ).text();
                    
                    if( entryValuePropertyText != null )
                    {
                        if( buf.length() > 0 )
                        {
                            buf.append( "<br/>" );
                        }
                        
                        buf.append( entryValuePropertyText );
                    }
                }
                
                text = buf.toString();
            }
            
            if( text == null )
            {
                text = "";
            }
            
            return text; 
        }
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
                            
                            try
                            {
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
                                    reader.close();
                                }
                                catch( IOException e ) {}
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

    private static String normalize( final String text )
    {
        return normalize( text, "" );
    }
    
    private static String normalize( final String text,
                                     final String textForNull )
    {
        return ( text == null || text.trim().length() == 0 ? textForNull : text );
    }
    
    private static int comp( final String x,
                             final String y )
    {
        if( x == null )
        {
            return -1;
        }
        else if( y == null )
        {
            return 1;
        }
        else
        {
            return x.compareToIgnoreCase( y );
        }
    }

    private static String formatClassName( final String className )
    {
        final StringBuilder buf = new StringBuilder();
        final int lastDot = className.lastIndexOf( '.' );
        
        if( lastDot != -1 )
        {
            buf.append( "<font color=\"#888888\">" );
            buf.append( className.substring( 0, lastDot + 1 ) );
            buf.append( "</font>" );
        }
        
        final int lastDotPlusOne = lastDot + 1;
        
        if( lastDotPlusOne < className.length() )
        {
            buf.append( className.substring( lastDotPlusOne ) );
        }
        
        return buf.toString();
    }
    
}
