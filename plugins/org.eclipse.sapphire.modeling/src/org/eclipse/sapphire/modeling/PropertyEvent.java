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

package org.eclipse.sapphire.modeling;

import java.util.Map;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class PropertyEvent extends Event
{
    private final IModelElement element;
    private final ModelProperty property;
    
    protected PropertyEvent( final IModelElement element,
                             final ModelProperty property )
    {
        this.element = element;
        this.property = property;
    }
    
    public final IModelElement element()
    {
        return this.element;
    }
    
    public final ModelProperty property()
    {
        return this.property;
    }
    
    @Override
    public Map<String,String> fillTracingInfo( final Map<String,String> info )
    {
        super.fillTracingInfo( info );
        
        info.put( "element", element().type().getQualifiedName() + '(' + System.identityHashCode( element() ) + ')' );
        info.put( "property", property().getName() );
        
        return info;
    }
    
    public static final Listener filter( final Listener listener )
    {
        return Event.filter( listener, PropertyEvent.class );
    }

    public static final Listener filter( final Listener listener,
                                         final ModelProperty property )
    {
        return new FilteredListener( listener, property );
    }
    
    private static final class FilteredListener extends Listener
    {
        private final Listener listener;
        private final ModelProperty property;
        
        public FilteredListener( final Listener listener,
                                 final ModelProperty property )
        {
            this.listener = listener;
            this.property = property;
        }
        
        @Override
        public void handle( final Event event )
        {
            if( event instanceof PropertyEvent && ( (PropertyEvent) event ).property() == this.property )
            {
                this.listener.handle( event );
            }
        }
        
        @Override
        public boolean equals( final Object obj )
        {
            if( obj instanceof FilteredListener )
            {
                final FilteredListener x = (FilteredListener) obj;
                return ( this.listener.equals( x.listener ) && this.property == x.property );
            }
            
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return this.listener.hashCode() ^ this.property.hashCode();
        }
    }
    

}
