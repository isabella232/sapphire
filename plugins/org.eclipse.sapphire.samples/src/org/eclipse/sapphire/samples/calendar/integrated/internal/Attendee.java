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

import org.eclipse.sapphire.modeling.IModelParticle;
import org.eclipse.sapphire.modeling.LayeredModelStore;
import org.eclipse.sapphire.modeling.ModelElementListener;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.samples.contacts.IContact;
import org.eclipse.sapphire.samples.contacts.IContactsDatabase;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class Attendee

    extends AttendeeStub
    
{
    private final org.eclipse.sapphire.samples.calendar.IAttendee base;
    private final IContactsDatabase contacts;
    private final ModelElementListener listener;
    
    public Attendee( final IModelParticle parent,
                     final ModelProperty parentProperty,
                     final org.eclipse.sapphire.samples.calendar.IAttendee base )
    {
        super( parent, parentProperty );
        
        this.base = base;
        this.contacts = (IContactsDatabase) ( (LayeredModelStore) getModel().getModelStore() ).getModel( 1 );
        
        this.listener = new ModelElementListener()
        {
            @Override
            public void propertyChanged( final ModelPropertyChangeEvent event )
            {
                final ModelProperty property = event.getProperty();
                
                if( property == org.eclipse.sapphire.samples.calendar.IAttendee.PROP_NAME )
                {
                    refresh( PROP_NAME );
                    refresh( PROP_IN_CONTACTS_DATABASE );
                    refresh( PROP_E_MAIL );
                }
                else if( property == org.eclipse.sapphire.samples.calendar.IAttendee.PROP_TYPE )
                {
                    refresh( PROP_TYPE );
                }
                else if( property == IContactsDatabase.PROP_CONTACTS )
                {
                    refresh( PROP_IN_CONTACTS_DATABASE );
                    refresh( PROP_E_MAIL );
                }
                else if( property == IContact.PROP_NAME )
                {
                    refresh( PROP_IN_CONTACTS_DATABASE );
                    refresh( PROP_E_MAIL );
                }
                else if( property == IContact.PROP_E_MAIL )
                {
                    refresh( PROP_E_MAIL );
                }
            }
        };
        
        this.base.addListener( this.listener );
        this.contacts.addListener( this.listener );
    }
    
    org.eclipse.sapphire.samples.calendar.IAttendee getBase()
    {
        return this.base;
    }

    @Override
    protected String readName()
    {
        return this.base.getName().getText( false );
    }

    @Override
    protected void writeName( final String name )
    {
        this.base.setName( name );
    }

    @Override
    protected String readType()
    {
        return this.base.getType().getText( false );
    }

    @Override
    protected void writeType( final String type )
    {
        this.base.setType( type );
    }

    @Override
    protected String readInContactsDatabase()
    {
        return ( findContactRecord( false ) != null ? Boolean.TRUE.toString() : null );
    }

    @Override
    protected String readEMail()
    {
        final IContact c = findContactRecord( false );
        return ( c != null ? c.getEMail().getText() : null );
    }

    @Override
    protected void writeEMail( final String eMail )
    {
        final IContact c = findContactRecord( eMail != null );
        
        if( c != null )
        {
            c.setEMail( eMail );
        }
    }

    @Override
    protected void doRemove()
    {
        this.base.remove();
    }

    private IContact findContactRecord( final boolean createIfNecessary )
    {
        IContact c = null;
        final String name = getName().getText();
        
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
