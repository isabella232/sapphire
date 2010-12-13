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

package org.eclipse.sapphire.samples.contacts.internal;

import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.samples.contacts.IContact;
import org.eclipse.sapphire.samples.contacts.IPhoneNumber;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ContactMethods
{
    public static void removePhoneNumbersByAreaCode( final IContact contact,
                                                     final String areaCode )
    {
        final ModelElementList<IPhoneNumber> list = contact.getPhoneNumbers();
        
        for( IPhoneNumber pn : list )
        {
            if( areaCode.equals( pn.getAreaCode().getText() ) )
            {
                list.remove( pn );
            }
        }
    }
}
