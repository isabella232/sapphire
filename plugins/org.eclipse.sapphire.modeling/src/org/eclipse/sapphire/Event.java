/******************************************************************************
 * Copyright (c) 2013 Oracle
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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class Event
{
    private static final int TRACE_SOURCE_STACK_DEPTH = 1;
    
    public boolean supersedes( final Event event )
    {
        return false;
    }
    
    public static final Listener filter( final Listener listener,
                                         final Class<?>... types )
    {
        if( types.length == 0 )
        {
            return new FilteredListenerForZero( listener );
        }
        else if( types.length == 1 )
        {
            return new FilteredListenerForOne( listener, types[ 0 ] );
        }
        else if( types.length == 2 )
        {
            return new FilteredListenerForTwo( listener, types[ 0 ], types[ 1 ] );
        }
        else if( types.length == 3 )
        {
            return new FilteredListenerForThree( listener, types[ 0 ], types[ 1 ], types [ 2 ] );
        }
        else
        {
            return new FilteredListenerForMultiple( listener, types );
        }
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
    
    private static final class FilteredListenerForZero extends Listener
    {
        private final Listener listener;
        
        public FilteredListenerForZero( final Listener listener )
        {
            this.listener = listener;
        }
        
        @Override
        public void handle( final Event event )
        {
            // Do nothing, since no events are acceptable. Ideally, the filter creator should
            // avoid getting into this case since attaching such listener serves no purpose.
        }
        
        @Override
        public boolean equals( final Object obj )
        {
            if( obj instanceof FilteredListenerForZero )
            {
                final FilteredListenerForZero x = (FilteredListenerForZero) obj;
                return this.listener.equals( x.listener );
            }
            
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return this.listener.hashCode();
        }
    }

    private static final class FilteredListenerForOne extends Listener
    {
        private final Listener listener;
        private final Class<?> eventType;
        
        public FilteredListenerForOne( final Listener listener,
                                       final Class<?> eventType )
        {
            this.listener = listener;
            this.eventType = eventType;
        }
        
        @Override
        public void handle( final Event event )
        {
            if( this.eventType.isInstance( event ) )
            {
                this.listener.handle( event );
            }
        }
        
        @Override
        public boolean equals( final Object obj )
        {
            if( obj instanceof FilteredListenerForOne )
            {
                final FilteredListenerForOne x = (FilteredListenerForOne) obj;
                return ( this.listener.equals( x.listener ) && this.eventType == x.eventType );
            }
            
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return this.listener.hashCode() ^ this.eventType.hashCode();
        }
    }

    private static final class FilteredListenerForTwo extends Listener
    {
        private final Listener listener;
        private final Class<?> eventType1;
        private final Class<?> eventType2;
        
        public FilteredListenerForTwo( final Listener listener,
                                       final Class<?> eventType1,
                                       final Class<?> eventType2 )
        {
            this.listener = listener;
            this.eventType1 = eventType1;
            this.eventType2 = eventType2;
        }
        
        @Override
        public void handle( final Event event )
        {
            if( this.eventType1.isInstance( event ) || this.eventType2.isInstance( event ) )
            {
                this.listener.handle( event );
            }
        }
        
        @Override
        public boolean equals( final Object obj )
        {
            if( obj instanceof FilteredListenerForTwo )
            {
                final FilteredListenerForTwo x = (FilteredListenerForTwo) obj;
                return ( this.listener.equals( x.listener ) && this.eventType1 == x.eventType1 && this.eventType2 == x.eventType2 );
            }
            
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return this.listener.hashCode() ^ this.eventType1.hashCode() ^ this.eventType2.hashCode();
        }
    }
    
    private static final class FilteredListenerForThree extends Listener
    {
        private final Listener listener;
        private final Class<?> eventType1;
        private final Class<?> eventType2;
        private final Class<?> eventType3;
        
        public FilteredListenerForThree( final Listener listener,
                                         final Class<?> eventType1,
                                         final Class<?> eventType2,
                                         final Class<?> eventType3 )
        {
            this.listener = listener;
            this.eventType1 = eventType1;
            this.eventType2 = eventType2;
            this.eventType3 = eventType3;
        }
        
        @Override
        public void handle( final Event event )
        {
            if( this.eventType1.isInstance( event ) || this.eventType2.isInstance( event ) || this.eventType3.isInstance( event ) )
            {
                this.listener.handle( event );
            }
        }
        
        @Override
        public boolean equals( final Object obj )
        {
            if( obj instanceof FilteredListenerForThree )
            {
                final FilteredListenerForThree x = (FilteredListenerForThree) obj;
                return ( this.listener.equals( x.listener ) && this.eventType1 == x.eventType1 && this.eventType2 == x.eventType2 && this.eventType3 == x.eventType3 );
            }
            
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return this.listener.hashCode() ^ this.eventType1.hashCode() ^ this.eventType2.hashCode() ^ this.eventType3.hashCode();
        }
    }

    private static final class FilteredListenerForMultiple extends Listener
    {
        private final Listener listener;
        private final Class<?>[] eventTypes;
        
        public FilteredListenerForMultiple( final Listener listener,
                                            final Class<?>[] eventTypes )
        {
            this.listener = listener;
            this.eventTypes = eventTypes;
        }
        
        @Override
        public void handle( final Event event )
        {
            boolean ok = false;
            
            for( Class<?> type : this.eventTypes )
            {
                if( type.isInstance( event ) )
                {
                    ok = true;
                    break;
                }
            }
            
            if( ok )
            {
                this.listener.handle( event );
            }
        }
        
        @Override
        public boolean equals( final Object obj )
        {
            if( obj instanceof FilteredListenerForMultiple )
            {
                final FilteredListenerForMultiple x = (FilteredListenerForMultiple) obj;
                return ( this.listener.equals( x.listener ) && Arrays.equals( this.eventTypes, x.eventTypes ) );
            }
            
            return false;
        }
        
        @Override
        public int hashCode()
        {
            int hashcode = this.listener.hashCode();
            
            for( Class<?> eventType : this.eventTypes )
            {
                hashcode = hashcode ^ eventType.hashCode();
            }
            
            return hashcode;
        }
    }
}
