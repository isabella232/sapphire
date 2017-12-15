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

import java.util.SortedSet;

import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.NumericRange;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.modeling.util.internal.SapphireCommonUtil;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * Creates fact statements about numeric value property's range by using semantical information 
 * specified by @NumericRange annotation.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class NumericRangeFactsService extends FactsService
{
    @Override
    protected void facts( final SortedSet<String> facts )
    {
        final ValueProperty property = context( ValueProperty.class );
        final NumericRange range = property.getAnnotation( NumericRange.class );
        final String min = range.min();
        final String max = range.max();
            
        if( min.length() > 0 ) 
        {
            facts.add( NLS.bind( Resources.minValueStatement, SapphireCommonUtil.normalizeForDisplay( property, min ) ) );
        }
        
        if( max.length() > 0 ) 
        {
            facts.add( NLS.bind( Resources.maxValueStatement, SapphireCommonUtil.normalizeForDisplay( property, max ) ) );
        }
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            
            if( property != null )
            {
                final NumericRange range = property.getAnnotation( NumericRange.class );
                return ( range != null && ( range.min().length() > 0 || range.max().length() > 0 ) );
            }
            
            return false;
        }
    
        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new NumericRangeFactsService();
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String minValueStatement;
        public static String maxValueStatement;
        
        static
        {
            initializeMessages( NumericRangeFactsService.class.getName(), Resources.class );
        }
    }
    
}
