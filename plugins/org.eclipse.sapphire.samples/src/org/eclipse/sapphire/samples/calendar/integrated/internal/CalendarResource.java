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

package org.eclipse.sapphire.samples.calendar.integrated.internal;

import java.util.List;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.BindingImpl;
import org.eclipse.sapphire.modeling.LayeredListBindingImpl;
import org.eclipse.sapphire.modeling.ListBindingImpl;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.PropertyEvent;
import org.eclipse.sapphire.modeling.PropertyInitializationEvent;
import org.eclipse.sapphire.modeling.Resource;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.samples.calendar.integrated.ICalendar;
import org.eclipse.sapphire.samples.calendar.integrated.IEvent;
import org.eclipse.sapphire.samples.contacts.ContactsDatabase;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class CalendarResource extends Resource
{
    private final org.eclipse.sapphire.samples.calendar.ICalendar base;
    private final ContactsDatabase contacts;
    
    public CalendarResource( final org.eclipse.sapphire.samples.calendar.ICalendar base,
                             final ContactsDatabase contacts )
    {
        super( null );
        
        this.base = base;
        this.contacts = contacts;
        
        final Listener listener = new FilteredListener<PropertyEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyEvent event )
            {
                if( ! ( event instanceof PropertyInitializationEvent ) )
                {
                    final ModelProperty property = event.property();
                    
                    if( property == org.eclipse.sapphire.samples.calendar.ICalendar.PROP_EVENTS )
                    {
                        element().refresh( ICalendar.PROP_EVENTS );
                    }
                }
            }
        };
        
        this.base.attach( listener );
    }
    
    public org.eclipse.sapphire.samples.calendar.ICalendar getBase()
    {
        return this.base;
    }
    
    @Override
    @SuppressWarnings( "unchecked" )
    
    public <A> A adapt( final Class<A> adapterType )
    {
        A res;
        
        if( adapterType == ContactsDatabase.class )
        {
            res = (A) this.contacts;
        }
        else
        {
            res = super.adapt( adapterType );
            
            if( res == null )
            {
                res = this.base.adapt( adapterType );
            }
        }
        
        return res;
    }
    
    @Override
    protected BindingImpl createBinding( final ModelProperty property )
    {
        if( property == ICalendar.PROP_EVENTS )
        {
            final ListBindingImpl binding = new LayeredListBindingImpl()
            {
                private final ModelElementList<org.eclipse.sapphire.samples.calendar.IEvent> base
                    = CalendarResource.this.base.getEvents();
                
                @Override
                public ModelElementType type( final Resource resource )
                {
                    return IEvent.TYPE;
                }

                @Override
                protected Resource resource( final Object obj )
                {
                    return new EventResource( CalendarResource.this, (org.eclipse.sapphire.samples.calendar.IEvent) obj );
                }
                
                @Override
                protected List<?> readUnderlyingList()
                {
                    return this.base;
                }

                @Override
                protected Object insertUnderlyingObject( final ModelElementType type,
                                                         final int position )
                {
                    return this.base.insert( org.eclipse.sapphire.samples.calendar.IEvent.TYPE, position );
                }

                @Override
                public void move( final Resource resource, 
                                  final int position )
                {
                    this.base.move( ( (EventResource) resource ).getBase(), position );
                }

                @Override
                public void remove( final Resource resource )
                {
                    this.base.remove( ( (EventResource) resource ).getBase() );
                }
            };
            
            binding.init( element(), ICalendar.PROP_EVENTS, null );
            
            return binding;
        }
        
        return null;
    }
    
    @Override
    
    public void save()
    
        throws ResourceStoreException
        
    {
        this.base.resource().save();
        this.contacts.resource().save();
    }
    
}
