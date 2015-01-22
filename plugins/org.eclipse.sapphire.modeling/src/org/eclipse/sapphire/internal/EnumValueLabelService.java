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

import org.eclipse.sapphire.MasterConversionService;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.EnumValueType;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ValueLabelService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class EnumValueLabelService extends ValueLabelService
{
    private EnumValueType enumType;
    
    @Override
    protected void init()
    {
        super.init();
        
        this.enumType = new EnumValueType( context( ValueProperty.class ).getTypeClass() );
    }
    
    @Override
    public String provide( final String value )
    {
        final MasterConversionService masterConversionService = context( ValueProperty.class ).service( MasterConversionService.class );
        final Enum<?> item = masterConversionService.convert( value, this.enumType.getEnumTypeClass() );
        
        if( item == null )
        {
            return value;
        }
        else
        {
            return this.enumType.getLabel( item, false, CapitalizationType.NO_CAPS, false );
        }
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
