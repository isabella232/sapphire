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

package org.eclipse.sapphire.services.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.eclipse.sapphire.modeling.EnumValueType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.services.PossibleValuesService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;
import org.eclipse.sapphire.services.ValueSerializationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class EnumPossibleValuesService extends PossibleValuesService
{
    private final List<String> values = new ArrayList<String>();
    
    @Override
    protected void init()
    {
        super.init();
        
        final IModelElement element = context( IModelElement.class );
        final ValueProperty property = context( ValueProperty.class );
        
        final EnumValueType enumType = new EnumValueType( property.getTypeClass() );
        final ValueSerializationService valueSerializationService = element.service( property, ValueSerializationService.class );
        
        for( Enum<?> item : enumType.getItems() )
        {
            this.values.add( valueSerializationService.encode( item ) );
        }
    }
    
    @Override
    protected void fillPossibleValues( final SortedSet<String> values )
    {
        values.addAll( this.values );
    }

    @Override
    public Status.Severity getInvalidValueSeverity( final String invalidValue )
    {
        return Status.Severity.OK;
    }

    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            return ( property != null && Enum.class.isAssignableFrom( property.getTypeClass() ) );
        }

        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new EnumPossibleValuesService();
        }
    }
    
}
