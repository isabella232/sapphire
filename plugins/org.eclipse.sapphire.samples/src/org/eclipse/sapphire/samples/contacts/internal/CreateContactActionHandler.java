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

import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.PropertyContentEvent;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.samples.contacts.Contact;
import org.eclipse.sapphire.samples.contacts.ContactsDatabase;
import org.eclipse.sapphire.ui.PropertyEditorPart;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class CreateContactActionHandler extends SapphireActionHandler
{
    private ValueProperty property;
    
    @Override
    public void init( final SapphireAction action,
                      final ActionHandlerDef def )
    {
        super.init( action, def );
        
        this.property = (ValueProperty) ( (PropertyEditorPart) action.getPart() ).getProperty();
        
        final Listener listener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                refreshEnablementState();
            }
        };
        
        final IModelElement element = getModelElement();
        
        element.nearest( ContactsDatabase.class ).attach( listener, "Contacts/Name" );
        element.attach( listener, this.property.getName() );
        
        refreshEnablementState();
        
        attach
        (
            new FilteredListener<DisposeEvent>()
            {
                @Override
                protected void handleTypedEvent( final DisposeEvent event )
                {
                    element.nearest( ContactsDatabase.class ).detach( listener, "Contacts/Name" );
                    element.detach( listener, CreateContactActionHandler.this.property.getName() );
                }
            }
        );
    }
    
    private void refreshEnablementState()
    {
        final IModelElement element = getModelElement();
        final String name = element.read( this.property ).getText();
        final ContactsDatabase cdb = element.nearest( ContactsDatabase.class );
        
        boolean enabled;
        
        if( name == null )
        {
            enabled = false;
        }
        else
        {
            enabled = true;
            
            for( Contact contact : cdb.getContacts() )
            {
                if( name.equals( contact.getName().getText() ) )
                {
                    enabled = false;
                    break;
                }
            }
        }
        
        setEnabled( enabled );
    }

    @Override
    protected Object run( final SapphireRenderingContext context )
    {
        final IModelElement element = getModelElement();
        final String name = element.read( this.property ).getText();
        final ContactsDatabase cdb = element.nearest( ContactsDatabase.class );
        
        final Contact newContact = cdb.getContacts().insert();
        newContact.setName( name );
        
        return null;
    }
    
}
