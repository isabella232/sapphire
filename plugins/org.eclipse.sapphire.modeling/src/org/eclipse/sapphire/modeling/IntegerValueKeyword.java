/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling;

import java.text.NumberFormat;

import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;


/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class IntegerValueKeyword extends ValueKeyword
{
    public IntegerValueKeyword( final String keyword,
                                final String value )
    {
        super( keyword, value );
    }

    @Override
    protected String createDisplayString( final String keyword,
                                          final String value )
    {
        String formattedValue = value;
        
        try
        {
            final int parsedValue = Integer.parseInt( value );
            formattedValue = NumberFormat.getInstance().format( parsedValue );
        }
        catch( NumberFormatException e )
        {
            Sapphire.service( LoggingService.class ).log( e );
        }
        
        final StringBuilder buf = new StringBuilder();
        
        buf.append( keyword );
        buf.append( " (" );
        buf.append( formattedValue );
        buf.append( ")" );
        
        return buf.toString();
    }
    
}
