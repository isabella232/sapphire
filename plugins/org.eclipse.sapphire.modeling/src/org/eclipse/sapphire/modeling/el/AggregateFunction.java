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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.ValueProperty;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class AggregateFunction extends Function
{
    @Text( "Property {0}.{1} could not be found." )
    private static LocalizableText missingProperty;
    
    @Text( "Property {0}.{1} is not a value property." )
    private static LocalizableText notValueProperty;
    
    @Text( "Element type {0} does not contain a value property." )
    private static LocalizableText noValueProperties;
    
    static
    {
        LocalizableText.init( AggregateFunction.class );
    }

    protected static abstract class AggregateFunctionResult extends FunctionResult
    {
        private Element lastListParentElement;
        private String lastListenerModelPath;
        private Listener listener;
        
        public AggregateFunctionResult( final Function function,
                                        final FunctionContext context )
        {
            super( function, context );
        }

        @Override
        protected final Object evaluate()
        {
            final Object collection = operand( 0 );
            final List<Object> items = new ArrayList<Object>();
            
            if( collection != null )
            {
                if( collection instanceof ElementList )
                {
                    final ElementList<?> list = (ElementList<?>) collection;
                    final ListProperty listProperty = list.definition();
                    final ElementType listEntryType = listProperty.getType();
                    final ValueProperty listEntryProperty;
                    
                    if( operands().size() > 1 )
                    {
                        final String listEntryPropertyName = cast( operand( 1 ), String.class );
                    
                        final PropertyDef prop = listEntryType.property( listEntryPropertyName );
                        
                        if( prop == null )
                        {
                            throw new FunctionException( missingProperty.format( listEntryType.getSimpleName(), listEntryPropertyName ) );
                        }
                        
                        if( ! ( prop instanceof ValueProperty ) )
                        {
                            throw new FunctionException( notValueProperty.format( listEntryType.getSimpleName(), listEntryPropertyName ) );
                        }
                    
                        listEntryProperty = (ValueProperty) prop;
                    }
                    else
                    {
                        ValueProperty prop = null;
                        
                        for( PropertyDef p : listEntryType.properties() )
                        {
                            if( p instanceof ValueProperty )
                            {
                                prop = (ValueProperty) p;
                                break;
                            }
                        }
                        
                        if( prop == null )
                        {
                            throw new FunctionException( noValueProperties.format( listEntryType.getSimpleName() ) );
                        }
                        
                        listEntryProperty = prop;
                    }
                    
                    for( Element item : list )
                    {
                        items.add( item.property( listEntryProperty ).content() );
                    }
                    
                    final Element listParentElement = list.element();
                    final String listenerModelPath = listProperty.name() + "/" + listEntryProperty.name();
                    
                    if( this.lastListParentElement != listParentElement || ! this.lastListenerModelPath.equals( listenerModelPath ) )
                    {
                        if( this.lastListParentElement != null )
                        {
                            this.lastListParentElement.detach( this.listener, this.lastListenerModelPath );
                        }
                        
                        this.lastListParentElement = listParentElement;
                        this.lastListenerModelPath = listenerModelPath;
                        
                        if( this.listener == null )
                        {
                            this.listener = new FilteredListener<PropertyContentEvent>()
                            {
                                @Override
                                protected void handleTypedEvent( final PropertyContentEvent event )
                                {
                                    refresh();
                                }
                            };
                        }
                        
                        listParentElement.attach( this.listener, listenerModelPath );
                    }
                }
                else if( collection instanceof Object[] )
                {
                    for( Object item : (Object[]) collection )
                    {
                        items.add( item );
                    }
                }
                else if( collection instanceof Collection )
                {
                    items.addAll( (Collection<?>) collection );
                }
            }
            
            return evaluate( items );
        }
        
        protected abstract Object evaluate( List<Object> items );        

        @Override
        public void dispose()
        {
            super.dispose();
            
            if( this.lastListParentElement != null )
            {
                this.lastListParentElement.detach( this.listener, this.lastListenerModelPath );
            }
        }
    }
    
}
