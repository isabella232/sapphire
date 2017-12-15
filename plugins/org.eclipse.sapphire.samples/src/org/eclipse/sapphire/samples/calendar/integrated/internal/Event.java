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

import org.eclipse.sapphire.modeling.IModelParticle;
import org.eclipse.sapphire.modeling.LayeredModelElementListController;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementListController;
import org.eclipse.sapphire.modeling.ModelElementListener;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.samples.calendar.integrated.IAttendee;
import org.eclipse.sapphire.samples.calendar.integrated.IEventAttachment;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class Event

    extends EventStub
    
{
    private final org.eclipse.sapphire.samples.calendar.IEvent base;

    public Event( final IModelParticle parent,
                  final ModelProperty parentProperty,
                  final org.eclipse.sapphire.samples.calendar.IEvent base )
    {
        super( parent, parentProperty );
        
        this.base = base;
        
        final ModelElementListener listener = new ModelElementListener()
        {
            @Override
            public void propertyChanged( final ModelPropertyChangeEvent event )
            {
                final ModelProperty property = event.getProperty();
                
                if( property == org.eclipse.sapphire.samples.calendar.IEvent.PROP_SUBJECT )
                {
                    refresh( PROP_SUBJECT );
                }
                else if( property == org.eclipse.sapphire.samples.calendar.IEvent.PROP_LOCATION )
                {
                    refresh( PROP_LOCATION );
                }
                else if( property == org.eclipse.sapphire.samples.calendar.IEvent.PROP_NOTES )
                {
                    refresh( PROP_NOTES );
                }
                else if( property == org.eclipse.sapphire.samples.calendar.IEvent.PROP_START_TIME )
                {
                    refresh( PROP_START_TIME );
                }
                else if( property == org.eclipse.sapphire.samples.calendar.IEvent.PROP_END_TIME )
                {
                    refresh( PROP_END_TIME );
                }
                else if( property == org.eclipse.sapphire.samples.calendar.IEvent.PROP_ATTENDEES )
                {
                    refresh( PROP_ATTENDEES );
                }
                else if( property == org.eclipse.sapphire.samples.calendar.IEvent.PROP_ATTACHMENTS )
                {
                    refresh( PROP_ATTACHMENTS );
                }
            }
        };
        
        this.base.addListener( listener );
    }
    
    org.eclipse.sapphire.samples.calendar.IEvent getBase()
    {
        return this.base;
    }

    @Override
    protected String readSubject()
    {
        return this.base.getSubject().getText( false );
    }

    @Override
    protected void writeSubject( final String subject )
    {
        this.base.setSubject( subject );
    }

    @Override
    protected String readLocation()
    {
        return this.base.getLocation().getText( false );
    }

    @Override
    protected void writeLocation( final String location )
    {
        this.base.setLocation( location );
    }

    @Override
    protected String readNotes()
    {
        return this.base.getNotes().getText( false );
    }

    @Override
    protected void writeNotes( final String notes )
    {
        this.base.setNotes( notes );
    }

    @Override
    protected String readStartTime()
    {
        return this.base.getStartTime().getText( false );
    }

    @Override
    protected void writeStartTime( final String startTime )
    {
        this.base.setStartTime( startTime );
    }

    @Override
    protected String readEndTime()
    {
        return this.base.getEndTime().getText( false );
    }

    @Override
    protected void writeEndTime( final String endTime )
    {
        this.base.setEndTime( endTime );
    }

    @Override
    protected ModelElementList<IAttendee> initAttendees()
    {
        final ModelElementListController<IAttendee> controller = new LayeredModelElementListController<IAttendee,org.eclipse.sapphire.samples.calendar.IAttendee>()
        {
            private final ModelElementList<org.eclipse.sapphire.samples.calendar.IAttendee> base
                = Event.this.base.getAttendees();

            @Override
            public List<IAttendee> refresh( final List<IAttendee> contents )
            {
                this.base.refresh();
                return refresh( contents, this.base );
            }
            
            @Override
            protected IAttendee wrap( final org.eclipse.sapphire.samples.calendar.IAttendee obj )
            {
                return new Attendee( getList(), PROP_ATTENDEES, obj );
            }

            @Override
            protected org.eclipse.sapphire.samples.calendar.IAttendee unwrap( final IAttendee obj )
            {
                return ( (Attendee) obj ).getBase();
            }

            @Override
            public IAttendee createNewElement( final ModelElementType type )
            {
                return wrap( this.base.addNewElement( org.eclipse.sapphire.samples.calendar.IAttendee.TYPE ) );
            }

            @Override
            public void swap( final IAttendee a,
                              final IAttendee b )
            {
                this.base.swap( unwrap( a ), unwrap( b ) );
            }
        };
        
        final ModelElementList<IAttendee> list = new ModelElementList<IAttendee>( this, PROP_ATTENDEES );
        
        controller.init( this, PROP_ATTENDEES, list, new String[ 0 ] );
        list.init( controller );
        
        return list;
    }

    @Override
    protected ModelElementList<IEventAttachment> initAttachments()
    {
        final ModelElementListController<IEventAttachment> controller = new LayeredModelElementListController<IEventAttachment,org.eclipse.sapphire.samples.calendar.IEventAttachment>()
        {
            private final ModelElementList<org.eclipse.sapphire.samples.calendar.IEventAttachment> base
                = Event.this.base.getAttachments();

            @Override
            public List<IEventAttachment> refresh( final List<IEventAttachment> contents )
            {
                this.base.refresh();
                return refresh( contents, this.base );
            }
                    
            @Override
            protected IEventAttachment wrap( final org.eclipse.sapphire.samples.calendar.IEventAttachment obj )
            {
                return new EventAttachment( getList(), PROP_ATTACHMENTS, obj );
            }

            @Override
            protected org.eclipse.sapphire.samples.calendar.IEventAttachment unwrap( final IEventAttachment obj )
            {
                return ( (EventAttachment) obj ).getBase();
            }

            @Override
            public IEventAttachment createNewElement( final ModelElementType type )
            {
                return wrap( this.base.addNewElement( org.eclipse.sapphire.samples.calendar.IEventAttachment.TYPE ) );
            }

            @Override
            public void swap( final IEventAttachment a,
                              final IEventAttachment b )
            {
                this.base.swap( unwrap( a ), unwrap( b ) );
            }
        };
        
        final ModelElementList<IEventAttachment> list = new ModelElementList<IEventAttachment>( this, PROP_ATTACHMENTS );
        
        controller.init( this, PROP_ATTACHMENTS, list, new String[ 0 ] );
        list.init( controller );
        
        return list;
    }

    @Override
    protected void doRemove()
    {
        this.base.remove();
    }
    
}
