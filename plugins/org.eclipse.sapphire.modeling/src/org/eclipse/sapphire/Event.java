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

package org.eclipse.sapphire;

import static java.lang.Math.min;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class Event
{
    private static final int TRACE_SOURCE_STACK_DEPTH = 1;
    
    public boolean supersedes( final Event event )
    {
        return false;
    }
    
    final void trace( final int listeners )
    {
        final Map<String,String> info = new LinkedHashMap<String,String>();
        fillTracingInfo( info );
        info.put( "listeners", String.valueOf( listeners ) );
        System.err.println( toString( info, Thread.currentThread().getStackTrace() ) );
    }
    
    @Override
    public String toString()
    {
        return toString( fillTracingInfo(), null );
    }
    
    private final String toString( final Map<String,String> info,
                                   final StackTraceElement[] source )
    {
        final StringBuilder buf = new StringBuilder();
        
        buf.append( getClass().getSimpleName() );
        
        if( ! info.isEmpty() || source != null )
        {
            buf.append( "\n{\n" );
            
            for( Map.Entry<String,String> entry : info.entrySet() )
            {
                buf.append( "    " );
                buf.append( entry.getKey() );
                buf.append( " = " );
                buf.append( entry.getValue() );
                buf.append( '\n' );
            }
            
            if( source != null )
            {
                for( int i = 0; i < source.length; i++ )
                {
                    StackTraceElement entry = source[ i ];
                    String cl = entry.getClassName();
                    String method = entry.getMethodName();
                    
                    if( ! ( cl.equals( Event.class.getName() ) ||
                            cl.equals( ListenerContext.class.getName() ) ||
                            cl.equals( Thread.class.getName() ) ||
                            method.equals( "broadcast" ) ||
                            method.equals( "post" ) ) )
                    {
                        for( int j = i, n = min( i + TRACE_SOURCE_STACK_DEPTH, source.length ); j < n; j++ )
                        {
                            entry = source[ j ];
                            cl = entry.getClassName();
                            method = entry.getMethodName();
                            
                            buf.append( "    " );
                            buf.append( j == i ? "source = " : "         " );
                            buf.append( cl );
                            buf.append( '.' );
                            buf.append( method );
                            buf.append( '(' );
                            buf.append( entry.getLineNumber() );
                            buf.append( ")\n" );
                        }
                        
                        break;
                    }
                }
            }
            
            buf.append( '}' );
        }
        
        return buf.toString();
    }

    private final Map<String,String> fillTracingInfo()
    {
        return fillTracingInfo( new LinkedHashMap<String,String>() );
    }

    protected Map<String,String> fillTracingInfo( final Map<String,String> info )
    {
        return info;
    }
    
}
