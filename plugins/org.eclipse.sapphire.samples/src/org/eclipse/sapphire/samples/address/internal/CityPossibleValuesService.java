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

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.PropertyContentEvent;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.samples.address.Address;
import org.eclipse.sapphire.samples.zipcodes.ZipCodeRepository;
import org.eclipse.sapphire.services.PossibleValuesService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class CityPossibleValuesService extends PossibleValuesService
{
    @Override
    protected void init()
    {
        final Listener listener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                broadcast();
            }
        };
        
        final Address address = context( Address.class );
        
        address.attach( listener, Address.PROP_STATE );
        address.attach( listener, Address.PROP_ZIP_CODE );
    }

    @Override
    protected void fillPossibleValues( final SortedSet<String> values )
    {
        final Address address = context( Address.class );
        final String state = address.getState().getText();
        final String zip = address.getZipCode().getText();
        
        values.addAll( ZipCodeRepository.getCities( zip, state ) );
    }

    @Override
    public String getInvalidValueMessage( final String invalidValue )
    {
        return NLS.bind( "\"{0}\" is not a valid city for the specified state and ZIP code.", invalidValue );
    }

    @Override
    public boolean isCaseSensitive()
    {
        return false;
    }

}
