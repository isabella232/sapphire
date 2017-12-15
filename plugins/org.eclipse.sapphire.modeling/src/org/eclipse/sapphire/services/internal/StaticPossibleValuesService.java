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

import java.util.List;
import java.util.SortedSet;

import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.services.PossibleValuesService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;
import org.eclipse.sapphire.util.ListFactory;

/**
 * Implementation of PossibleValuesService based on @PossibleValues annotation's values attribute..
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StaticPossibleValuesService extends PossibleValuesService
{
    private final List<String> values;
    
    public StaticPossibleValuesService( final String[] values,
                                        final String invalidValueMessageTemplate,
                                        final Status.Severity invalidValueSeverity,
                                        final boolean caseSensitive )
    {
        super( invalidValueMessageTemplate, invalidValueSeverity, caseSensitive );
        
        this.values = ListFactory.unmodifiable( values );
    }

    @Override
    protected void fillPossibleValues( final SortedSet<String> values )
    {
        values.addAll( this.values );
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            return ( property != null && property.hasAnnotation( PossibleValues.class ) && property.getAnnotation( PossibleValues.class ).values().length > 0 );
        }

        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            final PossibleValues a = context.find( ValueProperty.class ).getAnnotation( PossibleValues.class );
            return new StaticPossibleValuesService( a.values(), a.invalidValueMessage(), a.invalidValueSeverity(), a.caseSensitive() );
        }
    }
    
}
