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

package org.eclipse.sapphire.services;

import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Whitespace;

/**
 * Standard implementation of ValueNormalizationService that derives its behavior from
 * declarative annotations, such as @Whitespace. Can be extended to add further normalization
 * logic to what's provided by the system.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class StandardValueNormalizationService extends ValueNormalizationService
{
    private boolean trim;
    private boolean collapse;
    
    @Override
    protected void init()
    {
        super.init();
        
        final Whitespace ws = context( ValueProperty.class ).getAnnotation( Whitespace.class );
        
        if( ws == null )
        {
            this.trim = true;
            this.collapse = false;
        }
        else
        {
            this.trim = ws.trim();
            this.collapse = ws.collapse();
        }
    }

    @Override
    public String normalize( final String str )
    {
        String text = str;
        
        if( str != null )
        {
            if( this.trim )
            {
                text = trim( text );
            }
            
            if( this.collapse )
            {
                text = collapse( text );
            }
        }
        
        return text;
    }
    
    public static final String trim( final String str ) 
    {
        return str.trim();
    }

    public static final String collapse( final String str ) 
    {
        String text = str;
        
        final StringBuilder buf = new StringBuilder();
        boolean skipNextWhitespace = true;
        
        for( int i = 0, n = text.length(); i < n; i++ )
        {
            final char ch = text.charAt( i );
            
            if( Character.isWhitespace( ch ) )
            {
                if( ! skipNextWhitespace )
                {
                    buf.append( ' ' );
                    skipNextWhitespace = true;
                }
            }
            else
            {
                buf.append( ch );
                skipNextWhitespace = false;
            }
        }
        
        final int length = buf.length();
        
        if( length > 0 && buf.charAt( length - 1 ) == ' ' )
        {
            buf.deleteCharAt( length - 1 );
        }
        
        return buf.toString();
    }
    
}
