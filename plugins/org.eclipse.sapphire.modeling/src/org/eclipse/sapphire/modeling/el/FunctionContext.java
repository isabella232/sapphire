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

package org.eclipse.sapphire.modeling.el;

import static org.eclipse.sapphire.util.CollectionsUtil.findIgnoringCase;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.ObservableMap;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.internal.SapphireModelingExtensionSystem;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.localization.SourceLanguageLocalizationService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class FunctionContext
{
    @Text( "Property \"{0}\" is undefined." )
    private static LocalizableText undefinedPropertyMessage;
    
    @Text( "Property \"{0}\" is undefined for {1} objects." )
    private static LocalizableText undefinedPropertyMessageExt;
    
    @Text( "Cannot read properties from null object." )
    private static LocalizableText cannotReadPropertiesFromNull;
    
    @Text( "Index {0} is outside the bounds of the collection." )
    private static LocalizableText indexOutOfBounds;
    
    static
    {
        LocalizableText.init( FunctionContext.class );
    }

    public FunctionResult property( final Object element, final String name )
    {
        if( element == null )
        {
            throw new FunctionException( cannotReadPropertiesFromNull.text() );
        }
        else
        {
            if( element instanceof Map )
            {
                final Map<?,?> map = (Map<?,?>) element;
                final Function f;
                
                if( map instanceof ObservableMap )
                {
                    final ObservableMap<?,?> observable = (ObservableMap<?,?>) map;
                    
                    f = new Function()
                    {
                        @Override
                        public String name()
                        {
                            return name;
                        }
    
                        @Override
                        public FunctionResult evaluate( final FunctionContext context )
                        {
                            return new FunctionResult( this, context )
                            {
                                private Listener listener;
                                
                                @Override
                                protected void init()
                                {
                                    this.listener = new Listener()
                                    {
                                        @Override
                                        public void handle( final Event event )
                                        {
                                            refresh();
                                        }
                                    };
                                    
                                    observable.attach( this.listener );
                                }

                                @Override
                                protected Object evaluate()
                                {
                                    return findIgnoringCase( observable, name );
                                }
                                
                                @Override
                                public void dispose()
                                {
                                    super.dispose();
                                    
                                    observable.detach( this.listener );
                                }
                            };
                        }
                    };
                }
                else
                {
                    f = new Function()
                    {
                        @Override
                        public String name()
                        {
                            return name;
                        }

                        @Override
                        public FunctionResult evaluate( final FunctionContext context )
                        {
                            return new FunctionResult( this, context )
                            {
                                @Override
                                protected Object evaluate()
                                {
                                    return findIgnoringCase( map, name );
                                }
                            };
                        }
                    };
                }
                
                f.init();
                
                return f.evaluate( this );
            }
            else
            {
                try
                {
                    final int index = Integer.parseInt( name );
                    
                    if( element instanceof List )
                    {
                        final List<?> list = (List<?>) element;
                        
                        final Function f = new Function()
                        {
                            @Override
                            public String name()
                            {
                                return name;
                            }
        
                            @Override
                            public FunctionResult evaluate( final FunctionContext context )
                            {
                                return new FunctionResult( this, context )
                                {
                                    @Override
                                    protected Object evaluate()
                                    {
                                        if( index >= 0 && index < list.size() )
                                        {
                                            return list.get( index );
                                        }
                                        else
                                        {
                                            throw new FunctionException( indexOutOfBounds.format( index ) );
                                        }
                                    }
                                };
                            }
                        };
                        
                        f.init();
                        
                        return f.evaluate( this );
                    }
                    else if( element.getClass().isArray() )
                    {
                        final Function f = new Function()
                        {
                            @Override
                            public String name()
                            {
                                return name;
                            }
        
                            @Override
                            public FunctionResult evaluate( final FunctionContext context )
                            {
                                return new FunctionResult( this, context )
                                {
                                    @Override
                                    protected Object evaluate()
                                    {
                                        if( index >= 0 && index < Array.getLength( element ) )
                                        {
                                            return Array.get( element, index );
                                        }
                                        else
                                        {
                                            throw new FunctionException( indexOutOfBounds.format( index ) );
                                        }
                                    }
                                };
                            }
                        };
                        
                        f.init();
                        
                        return f.evaluate( this );
                    }
                }
                catch( NumberFormatException e )
                {
                    // Ignore. Non-integer property means call isn't trying to index into a collection.
                }
            }
        }
        
        if( element == this )
        {
            if( ! SapphireModelingExtensionSystem.functions( name, 0 ).isEmpty() )
            {
                final Function f = new DeferredFunction( name );
                f.init( new Function[ 0 ] );
                return f.evaluate( this );
            }
        }
        else
        {
            if( ! SapphireModelingExtensionSystem.functions( name, 1 ).isEmpty() )
            {
                final Function f = new DeferredFunction( name );
                f.init( new Function[] { Literal.create( element ) } );
                return f.evaluate( this );
            }
        }
        
        if( element == this )
        {
            throw new FunctionException( undefinedPropertyMessage.format( name ) );
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
            
            throw new FunctionException( undefinedPropertyMessageExt.format( name, type ) );
        }
    }
    
    public LocalizationService getLocalizationService()
    {
        return SourceLanguageLocalizationService.INSTANCE;
    }
    
}
