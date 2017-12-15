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

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.samples.calendar.integrated.IAttendee;
import org.eclipse.sapphire.samples.contacts.IContact;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.JumpHandler;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsContentNode;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsContentTree;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsPage;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ContactDetailsJumpHandler

    extends JumpHandler
    
{
    @Override
    public boolean isApplicable( final ValueProperty property )
    {
        return true;
    }
    
    @Override
    public boolean canLocateJumpTarget( final SapphirePart part,
                                        final SapphireRenderingContext context,
                                        final IModelElement modelElement,
                                        final ValueProperty property )
    {
        final IAttendee attendee = (IAttendee) modelElement;
        return attendee.isInContactsDatabase().getContent();
    }

    @Override
    public void jump( final SapphirePart part,
                      final SapphireRenderingContext context,
                      final IModelElement modelElement,
                      final ValueProperty property )
    {
        final CalendarEditor editor = part.getNearestPart( CalendarEditor.class );
        jump( editor, modelElement );
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
