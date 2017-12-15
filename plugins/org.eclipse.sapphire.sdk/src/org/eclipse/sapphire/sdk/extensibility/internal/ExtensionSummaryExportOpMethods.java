/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.sdk.extensibility.internal;

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
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.util.Filter;
import org.eclipse.sapphire.sdk.extensibility.ExtensionSummaryExportOp;
import org.eclipse.sapphire.sdk.extensibility.ExtensionSummarySectionColumnDef;
import org.eclipse.sapphire.sdk.extensibility.ExtensionSummarySectionDef;
import org.eclipse.sapphire.sdk.extensibility.FunctionDef;
import org.eclipse.sapphire.sdk.extensibility.SapphireExtensionDef;
import org.eclipse.sapphire.sdk.extensibility.ServiceDef;
import org.eclipse.sapphire.sdk.extensibility.TypeCastDef;
import org.eclipse.sapphire.ui.def.ActionContextRef;
import org.eclipse.sapphire.ui.def.ActionDef;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.def.ActionHandlerFactoryDef;
import org.eclipse.sapphire.ui.def.PresentationStyleDef;
import org.osgi.framework.Bundle;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ExtensionSummaryExportOpMethods
{
    private static String STYLE;
    
    public static String execute( final ExtensionSummaryExportOp op,
                                  final List<SapphireExtensionDef> extensions,
                                  final Filter<IModelElement> filter )
    {
        // Write the summary document.
        
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter out = new PrintWriter( stringWriter );
        
        if( op.getCreateFinishedDocument().getContent() )
        {
            out.println( "<html>" );
            out.println();
            out.println( "<head>" );
            out.println( "  <title>" + op.getDocumentTitle().getContent() + "</title>" );
            
            if( op.getEmbedDefaultStyle().getContent() )
            {
                out.println( style() );
            }
            
            out.println( "</head>" );
            out.println();
            out.println( "<body>" );
            
            final String documentBodyTitle = op.getDocumentBodyTitle().getContent();
            
            if( documentBodyTitle != null )
            {
                out.println( "<h1>" + documentBodyTitle + "</h1>" );
            }
        }
        
        final ModelElementList<ExtensionSummarySectionDef> sections = op.getSections();
        
        if( sections.isEmpty() )
        {
            ExtensionSummarySectionDef def;
            
            def = sections.insert();
            def.setExtensionType( SapphireExtensionDef.PROP_SERVICES.getName() );
            
            def = sections.insert();
            def.setExtensionType( SapphireExtensionDef.PROP_FUNCTIONS.getName() );

            def = sections.insert();
            def.setExtensionType( SapphireExtensionDef.PROP_TYPE_CASTS.getName() );

            def = sections.insert();
            def.setExtensionType( SapphireExtensionDef.PROP_ACTIONS.getName() );

            def = sections.insert();
            def.setExtensionType( SapphireExtensionDef.PROP_ACTION_HANDLERS.getName() );

            def = sections.insert();
            def.setExtensionType( SapphireExtensionDef.PROP_ACTION_HANDLER_FACTORIES.getName() );

            def = sections.insert();
            def.setExtensionType( SapphireExtensionDef.PROP_PRESENTATION_STYLES.getName() );
        }
        
        for( ExtensionSummarySectionDef def : sections )
        {
            final String extensionType = def.getExtensionType().getText();
            final SectionWriter sectionWriter;
            
            if( extensionType.equals( SapphireExtensionDef.PROP_SERVICES.getName() ) )
            {
                sectionWriter = new ServicesSectionWriter( out, extensions, filter, def );
            }
            else if( extensionType.endsWith( SapphireExtensionDef.PROP_FUNCTIONS.getName() ) )
            {
                sectionWriter = new FunctionsSectionWriter( out, extensions, filter, def );
            }
            else if( extensionType.endsWith( SapphireExtensionDef.PROP_TYPE_CASTS.getName() ) )
            {
                sectionWriter = new TypeCastsSectionWriter( out, extensions, filter, def );
            }
            else if( extensionType.endsWith( SapphireExtensionDef.PROP_ACTIONS.getName() ) )
            {
                sectionWriter = new ActionsSectionWriter( out, extensions, filter, def );
            }
            else if( extensionType.endsWith( SapphireExtensionDef.PROP_ACTION_HANDLERS.getName() ) )
            {
                sectionWriter = new ActionHandlersSectionWriter( out, extensions, filter, def );
            }
            else if( extensionType.endsWith( SapphireExtensionDef.PROP_ACTION_HANDLER_FACTORIES.getName() ) )
            {
                sectionWriter = new ActionHandlerFactoriesSectionWriter( out, extensions, filter, def );
            }
            else if( extensionType.endsWith( SapphireExtensionDef.PROP_PRESENTATION_STYLES.getName() ) )
            {
                sectionWriter = new PresentationStylesSectionWriter( out, extensions, filter, def );
            }
            else
            {
                throw new IllegalStateException();
            }
            
            sectionWriter.write();
        }
        
        if( op.getCreateFinishedDocument().getContent() )
        {
            out.println( "<br/><br/>" );
            out.println();
            out.println( "</body>" );
            out.println();
            out.println( "</head>" );
        }
        
        out.flush();
        
        return stringWriter.getBuffer().toString();
    }
    
    private static final class ServicesSectionWriter extends SectionWriter
    {
        public ServicesSectionWriter( final PrintWriter out,
                                      final List<SapphireExtensionDef> extensions,
                                      final Filter<IModelElement> filter,
                                      final ExtensionSummarySectionDef def )
        {
            super( out, extensions, filter, def );
        }
        
        @Override
        protected List<ModelProperty> getDefaultColumns()
        {
            final List<ModelProperty> columns = new ArrayList<ModelProperty>();
            columns.add( ServiceDef.PROP_TYPE );
            columns.add( ServiceDef.PROP_FACTORY );
            columns.add( ServiceDef.PROP_CONTEXTS );
            return columns;
        }
    }
    
    private static final class FunctionsSectionWriter extends SectionWriter
    {
        public FunctionsSectionWriter( final PrintWriter out,
                                       final List<SapphireExtensionDef> extensions,
                                       final Filter<IModelElement> filter,
                                       final ExtensionSummarySectionDef def )
        {
            super( out, extensions, filter, def );
        }
        
        @Override
        protected void sort( final List<IModelElement> extElements )
        {
            Collections.sort
            ( 
                extElements, 
                new Comparator<IModelElement>()
                {
                    public int compare( final IModelElement a,
                                        final IModelElement b )
                    {
                        final FunctionDef x = (FunctionDef) a;
                        final FunctionDef y = (FunctionDef) b;
                        
                        return comp( x.getName().getContent(), y.getName().getContent() );
                    }
                }
            );
        }

        @Override
        protected List<ModelProperty> getDefaultColumns()
        {
            final List<ModelProperty> columns = new ArrayList<ModelProperty>();
            columns.add( FunctionDef.PROP_NAME );
            columns.add( FunctionDef.PROP_DESCRIPTION );
            columns.add( FunctionDef.PROP_IMPL_CLASS );
            return columns;
        }
    }
    
    private static final class TypeCastsSectionWriter extends SectionWriter
    {
        public TypeCastsSectionWriter( final PrintWriter out,
                                       final List<SapphireExtensionDef> extensions,
                                       final Filter<IModelElement> filter,
                                       final ExtensionSummarySectionDef def )
        {
            super( out, extensions, filter, def );
        }
        
        @Override
        protected void sort( final List<IModelElement> extElements )
        {
            Collections.sort
            ( 
                extElements, 
                new Comparator<IModelElement>()
                {
                    public int compare( final IModelElement a,
                                        final IModelElement b )
                    {
                        final TypeCastDef x = (TypeCastDef) a;
                        final TypeCastDef y = (TypeCastDef) b;
                        
                        return comp( x.getTargetType().getText(), y.getTargetType().getText() );
                    }
                }
            );
        }

        @Override
        protected List<ModelProperty> getDefaultColumns()
        {
            final List<ModelProperty> columns = new ArrayList<ModelProperty>();
            columns.add( TypeCastDef.PROP_SOURCE_TYPE );
            columns.add( TypeCastDef.PROP_TARGET_TYPE );
            columns.add( TypeCastDef.PROP_DESCRIPTION );
            columns.add( TypeCastDef.PROP_IMPLEMENTATION );
            return columns;
        }
    }
    
    private static final class ActionsSectionWriter extends SectionWriter
    {
        public ActionsSectionWriter( final PrintWriter out,
                                     final List<SapphireExtensionDef> extensions,
                                     final Filter<IModelElement> filter,
                                     final ExtensionSummarySectionDef def )
        {
            super( out, extensions, filter, def );
        }
        
        @Override
        protected void sort( final List<IModelElement> extElements )
        {
            Collections.sort
            ( 
                extElements, 
                new Comparator<IModelElement>()
                {
                    public int compare( final IModelElement a,
                                        final IModelElement b )
                    {
                        final ActionDef x = (ActionDef) a;
                        final ActionDef y = (ActionDef) b;
                        
                        return comp( x.getId().getContent(), y.getId().getContent() );
                    }
                }
            );
        }

        @Override
        protected List<ModelProperty> getDefaultColumns()
        {
            final List<ModelProperty> columns = new ArrayList<ModelProperty>();
            columns.add( ActionDef.PROP_ID );
            columns.add( ActionDef.PROP_DESCRIPTION );
            columns.add( ActionDef.PROP_KEY_BINDING );
            columns.add( ActionDef.PROP_CONTEXTS );
            return columns;
        }

        @Override
        protected String getCellText( final IModelElement element,
                                      final ModelProperty property )
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
                    
                    contexts.append( normalize( context.getContext().getText() ) );
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
                                            final Filter<IModelElement> filter,
                                            final ExtensionSummarySectionDef def )
        {
            super( out, extensions, filter, def );
        }
        
        @Override
        protected void sort( final List<IModelElement> extElements )
        {
            Collections.sort
            ( 
                extElements, 
                new Comparator<IModelElement>()
                {
                    public int compare( final IModelElement a,
                                        final IModelElement b )
                    {
                        final ActionHandlerDef x = (ActionHandlerDef) a;
                        final ActionHandlerDef y = (ActionHandlerDef) b;
                        
                        int res = comp( x.getAction().getContent(), y.getAction().getContent() );
                        
                        if( res == 0 )
                        {
                            res = comp( x.getId().getContent(), y.getId().getContent() );
                        }
                        
                        return res;
                    }
                }
            );
        }

        @Override
        protected List<ModelProperty> getDefaultColumns()
        {
            final List<ModelProperty> columns = new ArrayList<ModelProperty>();
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
                                                    final Filter<IModelElement> filter,
                                                    final ExtensionSummarySectionDef def )
        {
            super( out, extensions, filter, def );
        }
        
        @Override
        protected void sort( final List<IModelElement> extElements )
        {
            Collections.sort
            ( 
                extElements, 
                new Comparator<IModelElement>()
                {
                    public int compare( final IModelElement a,
                                        final IModelElement b )
                    {
                        final ActionHandlerFactoryDef x = (ActionHandlerFactoryDef) a;
                        final ActionHandlerFactoryDef y = (ActionHandlerFactoryDef) b;
                        
                        int res = comp( x.getAction().getContent(), y.getAction().getContent() );
                        
                        if( res == 0 )
                        {
                            res = comp( x.getImplClass().getText(), y.getImplClass().getText() );
                        }
                        
                        return res;
                    }
                }
            );
        }

        @Override
        protected List<ModelProperty> getDefaultColumns()
        {
            final List<ModelProperty> columns = new ArrayList<ModelProperty>();
            columns.add( ActionHandlerFactoryDef.PROP_ACTION );
            columns.add( ActionHandlerFactoryDef.PROP_DESCRIPTION );
            return columns;
        }
    }
    
    private static final class PresentationStylesSectionWriter extends SectionWriter
    {
        public PresentationStylesSectionWriter( final PrintWriter out,
                                                final List<SapphireExtensionDef> extensions,
                                                final Filter<IModelElement> filter,
                                                final ExtensionSummarySectionDef def )
        {
            super( out, extensions, filter, def );
        }
        
        @Override
        protected void sort( final List<IModelElement> extElements )
        {
            Collections.sort
            ( 
                extElements, 
                new Comparator<IModelElement>()
                {
                    public int compare( final IModelElement a,
                                        final IModelElement b )
                    {
                        final PresentationStyleDef x = (PresentationStyleDef) a;
                        final PresentationStyleDef y = (PresentationStyleDef) b;
                        
                        return comp( x.getId().getContent(), y.getId().getContent() );
                    }
                }
            );
        }

        @Override
        protected List<ModelProperty> getDefaultColumns()
        {
            final List<ModelProperty> columns = new ArrayList<ModelProperty>();
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
        private final Filter<IModelElement> filter;
        private final ExtensionSummarySectionDef def;
        
        public SectionWriter( final PrintWriter out,
                              final List<SapphireExtensionDef> extensions,
                              final Filter<IModelElement> filter,
                              final ExtensionSummarySectionDef def )
        {
            this.out = out;
            this.extensions = extensions;
            this.filter = filter;
            this.def = def;
        }
        
        public final void write()
        {
            final ListProperty extTypeListProperty = (ListProperty) SapphireExtensionDef.TYPE.property( this.def.getExtensionType().getText() );
            final ModelElementType extType = extTypeListProperty.getType();
            
            final List<IModelElement> extElements = new ArrayList<IModelElement>();
            
            if( this.filter == null )
            {
                for( SapphireExtensionDef extension : this.extensions )
                {
                    extElements.addAll( extension.read( extTypeListProperty ) );
                }
            }
            else
            {
                for( SapphireExtensionDef extension : this.extensions )
                {
                    for( IModelElement extElement : extension.read( extTypeListProperty ) )
                    {
                        if( this.filter.check( extElement ) )
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
                
                if( this.def.getIncludeSectionHeader().getContent() )
                {
                    final ExtensionSummaryExportOp op = this.def.nearest( ExtensionSummaryExportOp.class );
                    
                    final String sectionHeaderLevel 
                        = ( ( op.getCreateFinishedDocument().getContent() == false || op.getDocumentBodyTitle().getContent() == null ) ? "h1" : "h2" );
                    
                    this.out.println();
                    this.out.print( "<a name=\"" );
                    this.out.print( extTypeListProperty.getName() );
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
                
                final List<ModelProperty> columns = new ArrayList<ModelProperty>();
                
                for( ExtensionSummarySectionColumnDef cdef : this.def.getColumns() )
                {
                    final ModelProperty cprop = extType.property( cdef.getName().getText() );
                    
                    if( cprop != null )
                    {
                        columns.add( cprop );
                    }
                }
                
                if( columns.isEmpty() )
                {
                    columns.addAll( getDefaultColumns() );
                }
                
                for( Iterator<ModelProperty> itr = columns.iterator(); itr.hasNext(); )
                {
                    final ModelProperty cprop = itr.next();
                    boolean empty = true;
                    
                    if( cprop instanceof ValueProperty )
                    {
                        final ValueProperty cvprop = (ValueProperty) cprop;
                        
                        for( IModelElement element : extElements )
                        {
                            if( element.read( cvprop ).getText() != null )
                            {
                                empty = false;
                                break;
                            }
                        }
                    }
                    else if( cprop instanceof ListProperty )
                    {
                        final ListProperty clprop = (ListProperty) cprop;
                        
                        for( IModelElement element : extElements )
                        {
                            if( ! element.read( clprop ).isEmpty() )
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
                
                for( ModelProperty column : columns )
                {
                    this.out.print( "    <th>" );
                    this.out.print( column.getLabel( true, CapitalizationType.TITLE_STYLE, true ) );
                    this.out.print( "</th>" );
                    this.out.println();
                }
                
                this.out.println( "  </tr>" );
                
                for( IModelElement extElement : extElements )
                {
                    this.out.println( "  <tr>" );
                    
                    for( ModelProperty column : columns )
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
        
        protected void sort( final List<IModelElement> extElements )
        {
            // Do nothing by default.
        }
        
        protected abstract List<ModelProperty> getDefaultColumns();
        
        protected String getCellText( final IModelElement element, 
                                      final ModelProperty property )
        {
            String text = null;
            
            if( property instanceof ValueProperty )
            {
                text = element.read( (ValueProperty) property ).getText();
                
                final Reference ref = property.getAnnotation( Reference.class );
                
                if( ref != null && ref.target() == Class.class )
                {
                    text = formatClassName( text );
                }
            }
            else if( property instanceof ListProperty )
            {
                final ListProperty listProperty = (ListProperty) property;
                final ValueProperty entryValueProperty = (ValueProperty) listProperty.getType().properties().get( 0 );
                final StringBuilder buf = new StringBuilder();
                
                for( IModelElement entry : element.read( listProperty ) )
                {
                    final String entryValuePropertyText = entry.read( entryValueProperty ).getText();
                    
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
                            final Reader reader = new InputStreamReader( in );
                            
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
                LoggingService.log( e );
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
        if( x == y )
        {
            return 0;
        }
        else if( x == null )
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
