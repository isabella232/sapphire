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

package org.eclipse.sapphire.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.modeling.ClassLoaderResourceResolver;
import org.eclipse.sapphire.modeling.ClassResolver;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.UrlResourceStore;
import org.eclipse.sapphire.modeling.internal.SapphireModelingExtensionSystem;
import org.eclipse.sapphire.modeling.internal.SapphireModelingExtensionSystem.ExtensionHandle;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.ui.def.ISapphireActionDef;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerFactoryDef;
import org.eclipse.sapphire.ui.def.ISapphireUiExtensionDef;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireExtensionSystem
{
    private static List<ISapphireUiExtensionDef> extensions;
    private static List<ISapphireActionDef> actions;
    private static List<ISapphireActionHandlerDef> actionHandlers;
    private static List<ISapphireActionHandlerFactoryDef> actionHandlerFactories;

    public static synchronized List<ISapphireUiExtensionDef> getExtensions()
    {
        if( extensions == null )
        {
            final List<ISapphireUiExtensionDef> list = new ArrayList<ISapphireUiExtensionDef>();

            for( final ExtensionHandle handle : SapphireModelingExtensionSystem.getExtensionHandles() )
            {
                final ClassResolver classResolver = new ClassResolver()
                {
                    @Override
                    public Class<?> resolve( final String name )
                    {
                        try
                        {
                            return handle.loadClass( name );
                        }
                        catch( SapphireModelingExtensionSystem.InvalidExtensionException e )
                        {
                            return null;
                        }
                    }
                };
                
                final ClassLoaderResourceResolver classLoaderResourceResolver = new ClassLoaderResourceResolver()
                {
                    @Override
                    public URL resolve( final String name )
                    {
                        return handle.resolveResource( name );
                    }
                };
                
                for( URL url : handle.findExtensionFiles() )
                {
                    try
                    {
                        final UrlResourceStore store = new UrlResourceStore( url )
                        {
                            @Override
                            @SuppressWarnings("unchecked")
                            public <A> A adapt( final Class<A> adapterType )
                            {
                                if( adapterType == ClassResolver.class )
                                {
                                    return (A) classResolver;
                                }
                                else if( adapterType == ClassLoaderResourceResolver.class )
                                {
                                    return (A) classLoaderResourceResolver;
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
                        SapphireUiFrameworkPlugin.log( e );
                    }
                }
            }

            extensions = Collections.unmodifiableList( list );
        }

        return extensions;
    }

    public static synchronized List<ISapphireActionDef> getActions()
    {
        if( actions == null )
        {
            final List<ISapphireActionDef> list = new ArrayList<ISapphireActionDef>();

            for( ISapphireUiExtensionDef extension : getExtensions() )
            {
                for( ISapphireActionDef def : extension.getActions() )
                {
                    // TODO: Validate and log
                    list.add( def );
                }
            }

            actions = Collections.unmodifiableList( list );
        }

        return actions;
    }

    public static synchronized List<ISapphireActionHandlerDef> getActionHandlers()
    {
        if( actionHandlers == null )
        {
            final List<ISapphireActionHandlerDef> list = new ArrayList<ISapphireActionHandlerDef>();

            for( ISapphireUiExtensionDef extension : getExtensions() )
            {
                for( ISapphireActionHandlerDef def : extension.getActionHandlers() )
                {
                    // TODO: Validate and log
                    list.add( def );
                }
            }

            actionHandlers = Collections.unmodifiableList( list );
        }

        return actionHandlers;
    }

    public static synchronized List<ISapphireActionHandlerFactoryDef> getActionHandlerFactories()
    {
        if( actionHandlerFactories == null )
        {
            final List<ISapphireActionHandlerFactoryDef> list = new ArrayList<ISapphireActionHandlerFactoryDef>();

            for( ISapphireUiExtensionDef extension : getExtensions() )
            {
                for( ISapphireActionHandlerFactoryDef def : extension.getActionHandlerFactories() )
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
