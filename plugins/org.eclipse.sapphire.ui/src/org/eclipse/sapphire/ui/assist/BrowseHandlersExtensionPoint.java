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

package org.eclipse.sapphire.ui.assist;

import static org.eclipse.sapphire.modeling.util.internal.PluginUtil.findExtensions;
import static org.eclipse.sapphire.modeling.util.internal.PluginUtil.findRequiredAttribute;
import static org.eclipse.sapphire.modeling.util.internal.PluginUtil.getTopLevelElements;
import static org.eclipse.sapphire.modeling.util.internal.PluginUtil.instantiate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.util.internal.PluginUtil.InvalidExtensionException;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class BrowseHandlersExtensionPoint
{
    private static final String EL_BROWSE_HANDLER = "browse-handler";
    private static final String ATTR_FACTORY = "factory";
    
    private static final Comparator<BrowseHandlerFactory> COMPARATOR = new Comparator<BrowseHandlerFactory>()
    {
        public int compare( final BrowseHandlerFactory x,
                            final BrowseHandlerFactory y )
        {
            int res = x.getPriority() - y.getPriority();
            
            if( res == 0 )
            {
                res = x.getClass().getName().compareTo( y.getClass().getName() );
            }
            
            return res;
        }
    };
    
    private static List<BrowseHandlerFactory> browseHandlerFactories = null;
    
    public static List<BrowseHandler> getBrowseHandlers( final ValueProperty property )
    {
        initialize();
        
        final List<BrowseHandlerFactory> applicableBrowseHandlerFactories = new ArrayList<BrowseHandlerFactory>();
        
        for( BrowseHandlerFactory browseHandlerFactory : browseHandlerFactories )
        {
            try
            {
                if( browseHandlerFactory.isApplicable( property ) )
                {
                    applicableBrowseHandlerFactories.add( browseHandlerFactory );
                }
            }
            catch( Exception e )
            {
                SapphireUiFrameworkPlugin.log( e );
            }
        }
        
        Collections.sort( applicableBrowseHandlerFactories, COMPARATOR );
        
        final List<BrowseHandler> applicableBrowseHandlers = new ArrayList<BrowseHandler>();
        
        for( BrowseHandlerFactory browseHandlerFactory : applicableBrowseHandlerFactories )
        {
            try
            {
                applicableBrowseHandlers.add( browseHandlerFactory.create() );
            }
            catch( Exception e )
            {
                SapphireUiFrameworkPlugin.log( e );
            }
        }
        
        
        return applicableBrowseHandlers;
    }
    
    private static synchronized void initialize()
    {
        if( browseHandlerFactories == null )
        {
            browseHandlerFactories = new ArrayList<BrowseHandlerFactory>();
            
            for( IConfigurationElement el : getTopLevelElements( findExtensions( SapphireUiFrameworkPlugin.PLUGIN_ID, "browseHandlers" ) ) )
            {
                final String pluginId = el.getNamespaceIdentifier();
                
                if( el.getName().equals( EL_BROWSE_HANDLER ) )
                {
                    try
                    {
                        final String clname = findRequiredAttribute( el, ATTR_FACTORY );
                        final BrowseHandlerFactory browseHandlerFactory = instantiate( pluginId, clname, BrowseHandlerFactory.class );
                        
                        if( browseHandlerFactory != null )
                        {
                            browseHandlerFactories.add( browseHandlerFactory );
                        }
                    }
                    catch( InvalidExtensionException e )
                    {
                        // Problem already reported to the user. Just need to continue gracefully.
                    }
                }
            }
        }
    }

}
