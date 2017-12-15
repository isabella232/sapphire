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

package org.eclipse.sapphire.modeling.annotations.processor.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.modeling.util.internal.MiscUtil;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class Body

    extends BaseModel
    
{
    private static final BaseModel EMPTY_LINE_SEGMENT = new StringSegment( MiscUtil.EMPTY_STRING );
    
    private static final BaseModel BLOCK_OPEN_SEGMENT = new BaseModel()
    {
        @Override
        public void write( final IndentingPrintWriter pw )
        {
            pw.print( "{" );
            pw.println();
            pw.increaseIndent();
        }
    };
    
    private static final BaseModel BLOCK_CLOSE_SEGMENT = new BaseModel()
    {
        @Override
        public void write( final IndentingPrintWriter pw )
        {
            pw.decreaseIndent();
            pw.print( "}" );
            pw.println();
        }
    };
    
    private static final BaseModel BLOCK_CLOSE_SEGMENT_WITH_SEMICOLON = new BaseModel()
    {
        @Override
        public void write( final IndentingPrintWriter pw )
        {
            pw.decreaseIndent();
            pw.print( "};" );
            pw.println();
        }
    };
    
    private static final BaseModel METHOD_PARAMS_BLOCK_OPEN_SEGMENT = new BaseModel()
    {
        @Override
        public void write( final IndentingPrintWriter pw )
        {
            pw.print( "(" );
            pw.println();
            pw.increaseIndent();
        }
    };
    
    private static final BaseModel METHOD_PARAMS_BLOCK_CLOSE_SEGMENT = new BaseModel()
    {
        @Override
        public void write( final IndentingPrintWriter pw )
        {
            pw.decreaseIndent();
            pw.print( ");" );
            pw.println();
        }
    };
    
    private List<BaseModel> segments;
    
    public Body()
    {
        this.segments = new ArrayList<BaseModel>();
    }
    
    public void append( final String content )
    {
        this.segments.add( new StringSegment( content ) );
    }
    
    public void append( final String content,
                        final String... args )
    {
        final StringBuilder buf = new StringBuilder();
        
        for( int i = 0, n = content.length(); i < n; i++ )
        {
            char ch = content.charAt( i );
            
            if( ch == '#' && i + 1 < n )
            {
                i++;
                ch = content.charAt( i );
                buf.append( args[ Integer.parseInt( String.valueOf( ch ) ) - 1 ] );
            }
            else
            {
                buf.append( ch );
            }
        }
        
        append( buf.toString() );
    }
    
    public void appendEmptyLine()
    {
        this.segments.add( EMPTY_LINE_SEGMENT );
    }
    
    public void openBlock()
    {
        this.segments.add( BLOCK_OPEN_SEGMENT );
    }
    
    public void closeBlock()
    {
        closeBlock( false );
    }

    public void closeBlock( final boolean suffixWithSemicolon )
    {
        this.segments.add( suffixWithSemicolon ? BLOCK_CLOSE_SEGMENT_WITH_SEMICOLON : BLOCK_CLOSE_SEGMENT );
    }
    
    public void openMethodParamsBlock()
    {
        this.segments.add( METHOD_PARAMS_BLOCK_OPEN_SEGMENT );
    }
    
    public void closeMethodParamsBlock()
    {
        this.segments.add( METHOD_PARAMS_BLOCK_CLOSE_SEGMENT );
    }
    
    @Override
    public void write( final IndentingPrintWriter pw )
    {
        for( BaseModel segment : this.segments )
        {
            segment.write( pw );
        }
    }
    
    private static final class StringSegment
    
        extends BaseModel
        
    {
        private final String str;
        
        public StringSegment( final String str )
        {
            this.str = str;
        }
        
        @Override
        public void write( final IndentingPrintWriter pw )
        {
            for( String line : this.str.replace( "\r", "" ).split( "\n" ) )
            {
                pw.print( line );
                pw.println();
            }
        }
    }
    
}
