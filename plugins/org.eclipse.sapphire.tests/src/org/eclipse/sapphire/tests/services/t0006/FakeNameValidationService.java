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

package org.eclipse.sapphire.tests.services.t0006;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.modeling.PropertyContentEvent;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class FakeNameValidationService extends ValidationService
{
    @Override
    protected void init()
    {
        context( Contact.class ).attach
        (
            new FilteredListener<PropertyContentEvent>()
            {
                @Override
                protected void handleTypedEvent( final PropertyContentEvent event )
                {
                    if( event.property() == Contact.PROP_FIRST_NAME || event.property() == Contact.PROP_LAST_NAME )
                    {
                        broadcast();
                    }
                }
            }
        );
    }

    @Override
    public Status validate() 
    {
        final Contact contact = context( Contact.class );
        final String firstName = contact.getFirstName().getText();
        final String lastName = contact.getLastName().getText();
        
        if( firstName != null && lastName != null && firstName.equalsIgnoreCase( "John" ) && lastName.equals( "Doe" ) )
        {
            return Status.createWarningStatus( firstName + " " + lastName + " is likely a fake name." );
        }
        
        return Status.createOkStatus();
    }
    
}
