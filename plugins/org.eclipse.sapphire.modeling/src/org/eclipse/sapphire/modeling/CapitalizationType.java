/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public enum CapitalizationType
{
    NO_CAPS,
    FIRST_WORD_ONLY,
    TITLE_STYLE;
    
    public String changeTo( final String str )
    {
        if( this == CapitalizationType.NO_CAPS )
        {
            return str;
        }
        
        final StringBuilder buf = new StringBuilder( str );
        
        changeTo( buf );
        
        return buf.toString();
    }

    public void changeTo( final StringBuilder str )
    {
        if( this == CapitalizationType.NO_CAPS )
        {
            return;
        }
        
        boolean isFirstWord = true;
        boolean isFirstLetterInWord = true;
        
        for( int i = 0, n = str.length(); i < n; i++ )
        {
            final char ch = str.charAt( i );
            
            if( ch == ' ' || ch == '-' )
            {
                isFirstWord = false;
                isFirstLetterInWord = true;
            }
            else
            {
                if( isFirstLetterInWord )
                {
                    if( ( this == CapitalizationType.TITLE_STYLE ) ||
                        ( this == CapitalizationType.FIRST_WORD_ONLY && isFirstWord ) )
                    {
                        final char upper = Character.toUpperCase( ch );
                        str.setCharAt( i, upper );
                    }
                }
                
                isFirstLetterInWord = false;
            }
        }
    }
    
}