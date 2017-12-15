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

import org.eclipse.sapphire.samples.contacts.IAddress;
import org.eclipse.sapphire.samples.contacts.IContact;
import org.eclipse.sapphire.samples.contacts.IPhoneNumber;
import org.eclipse.sapphire.samples.contacts.ISendContactOp;
import org.eclipse.sapphire.services.DerivedValueServiceData;
import org.eclipse.sapphire.services.DerivedValueService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SendContactOpMessageBodyDerivedValueService extends DerivedValueService
{
    @Override
    protected DerivedValueServiceData compute()
    {
        final ISendContactOp op = context( ISendContactOp.class );
        final IContact contact = op.getContact().content();
        final StringBuilder buf = new StringBuilder();
        
        buf.append( "<html><body>\n" );
        
        if( contact != null )
        {
            buf.append( "<b>" );
            buf.append( contact.getName().getText() );
            buf.append( "</b>\n" );
            buf.append( "<br/><hr/>\n" );
            
            if( ! contact.getPhoneNumbers().isEmpty() )
            {
                buf.append( "<p><table>\n" );
                
                for( IPhoneNumber phone : contact.getPhoneNumbers() )
                {
                    buf.append( "<tr><td><i>" );
                    buf.append( phone.getType().getText() );
                    buf.append( "</i></td><td>" );
                    
                    final String areaCode = phone.getAreaCode().getText();
                    final String localNumber = phone.getLocalNumber().getText();
                    
                    if( areaCode != null )
                    {
                        buf.append( '(' );
                        buf.append( areaCode );
                        buf.append( ") " );
                    }
                    
                    buf.append( localNumber );
                    
                    buf.append( "</td></tr>\n" );
                }
                
                buf.append( "</table></p>\n" );
            }
            
            final IAddress address = contact.getAddress();
            
            if( address != null && address.getStreet().getContent() != null )
            {
                buf.append( "<p>" );
                buf.append( address.getStreet().getText() );
                buf.append( "<br/>" );
                buf.append( address.getCity().getText() );
                buf.append( ", " );
                buf.append( address.getState().getText() );
                buf.append( ' ' );
                buf.append( address.getZipCode().getText() );
                buf.append( "</p>\n" );
            }
        }
        
        buf.append( "</body></html>" );
        
        return new DerivedValueServiceData( buf.toString() );
    }

}
