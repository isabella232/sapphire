/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.address;

import java.util.Set;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.samples.zipcodes.ZipCodeRepository;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class CityPossibleValuesService extends PossibleValuesService
{
    @Text( "\"${City}\" is not a valid city for the specified state and ZIP code." )
    private static LocalizableText message;
    
    static
    {
        LocalizableText.init( CityPossibleValuesService.class );
    }

    @Override
    protected void initPossibleValuesService()
    {
        this.invalidValueMessage = message.text();
        
        final Address address = context( Address.class );
        
        final Listener listener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                refresh();
                
                final Set<String> values = values();
                
                if( values.size() == 1 )
                {
                    final String city = values.iterator().next();
                    
                    if( ! city.equalsIgnoreCase( address.getCity().text() ) )
                    {
                        address.setCity( city );
                    }
                }
            }
        };
        
        address.getState().attach( listener );
        address.getZipCode().attach( listener );
    }

    @Override
    protected void compute( final Set<String> values )
    {
        final Address address = context( Address.class );
        final String state = address.getState().text();
        final String zip = address.getZipCode().text();
        
        values.addAll( ZipCodeRepository.getCities( zip, state ) );
    }

}
