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

package org.eclipse.sapphire.samples.calendar.integrated.internal;

import java.util.List;

import org.eclipse.sapphire.modeling.LayeredModelElementListController;
import org.eclipse.sapphire.modeling.LayeredModelStore;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementListController;
import org.eclipse.sapphire.modeling.ModelElementListener;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.annotations.GenerateStub;
import org.eclipse.sapphire.samples.calendar.integrated.IEvent;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateStub

public final class Calendar

    extends CalendarStub
    
{
    private final org.eclipse.sapphire.samples.calendar.ICalendar base;

    public Calendar( final LayeredModelStore modelStore )
    {
        super( modelStore );

        this.base = (org.eclipse.sapphire.samples.calendar.ICalendar) modelStore.getModel( 0 );
        
        final ModelElementListener listener = new ModelElementListener()
        {
            @Override
            public void propertyChanged( final ModelPropertyChangeEvent event )
            {
                final ModelProperty property = event.getProperty();
                
                if( property == org.eclipse.sapphire.samples.calendar.ICalendar.PROP_EVENTS )
                {
                    refresh( PROP_EVENTS );
                }
            }
        };
        
        this.base.addListener( listener );
    }
    
    @Override
    protected ModelElementList<IEvent> initEvents()
    {
        final ModelElementListController<IEvent> controller = new LayeredModelElementListController<IEvent,org.eclipse.sapphire.samples.calendar.IEvent>()
        {
            private final ModelElementList<org.eclipse.sapphire.samples.calendar.IEvent> base
                = Calendar.this.base.getEvents();

            @Override
            public List<IEvent> refresh( final List<IEvent> contents )
            {
                this.base.refresh();
                return refresh( contents, this.base );
            }
            
            @Override
            protected IEvent wrap( final org.eclipse.sapphire.samples.calendar.IEvent obj )
            {
                return new Event( getList(), PROP_EVENTS, obj );
            }

            @Override
            protected org.eclipse.sapphire.samples.calendar.IEvent unwrap( final IEvent obj )
            {
                return ( (Event) obj ).getBase();
            }

            @Override
            public IEvent createNewElement( final ModelElementType type )
            {
                return wrap( this.base.addNewElement( org.eclipse.sapphire.samples.calendar.IEvent.TYPE ) );
            }

            @Override
            public void swap( final IEvent a,
                              final IEvent b )
            {
                this.base.swap( unwrap( a ), unwrap( b ) );
            }
        };
        
        final ModelElementList<IEvent> list = new ModelElementList<IEvent>( this, PROP_EVENTS );
        
        controller.init( this, PROP_EVENTS, list, new String[ 0 ] );
        list.init( controller );
        
        return list;
    }
    
}
