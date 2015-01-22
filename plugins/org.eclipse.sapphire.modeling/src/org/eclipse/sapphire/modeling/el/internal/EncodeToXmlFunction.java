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

package org.eclipse.sapphire.modeling.el.internal;

import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;

/**
 * Encodes text for use as XML element content or an attribute value. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class EncodeToXmlFunction extends Function
{
    private static final char[] ESCAPE_CHARS = 
    {
        '<',
        '>',
        '&',
        '\"',
        '\''
    };
    
    private static final String[] ESCAPE_STRINGS =
    {
        "&lt;",
        "&gt;",
        "&amp;",
        "&quot;",
        "&apos;"
    };
    
    @Override
    public String name()
    {
        return "EncodeToXml";
    }

    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            @Override
            protected Object evaluate()
            {
                final String string = cast( operand( 0 ), String.class );
                boolean needToEncode = false;
                
                for( int i = 0, n = string.length(); i < n && ! needToEncode; i++ )
                {
                    final char ch = string.charAt( i );
                    
                    for( int j = 0; j < ESCAPE_CHARS.length && ! needToEncode; j++ )
                    {
                        if( ch == ESCAPE_CHARS[ j ] )
                        {
                            needToEncode = true;
                        }
                    }
                }
                
                if( needToEncode )
                {
                    final StringBuilder buf = new StringBuilder();
                    
                    for( int i = 0, n = string.length(); i < n; i++ )
                    {
                        final char ch = string.charAt( i );
                        boolean encoded = false;
                        
                        for( int j = 0; j < ESCAPE_CHARS.length && ! encoded; j++ )
                        {
                            if( ch == ESCAPE_CHARS[ j ] )
                            {
                                buf.append( ESCAPE_STRINGS[ j ] );
                                encoded = true;
                            }
                        }
                        
                        if( ! encoded )
                        {
                            buf.append( ch );
                        }
                    }
                    
                    return buf.toString();
                }
                else
                {
                    return string;
                }
            }
        };
    }
    
}
