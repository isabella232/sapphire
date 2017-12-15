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

package org.eclipse.sapphire.samples.calendar.integrated.internal;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.samples.calendar.integrated.IAttendee;
import org.eclipse.sapphire.samples.contacts.IContact;
import org.eclipse.sapphire.services.ImageService;
import org.eclipse.sapphire.services.ImageServiceData;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class AttendeeImageService extends ImageService
{
    private static final ImageServiceData IMG_PERSON = new ImageServiceData( ImageData.readFromClassLoader( IContact.class, "Contact.png" ) );
    private static final ImageServiceData IMG_PERSON_FADED = new ImageServiceData( ImageData.readFromClassLoader( IContact.class, "ContactFaded.png" ) );
    
    private ModelPropertyListener listener;
    
    @Override
    protected void initImageService()
    {
        this.listener = new ModelPropertyListener()
        {
            @Override
            public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
            {
                refresh();
            }
        };
        
        context( IModelElement.class ).addListener( this.listener, IAttendee.PROP_IN_CONTACTS_DATABASE.getName() );
    }

    @Override
    protected ImageServiceData compute()
    {
        if( context( IAttendee.class ).isInContactsDatabase().getContent() )
        {
            return IMG_PERSON;
        }
        else
        {
            return IMG_PERSON_FADED;
        }
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        context( IModelElement.class ).removeListener( this.listener, IAttendee.PROP_IN_CONTACTS_DATABASE.getName() );
    }
    
}
