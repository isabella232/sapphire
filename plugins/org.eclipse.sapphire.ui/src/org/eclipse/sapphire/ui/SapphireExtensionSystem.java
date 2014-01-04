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

package org.eclipse.sapphire.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.Context;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.modeling.ExtensionsLocator;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.UrlResourceStore;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.ui.def.ActionDef;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.def.ActionHandlerFactoryDef;
import org.eclipse.sapphire.ui.def.ISapphireUiExtensionDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SapphireExtensionSystem
{
    private static List<ISapphireUiExtensionDef> extensions;
    private static List<ActionDef> actions;
    private static List<ActionHandlerDef> actionHandlers;
    private static List<ActionHandlerFactoryDef> actionHandlerFactories;

    public static synchronized List<ISapphireUiExtensionDef> getExtensions()
    {
        if( extensions == null )
        {
            final List<ISapphireUiExtensionDef> list = new ArrayList<ISapphireUiExtensionDef>();

            for( final ExtensionsLocator.Handle handle : ExtensionsLocator.instance().find() )
            {
                try
                {
                    final UrlResourceStore store = new UrlResourceStore( handle.extension() )
                    {
                        @Override
                        public <A> A adapt( final Class<A> adapterType )
                        {
                            if( adapterType == Context.class )
                            {
                                return adapterType.cast( handle.context() );
                            }
                            
                            return super.adapt( adapterType );
                        }
                    };
                    
                    final XmlResourceStore xmlResourceStore = new XmlResourceStore( store );
                    final RootXmlResource resource = new RootXmlResource( xmlResourceStore );
                    final ISapphireUiExtensionDef extension = ISapphireUiExtensionDef.TYPE.instantiate( resource );
                    list.add( extension );
                }
                catch( ResourceStoreException e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                }
            }

            extensions = Collections.unmodifiableList( list );
        }

        return extensions;
    }

    public static synchronized List<ActionDef> getActions()
    {
        if( actions == null )
        {
            final List<ActionDef> list = new ArrayList<ActionDef>();

            for( ISapphireUiExtensionDef extension : getExtensions() )
            {
                for( ActionDef def : extension.getActions() )
                {
                    // TODO: Validate and log
                    list.add( def );
                }
            }

            actions = Collections.unmodifiableList( list );
        }

        return actions;
    }

    public static synchronized List<ActionHandlerDef> getActionHandlers()
    {
        if( actionHandlers == null )
        {
            final List<ActionHandlerDef> list = new ArrayList<ActionHandlerDef>();

            for( ISapphireUiExtensionDef extension : getExtensions() )
            {
                for( ActionHandlerDef def : extension.getActionHandlers() )
                {
                    // TODO: Validate and log
                    list.add( def );
                }
            }

            actionHandlers = Collections.unmodifiableList( list );
        }

        return actionHandlers;
    }

    public static synchronized List<ActionHandlerFactoryDef> getActionHandlerFactories()
    {
        if( actionHandlerFactories == null )
        {
            final List<ActionHandlerFactoryDef> list = new ArrayList<ActionHandlerFactoryDef>();

            for( ISapphireUiExtensionDef extension : getExtensions() )
            {
                for( ActionHandlerFactoryDef def : extension.getActionHandlerFactories() )
                {
                    // TODO: Validate and log
                    list.add( def );
                }
            }

            actionHandlerFactories = Collections.unmodifiableList( list );
        }

        return actionHandlerFactories;
    }

}
