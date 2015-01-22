/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.el;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.PropertyEvent;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.localization.SourceLanguageLocalizationService;
import org.eclipse.sapphire.services.PossibleTypesService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class ModelElementFunctionContext extends FunctionContext
{
    @Text( "Index {0} is outside the bounds of the collection." )
    private static LocalizableText indexOutOfBounds;
    
    static
    {
        LocalizableText.init( ModelElementFunctionContext.class );
    }

    private final Element element;
    private final LocalizationService localizationService;
    
    public ModelElementFunctionContext( final Element element )
    {
        this( element, SourceLanguageLocalizationService.INSTANCE );
    }
    
    public ModelElementFunctionContext( final Element element, final LocalizationService localizationService )
    {
        if( element == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.element = element;
        this.localizationService = localizationService;
    }
    
    public final Element element()
    {
        return this.element;
    }
    
    @Override
    public FunctionResult property( final Object element,
                                    final String name )
    {
        if( element == this && name.equalsIgnoreCase( "This" ) )
        {
            final Function f = new Function()
            {
                @Override
                public String name()
                {
                    return "This";
                }

                @Override
                public FunctionResult evaluate( final FunctionContext context )
                {
                    return new FunctionResult( this, context )
                    {
                        @Override
                        protected Object evaluate()
                        {
                            return element();
                        }
                    };
                }
            };
            
            f.init();
            
            return f.evaluate( this );
        }
        else if( element == this || element instanceof Element )
        {
            final Element el = ( element == this ? element() : (Element) element );
            final Property property = el.property( name );
            
            if( property != null )
            {
                final Function f = new ReadPropertyFunction( property, name, PropertyContentEvent.class )
                {
                    @Override
                    protected Object evaluate()
                    {
                        return this.context;
                    }
                };
                
                f.init();
                
                return f.evaluate( this );
            }
        }
        else if( element instanceof ElementHandle )
        {
            final ElementHandle<?> handle = (ElementHandle<?>) element;
            final ElementProperty elementPropertyDef = handle.definition();
            
            boolean ok = false;
            
            if( elementPropertyDef instanceof ImpliedElementProperty )
            {
                ok = ( elementPropertyDef.getType().property( name ) != null );
            }
            else
            {
                for( final ElementType possibleChildType : handle.service( PossibleTypesService.class ).types() )
                {
                    ok = ( possibleChildType.property( name ) != null );
                    
                    if( ok )
                    {
                        break;
                    }
                }
            }
            
            if( ok )
            {
                final Function f = new ReadPropertyFunction( handle, name, PropertyContentEvent.class )
                {
                    @Override
                    protected Object evaluate()
                    {
                        final Element child = ( (ElementHandle<?>) this.context ).content();
                        
                        if( child != null )
                        {
                            return child.property( name );
                        }
                        
                        return null;
                    }
                };
                
                f.init();
                
                return f.evaluate( this );
            }
        }
        else if( element instanceof ElementList )
        {
            final ElementList<?> list = (ElementList<?>) element;
            
            try
            {
                final int index = Integer.parseInt( name );
                
                final Function f = new ReadPropertyFunction( list, name, PropertyContentEvent.class )
                {
                    @Override
                    protected Object evaluate()
                    {
                        final ElementList<?> list = (ElementList<?>) this.context;
                        
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
                
                f.init();
                
                return f.evaluate( this );
            }
            catch( NumberFormatException e )
            {
                // Ignore. Non-integer property means call isn't trying to index into the list.
            }
        }
        
        return super.property( element, name );
    }

    @Override
    public LocalizationService getLocalizationService()
    {
        return this.localizationService;
    }
    
    private static abstract class ReadPropertyFunction extends Function
    {
        protected final Property context;
        private final String name;
        private final Class<? extends PropertyEvent> eventType;
        
        public ReadPropertyFunction( final Property context,
                                     final String name,
                                     final Class<? extends PropertyEvent> eventType )
        {
            this.context = context;
            this.name = name;
            this.eventType = eventType;
        }
        
        @Override
        public final String name()
        {
            return this.name;
        }

        @Override
        public final FunctionResult evaluate( final FunctionContext context )
        {
            final Property property = this.context;
            final Class<? extends PropertyEvent> eventType = this.eventType;
            
            return new FunctionResult( this, context )
            {
                private Listener listener;
                
                @Override
                protected void init()
                {
                    super.init();
                    
                    this.listener = new Listener()
                    {
                        @Override
                        public void handle( final Event event )
                        {
                            if( eventType.isInstance( event ) )
                            {
                                refresh();
                            }
                        }
                    };
                    
                    property.attach( this.listener );
                }

                @Override
                protected Object evaluate()
                {
                    return ReadPropertyFunction.this.evaluate();
                }
                
                @Override
                public void dispose()
                {
                    super.dispose();
                    property.detach( this.listener );
                }
            };
        }
        
        protected abstract Object evaluate();
    };
    
}
