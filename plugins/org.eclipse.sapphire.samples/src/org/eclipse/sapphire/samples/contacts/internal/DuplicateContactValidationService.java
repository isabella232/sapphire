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

package org.eclipse.sapphire.samples.contacts.internal;

import static org.eclipse.sapphire.modeling.Status.createOkStatus;
import static org.eclipse.sapphire.modeling.Status.createWarningStatus;
import static org.eclipse.sapphire.modeling.util.MiscUtil.equal;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.PropertyContentEvent;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.samples.contacts.Contact;
import org.eclipse.sapphire.samples.contacts.ContactRepository;
import org.eclipse.sapphire.samples.contacts.PhoneNumber;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class DuplicateContactValidationService extends ValidationService
{
    private Listener listener;
    
    @Override
    protected void init()
    {
        final ContactRepository contacts = context( Contact.class ).nearest( ContactRepository.class );
        
        if( contacts != null )
        {
            this.listener = new FilteredListener<PropertyContentEvent>()
            {
                @Override
                protected void handleTypedEvent( final PropertyContentEvent event )
                {
                    broadcast();
                }
            };
            
            contacts.attach( this.listener, "Contacts/EMail" );
            contacts.attach( this.listener, "Contacts/PhoneNumbers/*" );
        }
    }

    @Override
    public Status validate() 
    {
        final Contact contact = context( Contact.class );
        final ContactRepository contacts = contact.nearest( ContactRepository.class );
        
        if( contacts != null )
        {
            final String email = contact.getEMail().getContent();
            final ModelElementList<PhoneNumber> numbers = contact.getPhoneNumbers();
            
            if( email != null && ! numbers.isEmpty() )
            {
                for( Contact x : contacts.getContacts() )
                {
                    if( x != contact && email.equals( x.getEMail().getContent() ) && ! x.getPhoneNumbers().isEmpty() )
                    {
                        for( PhoneNumber cn : numbers )
                        {
                            for( PhoneNumber xn : x.getPhoneNumbers() )
                            {
                                if( equal( cn.getAreaCode().getContent(), xn.getAreaCode().getContent() ) &&
                                    equal( cn.getLocalNumber().getContent(), xn.getLocalNumber().getContent() ) )
                                {
                                    final String msg = NLS.bind( Resources.likelyDuplicate, x.getName() );
                                    return createWarningStatus( msg );
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return createOkStatus();
    }
    
    @Override
    public void dispose()
    {
        final ContactRepository contacts = context( Contact.class ).nearest( ContactRepository.class );
        
        if( contacts != null )
        {
            contacts.detach( this.listener, "Contacts/EMail" );
            contacts.detach( this.listener, "Contacts/PhoneNumbers/*" );
        }
    }

    private static final class Resources extends NLS
    {
        public static String likelyDuplicate;
        
        static
        {
            initializeMessages( DuplicateContactValidationService.class.getName(), Resources.class );
        }
    }
    
}
