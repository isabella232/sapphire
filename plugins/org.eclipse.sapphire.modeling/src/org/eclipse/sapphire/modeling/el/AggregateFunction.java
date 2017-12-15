/******************************************************************************
 * Copyright (c) 2012 Oracle
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

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.util.NLS;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class AggregateFunction extends Function
{
    protected static abstract class AggregateFunctionResult extends FunctionResult
    {
        private IModelElement lastListParentElement;
        private String lastListenerModelPath;
        private ModelPropertyListener listener;
        
        public AggregateFunctionResult( final Function function,
                                        final FunctionContext context )
        {
            super( function, context );
        }

        @Override
        protected final Object evaluate()
        {
            final Object collection = operand( 0 ).value();
            final List<Object> items = new ArrayList<Object>();
            
            if( collection != null )
            {
                if( collection instanceof ModelElementList )
                {
                    final ModelElementList<?> list = (ModelElementList<?>) collection;
                    final ListProperty listProperty = list.getParentProperty();
                    final ModelElementType listEntryType = listProperty.getType();
                    final ValueProperty listEntryProperty;
                    
                    if( operands().size() > 1 )
                    {
                        final String listEntryPropertyName = cast( operand( 1 ).value(), String.class );
                    
                        final ModelProperty prop = listEntryType.getProperty( listEntryPropertyName );
                        
                        if( prop == null )
                        {
                            throw new FunctionException( NLS.bind( Resources.missingProperty, listEntryType.getSimpleName(), listEntryPropertyName ) );
                        }
                        
                        if( ! ( prop instanceof ValueProperty ) )
                        {
                            throw new FunctionException( NLS.bind( Resources.notValueProperty, listEntryType.getSimpleName(), listEntryPropertyName ) );
                        }
                    
                        listEntryProperty = (ValueProperty) prop;
                    }
                    else
                    {
                        ValueProperty prop = null;
                        
                        for( ModelProperty p : listEntryType.getProperties() )
                        {
                            if( p instanceof ValueProperty )
                            {
                                prop = (ValueProperty) p;
                                break;
                            }
                        }
                        
                        if( prop == null )
                        {
                            throw new FunctionException( NLS.bind( Resources.noValueProperties, listEntryType.getSimpleName() ) );
                        }
                        
                        listEntryProperty = prop;
                    }
                    
                    for( IModelElement item : list )
                    {
                        items.add( item.read( listEntryProperty ).getContent() );
                    }
                    
                    final IModelElement listParentElement = list.parent();
                    final String listenerModelPath = listProperty.getName() + "/" + listEntryProperty.getName();
                    
                    if( this.lastListParentElement != listParentElement || ! this.lastListenerModelPath.equals( listenerModelPath ) )
                    {
                        if( this.lastListParentElement != null )
                        {
                            this.lastListParentElement.removeListener( this.listener, this.lastListenerModelPath );
                        }
                        
                        this.lastListParentElement = listParentElement;
                        this.lastListenerModelPath = listenerModelPath;
                        
                        if( this.listener == null )
                        {
                            this.listener = new ModelPropertyListener()
                            {
                                @Override
                                public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
                                {
                                    refresh();
                                }
                            };
                        }
                        
                        listParentElement.addListener( this.listener, listenerModelPath );
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
                this.lastListParentElement.removeListener( this.listener, this.lastListenerModelPath );
            }
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String missingProperty;
        public static String notValueProperty;
        public static String noValueProperties;
        
        static
        {
            initializeMessages( AggregateFunction.class.getName(), Resources.class );
        }
    }

}
