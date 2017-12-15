/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcelo Paternostro - initial implementation
 *    Konstantin Komissarchik - initial implementation review and related changes
 ******************************************************************************/

package org.eclipse.sapphire.modeling.serialization;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;

/**
 * Standard implementation of Date serialization service that can also be subclassed to support
 * custom Date serialization needs.
 * 
 * @author <a href="marcelo.paternostro@oracle.com">Marcelo Paternostro</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class DateSerializationService extends ValueSerializationService 
{    
    private final static List<DateFormat> STANDARD_FORMATS;
    
    static 
    {
        final List<DateFormat> formats = new ArrayList<DateFormat>();
        formats.add( new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'.'SSSZ" ) );
        formats.add( new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'.'SSS" ) );
        formats.add( new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" ) );
        formats.add( new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm" ) );
        formats.add( new SimpleDateFormat( "yyyy-MM-dd" ) );
        formats.add( new SimpleDateFormat( "HH:mm" ) );
        formats.add( new SimpleDateFormat( "h:mm a" ) );

        DateFormat format = DateFormat.getDateInstance();
        
        if( ! formats.contains( format ) )
        {
            formats.add( format );
        }
        
        format = DateFormat.getTimeInstance();
        
        if( ! formats.contains( format ) ) 
        {
            formats.add( format );
        }
        
        format = DateFormat.getDateTimeInstance();
        
        if( ! formats.contains( format ) ) 
        {
            formats.add(format);
        }
        
        STANDARD_FORMATS = Collections.unmodifiableList(formats);
    };
    
    private List<DateFormat> formats;
    
    @Override
    public void init( final IModelElement element,
                      final ModelProperty property,
                      final String[] params )
    {
        super.init( element, property, params );
        
        if( params.length == 0 )
        {
            this.formats = STANDARD_FORMATS;
        }
        else
        {
            this.formats = new ArrayList<DateFormat>();
            
            for( String format : params )
            {
                this.formats.add( new SimpleDateFormat( format ) );
            }
            
            this.formats = Collections.unmodifiableList( this.formats );
        }
    }

    /**
     * <p>Returns the list of date formats supported by this serialization service. The first
     * format in the list will be always used for encoding to string. All formats will be
     * tried (in order) when decoding from string.</p>
     * 
     * <p>Subclasses may override to support different formats.</p>
     * 
     * @return the list of date formats supported by this serialization service
     */
    
    public List<? extends DateFormat> formats()
    {
        return this.formats;
    }
    
    @Override
    protected Object decodeFromString( final String value ) 
    {
        for( DateFormat supportedFormat : formats() )
        {
            try
            {
                return supportedFormat.parse( value );
            }
            catch( ParseException e ) 
            {
                // Intentionally ignored. It is not the job of serializer to report these
                // problems. That's handled by validators.
            }
        }
        
        return null;
    }
    
    @Override
    public String encode( final Object value )
    {
        return formats().get( 0 ).format( (Date) value );
    }
    
}
