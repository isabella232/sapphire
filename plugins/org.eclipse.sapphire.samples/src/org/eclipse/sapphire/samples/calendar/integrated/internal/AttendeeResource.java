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

import org.eclipse.sapphire.modeling.BindingImpl;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementListener;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.Resource;
import org.eclipse.sapphire.modeling.ValueBindingImpl;
import org.eclipse.sapphire.samples.calendar.integrated.IAttendee;
import org.eclipse.sapphire.samples.contacts.IContact;
import org.eclipse.sapphire.samples.contacts.IContactsDatabase;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class AttendeeResource

    extends Resource
    
{
    private final org.eclipse.sapphire.samples.calendar.IAttendee base;
    private final IContactsDatabase contacts;
    private final ModelElementListener listener;
    
    public AttendeeResource( final Resource parent,
                             final org.eclipse.sapphire.samples.calendar.IAttendee base )
    {
        super( parent );
        
        this.base = base;
        this.contacts = adapt( IContactsDatabase.class );
        
        this.listener = new ModelElementListener()
        {
            @Override
            public void propertyChanged( final ModelPropertyChangeEvent event )
            {
                final ModelProperty property = event.getProperty();
                final IModelElement element = element();
                
                if( property == org.eclipse.sapphire.samples.calendar.IAttendee.PROP_NAME )
                {
                    element.refresh( IAttendee.PROP_NAME );
                    element.refresh( IAttendee.PROP_IN_CONTACTS_DATABASE );
                    element.refresh( IAttendee.PROP_E_MAIL );
                }
                else if( property == org.eclipse.sapphire.samples.calendar.IAttendee.PROP_TYPE )
                {
                    element.refresh( IAttendee.PROP_TYPE );
                }
                else if( property == IContactsDatabase.PROP_CONTACTS )
                {
                    element.refresh( IAttendee.PROP_IN_CONTACTS_DATABASE );
                    element.refresh( IAttendee.PROP_E_MAIL );
                }
                else if( property == IContact.PROP_NAME )
                {
                    element.refresh( IAttendee.PROP_IN_CONTACTS_DATABASE );
                    element.refresh( IAttendee.PROP_E_MAIL );
                }
                else if( property == IContact.PROP_E_MAIL )
                {
                    element.refresh( IAttendee.PROP_E_MAIL );
                }
            }
        };
        
        this.base.addListener( this.listener );
        this.contacts.addListener( this.listener );
    }
    
    public org.eclipse.sapphire.samples.calendar.IAttendee getBase()
    {
        return this.base;
    }
    
    @Override
    protected BindingImpl createBinding( final ModelProperty property )
    {
        if( property == IAttendee.PROP_NAME )
        {
            return new ValueBindingImpl()
            {
                @Override
                public String read()
                {
                    return getBase().getName().getText( false );
                }
                
                @Override
                public void write( final String value )
                {
                    getBase().setName( value );
                }
            };
        }
        else if( property == IAttendee.PROP_TYPE )
        {
            return new ValueBindingImpl()
            {
                @Override
                public String read()
                {
                    return getBase().getType().getText( false );
                }
                
                @Override
                public void write( final String value )
                {
                    getBase().setType( value );
                }
            };
        }
        else if( property == IAttendee.PROP_E_MAIL )
        {
            return new ValueBindingImpl()
            {
                @Override
                public String read()
                {
                    final IContact c = findContactRecord( false );
                    return ( c != null ? c.getEMail().getText() : null );
                }
                
                @Override
                public void write( final String value )
                {
                    final IContact c = findContactRecord( value != null );
                    
                    if( c != null )
                    {
                        c.setEMail( value );
                    }
                }
            };
        }
        else if( property == IAttendee.PROP_IN_CONTACTS_DATABASE )
        {
            return new ValueBindingImpl()
            {
                @Override
                public String read()
                {
                    return ( findContactRecord( false ) != null ? Boolean.TRUE.toString() : null );
                }
                
                @Override
                public void write( final String value )
                {
                    throw new UnsupportedOperationException();
                }
            };
        }
        
        return null;
    }

    private IContact findContactRecord( final boolean createIfNecessary )
    {
        IContact c = null;
        final String name = this.base.getName().getText();
        
        if( name != null )
        {
            for( IContact contact : this.contacts.getContacts() )
            {
                if( name.equals( contact.getName().getText() ) )
                {
                    c = contact;
                    break;
                }
            }
    
            if( c == null && createIfNecessary )
            {
                c = this.contacts.getContacts().addNewElement();
                c.setName( name );
            }
    
            for( IContact contact : this.contacts.getContacts() )
            {
                contact.addListener( this.listener );
            }
        }
        
        return c;
    }
    
}
