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

package org.eclipse.sapphire.samples.calendar.ui;

import java.util.List;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.samples.calendar.integrated.IAttendee;
import org.eclipse.sapphire.samples.contacts.Contact;
import org.eclipse.sapphire.ui.SapphireJumpActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.form.editors.masterdetails.MasterDetailsContentNode;
import org.eclipse.sapphire.ui.form.editors.masterdetails.MasterDetailsContentOutline;
import org.eclipse.sapphire.ui.form.editors.masterdetails.MasterDetailsEditorPagePart;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ContactDetailsJumpHandler extends SapphireJumpActionHandler
{
    @Override
    protected void initDependencies( final List<String> dependencies )
    {
        super.initDependencies( dependencies );
        dependencies.add( IAttendee.PROP_IN_CONTACTS_DATABASE.getName() );
    }

    @Override
    protected boolean computeEnablementState()
    {
        if( super.computeEnablementState() == true )
        {
            final IAttendee attendee = (IAttendee) getModelElement();
            return attendee.isInContactsDatabase().getContent();
        }
        
        return false;
    }

    @Override
    protected Object run( final SapphireRenderingContext context )
    {
        final CalendarEditor editor = context.getPart().nearest( CalendarEditor.class );
        jump( editor, getModelElement() );
        return null;
    }

    public static void jump( final CalendarEditor editor,
                             final IModelElement modelElement )
    {
        final IAttendee attendee = (IAttendee) modelElement;
        final String name = attendee.getName().getText();
        
        if( name != null )
        {
            Contact contact = null;
            
            for( Contact c : editor.getContactsDatabase().getContacts() )
            {
                if( name.equals( c.getName().getText() ) )
                {
                    contact = c;
                    break;
                }
            }
            
            if( contact != null )
            {
                final MasterDetailsEditorPagePart contactsFormPage = (MasterDetailsEditorPagePart) editor.getEditorPagePart( "Contacts" );
                final MasterDetailsContentOutline outline = contactsFormPage.outline();
                final MasterDetailsContentNode contactNode = outline.getRoot().findNodeByModelElement( contact );
                
                if( contactNode != null )
                {
                    contactNode.select();
                    editor.showPage( contactsFormPage );
                }
            }
        }
    }
    
}
