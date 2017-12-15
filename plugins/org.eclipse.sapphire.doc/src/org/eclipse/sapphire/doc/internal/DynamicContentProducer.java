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

package org.eclipse.sapphire.doc.internal;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.help.IHelpContentProducer;
import org.eclipse.sapphire.modeling.ExtensionsLocator;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.UrlResourceStore;
import org.eclipse.sapphire.modeling.util.Filter;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.sdk.extensibility.ExtensionSummaryExportOp;
import org.eclipse.sapphire.sdk.extensibility.ExtensionSummarySectionColumnDef;
import org.eclipse.sapphire.sdk.extensibility.ExtensionSummarySectionDef;
import org.eclipse.sapphire.sdk.extensibility.FunctionDef;
import org.eclipse.sapphire.sdk.extensibility.SapphireExtensionDef;
import org.eclipse.sapphire.sdk.extensibility.ServiceDef;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.ui.IExportModelDocumentationOp;
import org.eclipse.sapphire.ui.def.ActionDef;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.def.ActionHandlerFactoryDef;
import org.eclipse.sapphire.ui.def.ActionHandlerFilterDef;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class DynamicContentProducer implements IHelpContentProducer
{
    private List<SapphireExtensionDef> extensions;

    public InputStream getInputStream( final String pluginID,
                                       final String href,
                                       final Locale locale )
    {
        String content = null;

        if( pluginID.equals( "org.eclipse.sapphire.doc" ) )
        {
            if( href.startsWith( "html/extensions/existing.html" ) )
            {
                final ExtensionSummaryExportOp op = ExtensionSummaryExportOp.TYPE.instantiate();
                op.setDocumentBodyTitle( "Sapphire Extensions" );

                content = op.execute( getExtensions(), null );
            }
            else if( href.startsWith( "html/el/index.html" ) )
            {
                final ExtensionSummaryExportOp op = ExtensionSummaryExportOp.TYPE.instantiate();
                op.setCreateFinishedDocument( false );

                final ExtensionSummarySectionDef section = op.getSections().insert();
                section.setExtensionType( SapphireExtensionDef.PROP_FUNCTIONS.getName() );
                section.setIncludeSectionHeader( false );

                final ExtensionSummarySectionColumnDef nameColumn = section.getColumns().insert();
                nameColumn.setName( FunctionDef.PROP_NAME.getName() );

                final ExtensionSummarySectionColumnDef descColumn = section.getColumns().insert();
                descColumn.setName( FunctionDef.PROP_DESCRIPTION.getName() );

                final String functions = op.execute( getExtensions(), null );

                content = loadResource( "html/el/index.html" );
                content = content.replace( "##functions##", functions );
            }
            else if( href.startsWith( "html/services/index.html" ) )
            {
                final ExtensionSummaryExportOp op = ExtensionSummaryExportOp.TYPE.instantiate();
                op.setCreateFinishedDocument( false );

                final ExtensionSummarySectionDef section = op.getSections().insert();
                section.setExtensionType( SapphireExtensionDef.PROP_SERVICES.getName() );
                section.setIncludeSectionHeader( false );

                final ExtensionSummarySectionColumnDef idColumn = section.getColumns().insert();
                idColumn.setName( ServiceDef.PROP_ID.getName() );

                final ExtensionSummarySectionColumnDef descColumn = section.getColumns().insert();
                descColumn.setName( ServiceDef.PROP_DESCRIPTION.getName() );
                
                final Filter<IModelElement> filter = new Filter<IModelElement>()
                {
                    @Override
                    public boolean check( final IModelElement element )
                    {
                        if( element instanceof ServiceDef )
                        {
                            final ServiceDef def = (ServiceDef) element;
                            final String id = def.getId().getText();
                            final String type = def.getType().getText();
                            
                            if( id != null && id.startsWith( "Sapphire." ) && 
                                type != null && type.equals( FactsService.class.getName() ) )
                            {
                                return true;
                            }
                        }

                        return false;
                    }
                };

                final String functions = op.execute( getExtensions(), filter );

                content = loadResource( "html/services/index.html" );
                content = content.replace( "##facts-servicess##", functions );
            }
            else if( href.startsWith( "html/actions/index.html" ) )
            {
                content = loadResource( "html/actions/index.html" );

                final String docAction = exportModelDocumentation( ActionDef.TYPE );
                content = content.replace( "##action-details##", docAction );

                final String docActionHandler = exportModelDocumentation( ActionHandlerDef.TYPE );
                content = content.replace( "##action-handler-details##", docActionHandler );

                final String docActionHandlerFactory = exportModelDocumentation( ActionHandlerFactoryDef.TYPE );
                content = content.replace( "##action-handler-factory-details##", docActionHandlerFactory );

                final String docActionHandlerFilter = exportModelDocumentation( ActionHandlerFilterDef.TYPE );
                content = content.replace( "##action-handler-filter-details##", docActionHandlerFilter );
            }
        }

        if( content != null )
        {
            return new ByteArrayInputStream( content.getBytes() );
        }

        return null;
    }

    private static String exportModelDocumentation( final ModelElementType type )
    {
        final IExportModelDocumentationOp op = IExportModelDocumentationOp.TYPE.instantiate();
        op.setCreateFinishedDocument( false );
        return op.execute( type, new NullProgressMonitor() );
    }

    private static String loadResource( final String name )
    {
        final InputStream in = DynamicContentProducer.class.getClassLoader().getResourceAsStream( name );

        if( in == null )
        {
            throw new IllegalArgumentException( name );
        }

        try
        {
            final BufferedReader r = new BufferedReader( new InputStreamReader( in ) );
            final char[] chars = new char[ 1024 ];
            final StringBuilder buf = new StringBuilder();

            for( int i = r.read( chars ); i != -1; i = r.read( chars ) )
            {
                buf.append( chars, 0, i );
            }

            return buf.toString();
        }
        catch( IOException e )
        {
            throw new RuntimeException( e );
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

    private synchronized List<SapphireExtensionDef> getExtensions()
    {
        if( extensions == null )
        {
            final List<SapphireExtensionDef> list = new ArrayList<SapphireExtensionDef>();

            for( ExtensionsLocator.Handle handle : ExtensionsLocator.instance().find() )
            {
                try
                {
                    final XmlResourceStore store = new XmlResourceStore( new UrlResourceStore( handle.extension() ) );
                    final RootXmlResource resource = new RootXmlResource( store );
                    final SapphireExtensionDef extension = SapphireExtensionDef.TYPE.instantiate( resource );
                    list.add( extension );
                }
                catch( ResourceStoreException e )
                {
                    SapphireUiFrameworkPlugin.log( e );
                }
            }

            extensions = Collections.unmodifiableList( list );
        }

        return extensions;
    }

}
