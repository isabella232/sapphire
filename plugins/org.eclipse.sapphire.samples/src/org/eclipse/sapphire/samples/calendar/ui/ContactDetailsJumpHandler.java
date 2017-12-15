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

package org.eclipse.sapphire.samples.calendar.ui;

import java.util.List;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.samples.calendar.integrated.IAttendee;
import org.eclipse.sapphire.samples.contacts.IContact;
import org.eclipse.sapphire.ui.SapphireJumpActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsContentNode;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsContentTree;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsPage;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ContactDetailsJumpHandler

    extends SapphireJumpActionHandler
    
{
    @Override
    protected void initDependencies( final List<String> dependencies )
    {
        super.initDependencies( dependencies );
        dependencies.add( IAttendee.PROP_IN_CONTACTS_DATABASE.getName() );
    }

    @Override
    protected void refreshEnablementState()
    {
        final IAttendee attendee = (IAttendee) getModelElement();
        setEnabled( attendee.isInContactsDatabase().getContent() );
    }

    @Override
    protected Object run( final SapphireRenderingContext context )
    {
        final CalendarEditor editor = context.getPart().getNearestPart( CalendarEditor.class );
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
            IContact contact = null;
            
            for( IContact c : editor.getContactsDatabase().getContacts() )
            {
                if( name.equals( c.getName().getText() ) )
                {
                    contact = c;
                    break;
                }
            }
            
            if( contact != null )
            {
                final MasterDetailsPage contactsFormPage = (MasterDetailsPage) editor.getPage( "Contacts" );
                final MasterDetailsContentTree content = contactsFormPage.getContentTree();
                final MasterDetailsContentNode contactNode = findContactNode( content.getRoot(), contact );
                
                if( contactNode != null )
                {
                    contactNode.select();
                    editor.showPage( "Contacts" );
                }
            }
        }
    }

    private static MasterDetailsContentNode findContactNode( final MasterDetailsContentNode node,
                                                             final IContact contact )
    {
        if( node.getModelElement() == contact )
        {
            return node;
        }

        for( MasterDetailsContentNode child : node.getChildNodes() )
        {
            final MasterDetailsContentNode res = findContactNode( child, contact );
            
            if( res != null )
            {
                return res;
            }
        }
        
        return null;
    }
    
}
