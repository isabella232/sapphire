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

package org.eclipse.sapphire.java;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class JavaQualifiedName implements Comparable<JavaQualifiedName>
{
    private final String name;
    
    public JavaQualifiedName( final String name )
    {
        boolean valid = true;
        
        final int STATE_EXPECTING_FIRST = 1;
        final int STATE_EXPECTING_NEXT = 2;
        
        int state = STATE_EXPECTING_FIRST;
        
        for( int i = 0, n = name.length(); i < n; i++ )
        {
            final char ch = name.charAt( i );
            
            if( state == STATE_EXPECTING_FIRST )
            {
                if( Character.isJavaIdentifierStart( ch ) )
                {
                    state = STATE_EXPECTING_NEXT;
                }
                else
                {
                    valid = false;
                    break;
                }
            }
            else
            {
                if( ch == '.' )
                {
                    state = STATE_EXPECTING_FIRST;
                }
                else if( Character.isJavaIdentifierPart( ch ) )
                {
                    // Keep state as STATE_EXPECTING_NEXT.
                }
                else
                {
                    valid = false;
                    break;
                }
            }
        }
        
        if( state == STATE_EXPECTING_FIRST )
        {
            valid = false;
        }
        
        if( ! valid )
        {
            throw new IllegalArgumentException();
        }
        
        this.name = name;
    }
    
    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof JavaQualifiedName )
        {
            return this.name.equals( ( (JavaQualifiedName) obj ).name );
        }
        
        return false;
    }
    
    @Override
    public int hashCode()
    {
        return this.name.hashCode();
    }

    public int compareTo( final JavaQualifiedName obj )
    {
        return this.name.compareTo( obj.name );
    }

    @Override
    public String toString()
    {
        return this.name;
    }
    
}
