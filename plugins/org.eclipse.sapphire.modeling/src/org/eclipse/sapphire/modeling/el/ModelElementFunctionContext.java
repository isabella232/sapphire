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

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.PropertyEvent;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.localization.SourceLanguageLocalizationService;
import org.eclipse.sapphire.modeling.util.NLS;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class ModelElementFunctionContext extends FunctionContext
{
    private final IModelElement element;
    private final LocalizationService localizationService;
    
    public ModelElementFunctionContext( final IModelElement element )
    {
        this( element, SourceLanguageLocalizationService.INSTANCE );
    }
    
    public ModelElementFunctionContext( final IModelElement element,
                                        final LocalizationService localizationService )
    {
        this.element = element;
        this.localizationService = localizationService;
    }
    
    public final IModelElement element()
    {
        return this.element;
    }
    
    @Override
    public FunctionResult property( final Object element,
                                    final String name )
    {
        if( element == this || element instanceof IModelElement )
        {
            final IModelElement el = ( element == this ? element() : (IModelElement) element );
            final ModelElementType type = el.type();
            final ModelProperty property = type.property( name );
            
            if( property != null )
            {
                final Function f = new ReadPropertyFunction( el, property )
                {
                    @Override
                    protected Object evaluate()
                    {
                        Object res = this.element.read( this.property );
                        
                        if( res instanceof ModelElementHandle<?> )
                        {
                            res = ( (ModelElementHandle<?>) res ).element();
                        }
                        
                        return res;
                    }
                };
                
                f.init();
                
                return f.evaluate( this );
            }
        }
        else if( element instanceof ModelElementList )
        {
            final ModelElementList<?> list = (ModelElementList<?>) element;
            
            if( name.equalsIgnoreCase( "Size" ) )
            {
                final Function f = new ReadPropertyFunction( list.parent(), list.getParentProperty() )
                {
                    @Override
                    protected Object evaluate()
                    {
                        return this.element.read( (ListProperty) this.property ).size();
                    }
                };
                
                f.init();
                
                return f.evaluate( this );
            }
            else
            {
                try
                {
                    final int index = Integer.parseInt( name );
                    
                    final Function f = new ReadPropertyFunction( list.parent(), list.getParentProperty() )
                    {
                        @Override
                        protected Object evaluate()
                        {
                            final ModelElementList<?> list = this.element.read( (ListProperty) this.property );
                            
                            if( index >= 0 && index < list.size() )
                            {
                                return list.get( index );
                            }
                            else
                            {
                                throw new FunctionException( NLS.bind( Resources.indexOutOfBounds, index ) );
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
        protected final IModelElement element;
        protected final ModelProperty property;
        
        public ReadPropertyFunction( final IModelElement element,
                                     final ModelProperty property )
        {
            this.element = element;
            this.property = property;
        }
        
        @Override
        public final String name()
        {
            return "ReadProperty";
        }

        @Override
        public final FunctionResult evaluate( final FunctionContext context )
        {
            final IModelElement element = this.element;
            final ModelProperty property = this.property;
            
            return new FunctionResult( this, context )
            {
                private Listener listener;
                
                @Override
                protected void init()
                {
                    super.init();
                    
                    this.listener = new FilteredListener<PropertyEvent>()
                    {
                        @Override
                        protected void handleTypedEvent( final PropertyEvent event )
                        {
                            refresh();
                        }
                    };
                    
                    element.attach( this.listener, property );
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
                    element.detach( this.listener, property );
                }
            };
        }
        
        protected abstract Object evaluate();
    };
    
    private static final class Resources extends NLS
    {
        public static String indexOutOfBounds;
        
        static
        {
            initializeMessages( ModelElementFunctionContext.class.getName(), Resources.class );
        }
    }
    
}
