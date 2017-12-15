/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.sdk.build.processor.internal.util;

import java.io.PrintWriter;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class IndentingPrintWriter
{
    private static final String INDENT = "    ";
    private static final String[] COMMON_INDENTS;
    
    static
    {
        COMMON_INDENTS = new String[ 10 ];
        COMMON_INDENTS[ 0 ] = "";
        
        for( int i = 1; i < COMMON_INDENTS.length; i++ )
        {
            COMMON_INDENTS[ i ] = COMMON_INDENTS[ i - 1 ] + INDENT;
        }
    }
    
    private final PrintWriter base;
    private int indentLevel;
    private String indentLevelString;
    private boolean indentOnNextWrite;
    
    public IndentingPrintWriter( final PrintWriter base )
    {
        this.base = base;
        this.indentLevel = 0;
        this.indentLevelString = COMMON_INDENTS[ 0 ];
        this.indentOnNextWrite = false;
    }
    
    public void print( final String value )
    {
        indentIfNecessary();
        this.base.print( value );
    }
    
    public void print( final char value )
    {
        indentIfNecessary();
        this.base.print( value );
    }
    
    public void print( final Object value )
    {
        indentIfNecessary();
        this.base.print( value );
    }
    
    public void println()
    {
        indentIfNecessary();
        this.base.println();
        this.indentOnNextWrite = true;
    }
    
    public void printf( final String format, 
                        final Object ... args ) 
    {
        indentIfNecessary();
        this.base.printf( format, args );
    }
    
    public void increaseIndent()
    {
        this.indentLevel++;
        
        if( this.indentLevel < COMMON_INDENTS.length )
        {
            this.indentLevelString = COMMON_INDENTS[ this.indentLevel ];
        }
        else
        {
            this.indentLevelString = this.indentLevelString + INDENT;
        }
    }
    
    public void decreaseIndent()
    {
        if( this.indentLevel > 0 )
        {
            this.indentLevel--;
            
            if( this.indentLevel < COMMON_INDENTS.length )
            {
                this.indentLevelString = COMMON_INDENTS[ this.indentLevel ];
            }
            else
            {
                this.indentLevelString = this.indentLevelString.substring( 0, this.indentLevelString.length() - INDENT.length() );
            }
        }
    }
    
    private void indentIfNecessary()
    {
        if( this.indentOnNextWrite )
        {
            this.base.print( this.indentLevelString );
            this.indentOnNextWrite = false;
        }
    }
    
}
