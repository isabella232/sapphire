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

package org.eclipse.sapphire.services.internal;

import java.util.SortedSet;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.internal.ValueSnapshot;
import org.eclipse.sapphire.modeling.annotations.NumericRange;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * Creates fact statements about numeric value property's range by using semantical information 
 * specified by @NumericRange annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class NumericRangeFactsService extends FactsService
{
    @Text( "Minimum value is {0}" )
    private static LocalizableText minValueStatement;
    
    @Text( "Maximum value is {0}" )
    private static LocalizableText maxValueStatement;
    
    static
    {
        LocalizableText.init( NumericRangeFactsService.class );
    }

    @Override
    protected void facts( final SortedSet<String> facts )
    {
        final ValueProperty property = context( ValueProperty.class );
        final NumericRange range = property.getAnnotation( NumericRange.class );
        final String min = range.min();
        final String max = range.max();
            
        if( min.length() > 0 ) 
        {
            facts.add( minValueStatement.format( new ValueSnapshot( property, min ) ) );
        }
        
        if( max.length() > 0 ) 
        {
            facts.add( maxValueStatement.format( new ValueSnapshot( property, max ) ) );
        }
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            
            if( property != null )
            {
                final NumericRange range = property.getAnnotation( NumericRange.class );
                return ( range != null && ( range.min().length() > 0 || range.max().length() > 0 ) );
            }
            
            return false;
        }
    }
    
}
