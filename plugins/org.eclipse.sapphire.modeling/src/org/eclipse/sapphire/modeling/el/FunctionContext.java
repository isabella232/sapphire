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

package org.eclipse.sapphire.modeling.el;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.sapphire.modeling.internal.SapphireModelingExtensionSystem;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.localization.SourceLanguageLocalizationService;
import org.eclipse.sapphire.modeling.util.NLS;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class FunctionContext
{
    public FunctionResult property( final Object element,
                                    final String name )
    {
        final Function f = new Function()
        {
            @Override
            public String name()
            {
                return "ReadProperty";
            }
            
            @Override
            public FunctionResult evaluate( final FunctionContext context )
            {
                return new FunctionResult( this, context )
                {
                    @Override
                    protected Object evaluate() throws FunctionException
                    {
                        if( element == null )
                        {
                            throw new FunctionException( Resources.cannotReadPropertiesFromNull );
                        }
                        else
                        {
                            if( element instanceof Collection && name.equalsIgnoreCase( "Size" ) )
                            {
                                return ( (Collection<?>) element ).size();
                            }
                            else if( element instanceof List )
                            {
                                try
                                {
                                    final int index = Integer.parseInt( name );
                                    final List<?> list = (List<?>) element;
                                    
                                    if( index >= 0 && index < list.size() )
                                    {
                                        return list.get( index );
                                    }
                                    else
                                    {
                                        throw new FunctionException( NLS.bind( Resources.indexOutOfBounds, index ) );
                                    }
                                }
                                catch( NumberFormatException e )
                                {
                                    // Ignore. Non-integer property means call isn't trying to index into the list.
                                }
                            }
                            else if( element.getClass().isArray() )
                            {
                                if( name.equalsIgnoreCase( "Size" ) )
                                {
                                    return Array.getLength( element );
                                }
                                else
                                {
                                    try
                                    {
                                        final int index = Integer.parseInt( name );
                                        
                                        if( index >= 0 && index < Array.getLength( element ) )
                                        {
                                            return Array.get( element, index );
                                        }
                                        else
                                        {
                                            throw new FunctionException( NLS.bind( Resources.indexOutOfBounds, index ) );
                                        }
                                    }
                                    catch( NumberFormatException e )
                                    {
                                        // Ignore. Non-integer property means call isn't trying to index into the list.
                                    }
                                }
                            }
                            else if( element instanceof Map )
                            {
                                final Map<?,?> map = (Map<?,?>) element;
                                
                                if( name.equalsIgnoreCase( "Size" ) )
                                {
                                    return map.size();
                                }
                                else
                                {
                                    for( final Map.Entry<?,?> entry : map.entrySet() )
                                    {
                                        final Object key = entry.getKey();
                                        
                                        if( key instanceof String )
                                        {
                                            if( ( (String) key ).equalsIgnoreCase( name ) )
                                            {
                                                return entry.getValue();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        if( element == FunctionContext.this )
                        {
                            throw new FunctionException( NLS.bind( Resources.undefinedPropertyMessage, name ) );
                        }
                        else
                        {
                            final Class<?> cl = element.getClass();
                            final String type;
                            
                            if( cl.isArray() )
                            {
                                type = cl.getComponentType().getName() + "[]";
                            }
                            else
                            {
                                type = cl.getName();
                            }
                            
                            throw new FunctionException( NLS.bind( Resources.undefinedPropertyMessageExt, name, type ) );
                        }
                    }
                };
            }
        };
        
        f.init();
        
        return f.evaluate( this );
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
    
    private static final class Resources extends NLS
    {
        public static String undefinedPropertyMessage;
        public static String undefinedPropertyMessageExt;
        public static String undefinedFunctionMessage;
        public static String cannotReadPropertiesFromNull;
        public static String indexOutOfBounds;
        
        static
        {
            initializeMessages( FunctionContext.class.getName(), Resources.class );
        }
    }
    
}
