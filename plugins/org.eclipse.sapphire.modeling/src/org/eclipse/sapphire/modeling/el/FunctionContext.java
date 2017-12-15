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

package org.eclipse.sapphire.modeling.el;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.internal.SapphireModelingExtensionSystem;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.localization.SourceLanguageLocalizationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class FunctionContext
{
    private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
    
    public Object property( final String name )
    {
        throw new FunctionException( NLS.bind( Resources.undefinedPropertyMessage, name ) );
    }
    
    public Function function( final String name,
                              final List<Function> arguments )
    {
        final Function function = SapphireModelingExtensionSystem.createFunction( name, arguments.toArray( new Function[ arguments.size() ] ) );
        
        if( function != null )
        {
            return function;
        }
        
        throw new FunctionException( NLS.bind( Resources.undefinedFunctionMessage, name ) );
    }
    
    public LocalizationService getLocalizationService()
    {
        return SourceLanguageLocalizationService.INSTANCE;
    }
    
    public final void addListener( final Listener listener )
    {
        this.listeners.add( listener );
    }
    
    public final void removeListener( final Listener listener )
    {
        this.listeners.remove( listener );
    }
    
    protected final void notifyListeners( final String property )
    {
        for( Listener listener : this.listeners )
        {
            try
            {
                listener.handlePropertyChanged( property );
            }
            catch( Exception e )
            {
                SapphireModelingFrameworkPlugin.log( e );
            }
        }
    }
    
    public void dispose()
    {
    }
    
    public static abstract class Listener
    {
        public abstract void handlePropertyChanged( String property );
    }
    
    private static final class Resources extends NLS
    {
        public static String undefinedPropertyMessage;
        public static String undefinedFunctionMessage;
        
        static
        {
            initializeMessages( FunctionContext.class.getName(), Resources.class );
        }
    }
    
}
