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

package org.eclipse.sapphire.samples.contacts.internal;

import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.samples.contacts.IContact;
import org.eclipse.sapphire.samples.contacts.IContactsDatabase;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class CreateContactActionHandler extends SapphireActionHandler
{
    private ValueProperty property;
    
    @Override
    public void init( final SapphireAction action,
                      final ISapphireActionHandlerDef def )
    {
        super.init( action, def );
        
        this.property = (ValueProperty) ( (SapphirePropertyEditor) action.getPart() ).getProperty();
        
        final ModelPropertyListener listener = new ModelPropertyListener()
        {
            @Override
            public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
            {
                refreshEnablementState();
            }
        };
        
        final IModelElement element = getModelElement();
        
        element.nearest( IContactsDatabase.class ).addListener( listener, "Contacts/Name" );
        element.addListener( listener, this.property.getName() );
        
        refreshEnablementState();
        
        attach
        (
            new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof DisposeEvent )
                    {
                        element.nearest( IContactsDatabase.class ).removeListener( listener, "Contacts/Name" );
                        element.removeListener( listener, CreateContactActionHandler.this.property.getName() );
                    }
                }
            }
        );
    }
    
    private void refreshEnablementState()
    {
        final IModelElement element = getModelElement();
        final String name = element.read( this.property ).getText();
        final IContactsDatabase cdb = element.nearest( IContactsDatabase.class );
        
        boolean enabled;
        
        if( name == null )
        {
            enabled = false;
        }
        else
        {
            enabled = true;
            
            for( IContact contact : cdb.getContacts() )
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
        final IContactsDatabase cdb = element.nearest( IContactsDatabase.class );
        
        final IContact newContact = cdb.getContacts().addNewElement();
        newContact.setName( name );
        
        return null;
    }
    
}
