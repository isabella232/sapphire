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

package org.eclipse.sapphire.samples.zoo.internal;

import java.util.SortedSet;

import org.eclipse.sapphire.modeling.annotations.PossibleValuesProviderImpl;
import org.eclipse.sapphire.samples.zipcodes.ZipCodesDatabase;
import org.eclipse.sapphire.samples.zoo.IAddress;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StateCodeValuesProvider

    extends PossibleValuesProviderImpl
    
{
    @Override
    protected void fillPossibleValues( final SortedSet<String> values )
    {
        final IAddress address = (IAddress) getModelElement();
        
        final String zipCode = address.getZipCode().getText();
        final String city = address.getCity().getText();
        
        values.addAll( ZipCodesDatabase.getStateCodes( zipCode, city ) );
    }

}
