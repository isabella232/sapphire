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

package org.eclipse.sapphire.samples.address.internal;

import java.util.SortedSet;

import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.samples.address.Address;
import org.eclipse.sapphire.samples.zipcodes.ZipCodesDatabase;
import org.eclipse.sapphire.services.PossibleValuesService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StateCodePossibleValuesService extends PossibleValuesService
{
    @Override
    protected void fillPossibleValues( final SortedSet<String> values )
    {
        final Address address = context( Address.class );
        
        final String zipCode = address.getZipCode().getText();
        final String city = address.getCity().getText();
        
        values.addAll( ZipCodesDatabase.getStateCodes( zipCode, city ) );
    }

    @Override
    public String getInvalidValueMessage( final String invalidValue )
    {
        return NLS.bind( "\"{0}\" is not a valid state postal code for the specified city and ZIP code.", invalidValue );
    }

    @Override
    public boolean isCaseSensitive()
    {
        return false;
    }
    
}
