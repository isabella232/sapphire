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

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.PossibleValuesService;
import org.eclipse.sapphire.samples.contacts.IAddress;
import org.eclipse.sapphire.samples.zipcodes.ZipCodesDatabase;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ZipCodeValuesProvider

    extends PossibleValuesService
    
{
    @Override
    protected void fillPossibleValues( final SortedSet<String> values )
    {
        final IAddress address = (IAddress) element();
        
        final String state = address.getState().getText();
        final String city = address.getCity().getText();
        
        values.addAll( ZipCodesDatabase.getZipCodes( state, city ) );
    }

    @Override
    public String getInvalidValueMessage( final String invalidValue )
    {
        return NLS.bind( "\"{0}\" is not a valid ZIP code for the specified city and state.", invalidValue );
    }

    @Override
    public boolean isCaseSensitive()
    {
        return false;
    }
    
}
