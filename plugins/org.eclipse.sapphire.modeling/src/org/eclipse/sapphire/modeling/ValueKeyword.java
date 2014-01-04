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

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class ValueKeyword
{
    private final String keyword;
    private final String value;
    private final String displayString;
    
    public ValueKeyword( final String keyword,
                         final String value )
    {
        this.keyword = keyword;
        this.value = value;
        this.displayString = createDisplayString( keyword, value );
    }
    
    public final String getKeyword()
    {
        return this.keyword;
    }
    
    public final String getValue()
    {
        return this.value;
    }
    
    public final String decode( final String str )
    {
        if( str != null && str.equals( this.keyword ) )
        {
            return this.value;
        }
        
        return str;
    }
    
    public final String encode( final String str )
    {
        if( str != null && str.equals( this.value ) )
        {
            return this.keyword;
        }
        
        return str;
    }
    
    public final String toDisplayString()
    {
        return this.displayString;
    }
    
    protected String createDisplayString( final String keyword,
                                          final String value )
    {
        final StringBuilder buf = new StringBuilder();
        
        buf.append( keyword );
        buf.append( " (" );
        buf.append( value );
        buf.append( ")" );
        
        return buf.toString();
    }
    
    @Override
    public final String toString()
    {
        return this.keyword;
    }
}
