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

package org.eclipse.sapphire.modeling;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
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
            
            if( ch == ' ' || ch == '-' || ch == '(' || ch == '<' || ch == '[' || ch == '{' )
            {
                isFirstWord = false;
                isFirstLetterInWord = true;
            }
            else
            {
                if( isFirstLetterInWord )
                {
                    final int startOfWord = i;
                    boolean capitalize = false;
                    
                    if( this == CapitalizationType.FIRST_WORD_ONLY && isFirstWord )
                    {
                        capitalize = true;
                    }
                    else if( this == CapitalizationType.TITLE_STYLE )
                    {
                        if( isFirstWord )
                        {
                            capitalize = true;
                        }
                        else
                        {
                            final StringBuilder word = new StringBuilder();
                            int endOfWord = -1;
                            
                            for( int j = i; j < n; j++ )
                            {
                                final char ch2 = str.charAt( j );
                                
                                if( Character.isLetter( ch2 ) )
                                {
                                    word.append( ch2 );
                                }
                                else
                                {
                                    endOfWord = j - 1;
                                    break;
                                }
                            }
                            
                            if( endOfWord == -1 )
                            {
                                i = n - 1;
                                capitalize = true;
                            }
                            else
                            {
                                i = endOfWord;
                                
                                if( ! WORDS_NOT_TO_CAPITALIZE.contains( word.toString() ) )
                                {
                                    capitalize = true;
                                }
                            }
                        }
                    }
                    
                    if( capitalize )
                    {
                        final char upper = Character.toUpperCase( ch );
                        str.setCharAt( startOfWord, upper );
                    }
                }
                
                isFirstLetterInWord = false;
            }
        }
    }
    
    private static final Set<String> WORDS_NOT_TO_CAPITALIZE = new HashSet<String>();
    
    static
    {
        WORDS_NOT_TO_CAPITALIZE.add( "a" );
        WORDS_NOT_TO_CAPITALIZE.add( "an" );
        WORDS_NOT_TO_CAPITALIZE.add( "and" );
        WORDS_NOT_TO_CAPITALIZE.add( "as" );
        WORDS_NOT_TO_CAPITALIZE.add( "at" );
        WORDS_NOT_TO_CAPITALIZE.add( "but" );
        WORDS_NOT_TO_CAPITALIZE.add( "by" );
        WORDS_NOT_TO_CAPITALIZE.add( "if" );
        WORDS_NOT_TO_CAPITALIZE.add( "in" );
        WORDS_NOT_TO_CAPITALIZE.add( "nor" );
        WORDS_NOT_TO_CAPITALIZE.add( "of" );
        WORDS_NOT_TO_CAPITALIZE.add( "on" );
        WORDS_NOT_TO_CAPITALIZE.add( "or" );
        WORDS_NOT_TO_CAPITALIZE.add( "the" );
        WORDS_NOT_TO_CAPITALIZE.add( "to" );
    }
    
}