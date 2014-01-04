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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.sapphire.ConversionException;
import org.eclipse.sapphire.ConversionService;
import org.eclipse.sapphire.Serialization;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.util.ListFactory;

/**
 * ConversionService implementation for String to Date conversions specified at property level using @Serialization annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StringToDateConversionServiceForProperty extends ConversionService<String,Date> 
{    
    private List<DateFormat> formats;

    public StringToDateConversionServiceForProperty()
    {
        super( String.class, Date.class );
    }

    @Override
    protected void init()
    {
        final Serialization serialization = context( ValueProperty.class ).getAnnotation( Serialization.class );
        final ListFactory<DateFormat> formatsListFactory = ListFactory.start();
        
        formatsListFactory.add( new SimpleDateFormat( serialization.primary() ) );
        
        for( final String alternative : serialization.alternative() )
        {
            formatsListFactory.add( new SimpleDateFormat( alternative ) );
        }
        
        this.formats = formatsListFactory.result();
    }

    @Override
    public synchronized Date convert( final String string ) 
    {
        // Must synchronize as SimpleDateFormat is not safe for concurrent use by multiple threads.
        
        for( DateFormat format : this.formats )
        {
            try
            {
                return format.parse( string );
            }
            catch( ParseException e ) 
            {
                // Intentionally ignored.
            }
        }
        
        throw new ConversionException();
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
