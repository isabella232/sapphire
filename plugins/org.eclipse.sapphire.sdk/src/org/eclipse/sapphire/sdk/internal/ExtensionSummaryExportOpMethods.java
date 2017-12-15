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

package org.eclipse.sapphire.sdk.internal;

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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.sdk.extensibility.IExtensionSummaryExportOp;
import org.eclipse.sapphire.sdk.extensibility.IExtensionSummarySectionColumnDef;
import org.eclipse.sapphire.sdk.extensibility.IExtensionSummarySectionDef;
import org.eclipse.sapphire.sdk.extensibility.IFunctionDef;
import org.eclipse.sapphire.sdk.extensibility.IModelElementServiceDef;
import org.eclipse.sapphire.sdk.extensibility.IModelPropertyServiceDef;
import org.eclipse.sapphire.sdk.extensibility.ISapphireExtensionDef;
import org.eclipse.sapphire.sdk.extensibility.ITypeCastDef;
import org.eclipse.sapphire.sdk.extensibility.IValueSerializationServiceDef;
import org.eclipse.sapphire.ui.def.ISapphireActionContext;
import org.eclipse.sapphire.ui.def.ISapphireActionDef;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerFactoryDef;
import org.osgi.framework.Bundle;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ExtensionSummaryExportOpMethods
{
    private static String STYLE;
    
    public static String execute( final IExtensionSummaryExportOp op,
                                  final List<ISapphireExtensionDef> extensions,
                                  final IProgressMonitor monitor )
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
        
        final ModelElementList<IExtensionSummarySectionDef> sections = op.getSections();
        
        if( sections.isEmpty() )
        {
            IExtensionSummarySectionDef def;
            
            def = sections.addNewElement();
            def.setExtensionType( ISapphireExtensionDef.PROP_MODEL_ELEMENT_SERVICES.getName() );
            
            def = sections.addNewElement();
            def.setExtensionType( ISapphireExtensionDef.PROP_MODEL_PROPERTY_SERVICES.getName() );

            def = sections.addNewElement();
            def.setExtensionType( ISapphireExtensionDef.PROP_VALUE_SERIALIZATION_SERVICES.getName() );

            def = sections.addNewElement();
            def.setExtensionType( ISapphireExtensionDef.PROP_FUNCTIONS.getName() );

            def = sections.addNewElement();
            def.setExtensionType( ISapphireExtensionDef.PROP_TYPE_CASTS.getName() );

            def = sections.addNewElement();
            def.setExtensionType( ISapphireExtensionDef.PROP_ACTIONS.getName() );

            def = sections.addNewElement();
            def.setExtensionType( ISapphireExtensionDef.PROP_ACTION_HANDLERS.getName() );

            def = sections.addNewElement();
            def.setExtensionType( ISapphireExtensionDef.PROP_ACTION_HANDLER_FACTORIES.getName() );
        }
        
        for( IExtensionSummarySectionDef def : sections )
        {
            final String extensionType = def.getExtensionType().getText();
            final SectionWriter sectionWriter;
            
            if( extensionType.equals( ISapphireExtensionDef.PROP_MODEL_ELEMENT_SERVICES.getName() ) )
            {
                sectionWriter = new ModelElementServicesSectionWriter( out, extensions, def );
            }
            else if( extensionType.endsWith( ISapphireExtensionDef.PROP_MODEL_PROPERTY_SERVICES.getName() ) )
            {
                sectionWriter = new ModelPropertyServicesSectionWriter( out, extensions, def );
            }
            else if( extensionType.endsWith( ISapphireExtensionDef.PROP_VALUE_SERIALIZATION_SERVICES.getName() ) )
            {
                sectionWriter = new ValueSerializationServicesSectionWriter( out, extensions, def );
            }
            else if( extensionType.endsWith( ISapphireExtensionDef.PROP_FUNCTIONS.getName() ) )
            {
                sectionWriter = new FunctionsSectionWriter( out, extensions, def );
            }
            else if( extensionType.endsWith( ISapphireExtensionDef.PROP_TYPE_CASTS.getName() ) )
            {
                sectionWriter = new TypeCastsSectionWriter( out, extensions, def );
            }
            else if( extensionType.endsWith( ISapphireExtensionDef.PROP_ACTIONS.getName() ) )
            {
                sectionWriter = new ActionsSectionWriter( out, extensions, def );
            }
            else if( extensionType.endsWith( ISapphireExtensionDef.PROP_ACTION_HANDLERS.getName() ) )
            {
                sectionWriter = new ActionHandlersSectionWriter( out, extensions, def );
            }
            else if( extensionType.endsWith( ISapphireExtensionDef.PROP_ACTION_HANDLER_FACTORIES.getName() ) )
            {
                sectionWriter = new ActionHandlerFactoriesSectionWriter( out, extensions, def );
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
    
    private static final class ModelElementServicesSectionWriter extends SectionWriter
    {
        public ModelElementServicesSectionWriter( final PrintWriter out,
                                                  final List<ISapphireExtensionDef> extensions,
                                                  final IExtensionSummarySectionDef def )
        {
            super( out, extensions, def );
        }
        
        @Override
        protected List<ModelProperty> getDefaultColumns()
        {
            final List<ModelProperty> columns = new ArrayList<ModelProperty>();
            columns.add( IModelElementServiceDef.PROP_TYPE_CLASS );
            columns.add( IModelElementServiceDef.PROP_FACTORY_CLASS );
            return columns;
        }
    }
    
    private static final class ModelPropertyServicesSectionWriter extends SectionWriter
    {
        public ModelPropertyServicesSectionWriter( final PrintWriter out,
                                                   final List<ISapphireExtensionDef> extensions,
                                                   final IExtensionSummarySectionDef def )
        {
            super( out, extensions, def );
        }
        
        @Override
        protected List<ModelProperty> getDefaultColumns()
        {
            final List<ModelProperty> columns = new ArrayList<ModelProperty>();
            columns.add( IModelPropertyServiceDef.PROP_TYPE_CLASS );
            columns.add( IModelPropertyServiceDef.PROP_FACTORY_CLASS );
            return columns;
        }
    }
    
    private static final class ValueSerializationServicesSectionWriter extends SectionWriter
    {
        public ValueSerializationServicesSectionWriter( final PrintWriter out,
                                                        final List<ISapphireExtensionDef> extensions,
                                                        final IExtensionSummarySectionDef def )
        {
            super( out, extensions, def );
        }
        
        @Override
        protected List<ModelProperty> getDefaultColumns()
        {
            final List<ModelProperty> columns = new ArrayList<ModelProperty>();
            columns.add( IValueSerializationServiceDef.PROP_TYPE_CLASS );
            columns.add( IValueSerializationServiceDef.PROP_IMPL_CLASS );
            return columns;
        }
    }
    
    private static final class FunctionsSectionWriter extends SectionWriter
    {
        public FunctionsSectionWriter( final PrintWriter out,
                                       final List<ISapphireExtensionDef> extensions,
                                       final IExtensionSummarySectionDef def )
        {
            super( out, extensions, def );
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
                        final IFunctionDef x = (IFunctionDef) a;
                        final IFunctionDef y = (IFunctionDef) b;
                        
                        return comp( x.getName().getContent(), y.getName().getContent() );
                    }
                }
            );
        }

        @Override
        protected List<ModelProperty> getDefaultColumns()
        {
            final List<ModelProperty> columns = new ArrayList<ModelProperty>();
            columns.add( IFunctionDef.PROP_NAME );
            columns.add( IFunctionDef.PROP_DESCRIPTION );
            columns.add( IFunctionDef.PROP_IMPL_CLASS );
            return columns;
        }
    }
    
    private static final class TypeCastsSectionWriter extends SectionWriter
    {
        public TypeCastsSectionWriter( final PrintWriter out,
                                       final List<ISapphireExtensionDef> extensions,
                                       final IExtensionSummarySectionDef def )
        {
            super( out, extensions, def );
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
                        final ITypeCastDef x = (ITypeCastDef) a;
                        final ITypeCastDef y = (ITypeCastDef) b;
                        
                        return comp( x.getTargetType().getText(), y.getTargetType().getText() );
                    }
                }
            );
        }

        @Override
        protected List<ModelProperty> getDefaultColumns()
        {
            final List<ModelProperty> columns = new ArrayList<ModelProperty>();
            columns.add( ITypeCastDef.PROP_SOURCE_TYPE );
            columns.add( ITypeCastDef.PROP_TARGET_TYPE );
            columns.add( ITypeCastDef.PROP_DESCRIPTION );
            columns.add( ITypeCastDef.PROP_IMPLEMENTATION );
            return columns;
        }
    }
    
    private static final class ActionsSectionWriter extends SectionWriter
    {
        public ActionsSectionWriter( final PrintWriter out,
                                     final List<ISapphireExtensionDef> extensions,
                                     final IExtensionSummarySectionDef def )
        {
            super( out, extensions, def );
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
                        final ISapphireActionDef x = (ISapphireActionDef) a;
                        final ISapphireActionDef y = (ISapphireActionDef) b;
                        
                        return comp( x.getId().getContent(), y.getId().getContent() );
                    }
                }
            );
        }

        @Override
        protected List<ModelProperty> getDefaultColumns()
        {
            final List<ModelProperty> columns = new ArrayList<ModelProperty>();
            columns.add( ISapphireActionDef.PROP_ID );
            columns.add( ISapphireActionDef.PROP_DESCRIPTION );
            columns.add( ISapphireActionDef.PROP_KEY_BINDING );
            columns.add( ISapphireActionDef.PROP_CONTEXTS );
            return columns;
        }

        @Override
        protected String getCellText( final IModelElement element,
                                      final ModelProperty property )
        {
            if( property == ISapphireActionDef.PROP_CONTEXTS )
            {
                final StringBuilder contexts = new StringBuilder();
                
                for( ISapphireActionContext context : ( (ISapphireActionDef) element ).getContexts() )
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
                                            final List<ISapphireExtensionDef> extensions,
                                            final IExtensionSummarySectionDef def )
        {
            super( out, extensions, def );
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
                        final ISapphireActionHandlerDef x = (ISapphireActionHandlerDef) a;
                        final ISapphireActionHandlerDef y = (ISapphireActionHandlerDef) b;
                        
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
            columns.add( ISapphireActionHandlerDef.PROP_ACTION );
            columns.add( ISapphireActionHandlerDef.PROP_ID );
            columns.add( ISapphireActionHandlerDef.PROP_DESCRIPTION );
            return columns;
        }
    }
    
    private static final class ActionHandlerFactoriesSectionWriter extends SectionWriter
    {
        public ActionHandlerFactoriesSectionWriter( final PrintWriter out,
                                                    final List<ISapphireExtensionDef> extensions,
                                                    final IExtensionSummarySectionDef def )
        {
            super( out, extensions, def );
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
                        final ISapphireActionHandlerFactoryDef x = (ISapphireActionHandlerFactoryDef) a;
                        final ISapphireActionHandlerFactoryDef y = (ISapphireActionHandlerFactoryDef) b;
                        
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
            columns.add( ISapphireActionHandlerFactoryDef.PROP_ACTION );
            columns.add( ISapphireActionHandlerFactoryDef.PROP_DESCRIPTION );
            return columns;
        }
    }
    
    private static abstract class SectionWriter
    {
        private final PrintWriter out;
        private final List<ISapphireExtensionDef> extensions;
        private final IExtensionSummarySectionDef def;
        
        public SectionWriter( final PrintWriter out,
                              final List<ISapphireExtensionDef> extensions,
                              final IExtensionSummarySectionDef def )
        {
            this.out = out;
            this.extensions = extensions;
            this.def = def;
        }
        
        public final void write()
        {
            final ListProperty extTypeListProperty = (ListProperty) ISapphireExtensionDef.TYPE.getProperty( this.def.getExtensionType().getText() );
            final ModelElementType extType = extTypeListProperty.getType();
            
            final List<IModelElement> extElements = new ArrayList<IModelElement>();
            
            for( ISapphireExtensionDef extension : this.extensions )
            {
                extElements.addAll( extension.read( extTypeListProperty ) );
            }
            
            if( ! extElements.isEmpty() )
            {
                // Sort extensions.
                
                sort( extElements );
                
                // Write section header.
                
                if( this.def.getIncludeSectionHeader().getContent() )
                {
                    final IExtensionSummaryExportOp op = this.def.nearest( IExtensionSummaryExportOp.class );
                    
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
                
                for( IExtensionSummarySectionColumnDef cdef : this.def.getColumns() )
                {
                    final ModelProperty cprop = extType.getProperty( cdef.getName().getText() );
                    
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
            String text = element.read( (ValueProperty) property ).getText();
            
            final Reference ref = property.getAnnotation( Reference.class );
            
            if( ref != null && ref.target() == Class.class )
            {
                text = formatClassName( text );
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
