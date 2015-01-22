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

package org.eclipse.sapphire.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.sapphire.MasterConversionService;
import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.EnumValueType;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class EnumPossibleValuesService extends PossibleValuesService
{
    private final List<String> values = new ArrayList<String>();
    
    @Override
    protected void initPossibleValuesService()
    {
        final ValueProperty property = context( ValueProperty.class );
        
        final EnumValueType enumType = new EnumValueType( property.getTypeClass() );
        final MasterConversionService masterConversionService = property.service( MasterConversionService.class );
        
        for( Enum<?> item : enumType.getItems() )
        {
            this.values.add( masterConversionService.convert( item, String.class ) );
        }
    }
    
    @Override
    protected void compute( final Set<String> values )
    {
        values.addAll( this.values );
    }
    
    @Override
    public Status problem( final Value<?> value )
    {
        return Status.createOkStatus();
    }

    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            return ( property != null && Enum.class.isAssignableFrom( property.getTypeClass() ) );
        }
    }
    
}
