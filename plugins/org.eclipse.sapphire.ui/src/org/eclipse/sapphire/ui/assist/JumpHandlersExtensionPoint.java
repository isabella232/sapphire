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

public final class JumpHandlersExtensionPoint
{
    private static final String EL_JUMP_HANDLER = "jump-handler";
    private static final String ATTR_CLASS = "class";
    
    private static final Comparator<JumpHandler> COMPARATOR = new Comparator<JumpHandler>()
    {
        public int compare( final JumpHandler x,
                            final JumpHandler y )
        {
            int res = x.getPriority() - y.getPriority();
            
            if( res == 0 )
            {
                res = x.getClass().getName().compareTo( y.getClass().getName() );
            }
            
            return res;
        }
    };
    
    private static List<JumpHandler> jumpHandlers = null;
    
    public static JumpHandler getJumpHandler( final ValueProperty property )
    {
        initialize();
        
        final List<JumpHandler> candidates = new ArrayList<JumpHandler>();
        
        for( JumpHandler jumpHandler : jumpHandlers )
        {
            try
            {
                if( jumpHandler.isApplicable( property ) )
                {
                    candidates.add( jumpHandler );
                }
            }
            catch( Exception e )
            {
                SapphireUiFrameworkPlugin.log( e );
            }
        }
        
        if( candidates.isEmpty() )
        {
            return null;
        }
        else if( candidates.size() == 1 )
        {
            return candidates.get( 0 );
        }
        else
        {
            Collections.sort( candidates, COMPARATOR );
            return candidates.get( candidates.size() - 1 );
        }
    }
    
    private static synchronized void initialize()
    {
        if( jumpHandlers == null )
        {
            jumpHandlers = new ArrayList<JumpHandler>();
            
            for( IConfigurationElement el : getTopLevelElements( findExtensions( SapphireUiFrameworkPlugin.PLUGIN_ID, "jumpHandlers" ) ) )
            {
                final String pluginId = el.getNamespaceIdentifier();
                
                if( el.getName().equals( EL_JUMP_HANDLER ) )
                {
                    try
                    {
                        final String clname = findRequiredAttribute( el, ATTR_CLASS );
                        final JumpHandler jumpHandler = instantiate( pluginId, clname, JumpHandler.class );
                        
                        if( jumpHandler != null )
                        {
                            jumpHandlers.add( jumpHandler );
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
