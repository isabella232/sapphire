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

package org.eclipse.sapphire.java;

import static java.lang.Character.isJavaIdentifierPart;
import static java.lang.Character.isJavaIdentifierStart;

import org.eclipse.sapphire.modeling.annotations.Label;

/**
 * A Java identifier is a name of a variable, a field or a method. Identifiers must conform
 * to <nobr>[a-zA-Z_$][a-zA-Z0-9_$]*</nobr> pattern. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "Java identifier" )

public final class JavaIdentifier implements Comparable<JavaIdentifier>
{
    private final String text;
    
    public JavaIdentifier( final String text )
    {
        if( text == null )
        {
            throw new IllegalArgumentException();
        }
        
        final int length = text.length();
        
        if( length == 0 )
        {
            throw new IllegalArgumentException();
        }
        
        final char first = text.charAt( 0 );
        
        if( ! isJavaIdentifierStart( first ) )
        {
            throw new IllegalArgumentException();
        }
        
        for( int i = 1; i < length; i++ )
        {
            if( ! isJavaIdentifierPart( text.charAt( i ) ) )
            {
                throw new IllegalArgumentException();
            }
        }
        
        this.text = text;
    }
    
    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof JavaIdentifier )
        {
            return this.text.equals( ( (JavaIdentifier) obj ).text );
        }
        
        return false;
    }
    
    @Override
    public int hashCode()
    {
        return this.text.hashCode();
    }

    public int compareTo( final JavaIdentifier obj )
    {
        return this.text.compareTo( obj.text );
    }
    
    @Override
    public String toString()
    {
        return this.text;
    }
    
}
