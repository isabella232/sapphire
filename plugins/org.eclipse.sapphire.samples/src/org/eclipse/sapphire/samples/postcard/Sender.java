/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.postcard;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Listeners;
import org.eclipse.sapphire.modeling.annotations.Service;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface Sender extends FullAddress
{
    ElementType TYPE = new ElementType( Sender.class );
    
    // *** Name ***
    
    @Service( impl = SenderDefaultValueService.class )
    @Listeners( SenderListener.class )
    
    ValueProperty PROP_NAME = new ValueProperty( TYPE, FullAddress.PROP_NAME );

    // *** Street ***
    
    @Service( impl = SenderDefaultValueService.class )
    @Listeners( SenderListener.class )
    
    ValueProperty PROP_STREET = new ValueProperty( TYPE, FullAddress.PROP_STREET );

    // *** City ***
    
    @Service( impl = SenderDefaultValueService.class )
    @Listeners( SenderListener.class )
    
    ValueProperty PROP_CITY = new ValueProperty( TYPE, FullAddress.PROP_CITY );

    // *** State ***
    
    @Service( impl = SenderDefaultValueService.class )
    @Listeners( SenderListener.class )
    
    ValueProperty PROP_STATE = new ValueProperty( TYPE, FullAddress.PROP_STATE );

    // *** ZipCode ***
    
    @Service( impl = SenderDefaultValueService.class )
    @Listeners( SenderListener.class )
    
    ValueProperty PROP_ZIP_CODE = new ValueProperty( TYPE, FullAddress.PROP_ZIP_CODE );

}
