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

package org.eclipse.sapphire.internal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.sapphire.ConversionService;
import org.eclipse.sapphire.Serialization;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * ConversionService implementation for Date to String conversions specified at property level using @Serialization annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DateToStringConversionServiceForProperty extends ConversionService<Date,String> 
{    
    private DateFormat format;

    public DateToStringConversionServiceForProperty()
    {
        super( Date.class, String.class );
    }

    @Override
    protected void init()
    {
        final Serialization serialization = context( ValueProperty.class ).getAnnotation( Serialization.class );
        this.format = new SimpleDateFormat( serialization.primary() );
    }
    
    @Override
    public synchronized String convert( final Date date )
    {
        // Must synchronize as SimpleDateFormat is not safe for concurrent use by multiple threads.
        
        return this.format.format( date );
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            return ( property != null && property.getTypeClass() == Date.class && property.hasAnnotation( Serialization.class ) );
        }
    }
    
}
