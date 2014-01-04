/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcelo Paternostro - initial implementation
 *    Konstantin Komissarchik - initial implementation review and related changes
 ******************************************************************************/

package org.eclipse.sapphire.internal;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.sapphire.ConversionException;
import org.eclipse.sapphire.ConversionService;

/**
 * ConversionService implementation for String to Date conversions.
 * 
 * @author <a href="marcelo.paternostro@oracle.com">Marcelo Paternostro</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StringToDateConversionService extends ConversionService<String,Date> 
{    
    private final List<DateFormat> formats;
    
    public StringToDateConversionService()
    {
        super( String.class, Date.class );

        this.formats = new ArrayList<DateFormat>( 10 );
        this.formats.add( new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'.'SSSZ" ) );
        this.formats.add( new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'.'SSS" ) );
        this.formats.add( new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" ) );
        this.formats.add( new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm" ) );
        this.formats.add( new SimpleDateFormat( "yyyy-MM-dd" ) );
        this.formats.add( new SimpleDateFormat( "HH:mm" ) );
        this.formats.add( new SimpleDateFormat( "h:mm a" ) );

        DateFormat format = DateFormat.getDateInstance();
        
        if( ! this.formats.contains( format ) )
        {
            this.formats.add( format );
        }
        
        format = DateFormat.getTimeInstance();
        
        if( ! this.formats.contains( format ) ) 
        {
            this.formats.add( format );
        }
        
        format = DateFormat.getDateTimeInstance();
        
        if( ! this.formats.contains( format ) ) 
        {
            this.formats.add( format );
        }
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
    
}
