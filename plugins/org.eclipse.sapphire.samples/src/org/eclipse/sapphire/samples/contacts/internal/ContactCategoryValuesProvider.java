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

import org.eclipse.sapphire.modeling.annotations.PossibleValuesProviderImpl;
import org.eclipse.sapphire.samples.contacts.IContact;
import org.eclipse.sapphire.samples.contacts.IContactsDatabase;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ContactCategoryValuesProvider

    extends PossibleValuesProviderImpl
    
{
    @Override
    protected void fillPossibleValues( final SortedSet<String> values )
    {
        values.add( "Personal" );
        
        final IContact c = (IContact) getModelElement();
        final IContactsDatabase cdb = (IContactsDatabase) c.getModel();
        
        for( IContact contact : cdb.getContacts() )
        {
            if( contact != c )
            {
                values.add( contact.getCategory().getText( true ) );
            }
        }
    }
    
}
