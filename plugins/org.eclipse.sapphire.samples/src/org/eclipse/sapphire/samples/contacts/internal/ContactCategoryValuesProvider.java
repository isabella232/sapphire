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

import java.util.SortedSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.sapphire.modeling.PossibleValuesService;
import org.eclipse.sapphire.samples.contacts.IContact;
import org.eclipse.sapphire.samples.contacts.IContactsDatabase;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ContactCategoryValuesProvider

    extends PossibleValuesService
    
{
    @Override
    protected void fillPossibleValues( final SortedSet<String> values )
    {
        values.add( "Personal" );
        
        final IContact c = (IContact) element();
        final IContactsDatabase cdb = c.nearest( IContactsDatabase.class );
        
        for( IContact contact : cdb.getContacts() )
        {
            if( contact != c )
            {
                values.add( contact.getCategory().getText( true ) );
            }
        }
    }

    @Override
    public int getInvalidValueSeverity( String invalidValue )
    {
        return IStatus.OK;
    }
    
}
