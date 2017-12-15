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

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class DelimitedListController<T extends IModelElement>

    extends LayeredModelElementListController<T,DelimitedListController.Entry>

{
    public static final class Entry
    {
        private DelimitedListController<?> controller;
        private Entry prev;
        private Entry next;
        private String value;
        
        public Entry( final DelimitedListController<?> controller )
        {
            this.controller = controller;
            this.prev = null;
            this.next = null;
            this.value = null;
        }
        
        public String getValue()
        {
            return this.value;
        }

        public void setValue( final String value )
        {
            final List<String> segments = this.controller.split( value );
            final int count = segments.size();
            
            if( count == 0 )
            {
                this.value = null;
            }
            else
            {
                this.value = segments.get( 0 );
                
                for( int i = 1; i < count; i++ )
                {
                    final Entry entry = insertAfter();
                    entry.setValue( segments.get( i ) );
                }
            }
            
            this.controller.save();
        }
            
        public void remove()
        {
            if( this == this.controller.head )
            {
                this.controller.head = this.next;
            }
            
            if( this.prev != null )
            {
                this.prev.next = this.next;
            }
            
            if( this.next != null )
            {
                this.next.prev = this.prev;
            }
            
            this.prev = null;
            this.next = null;
            
            this.controller.save();
        }
        
        private Entry insertAfter()
        {
            final Entry entry = new Entry( this.controller );

            entry.prev = this;
            entry.next = this.next;
            this.next = entry;
            
            if( entry.next != null )
            {
                entry.next.prev = entry;
            }
            
            return entry;
        }
    }

    private final char delimiter;
    private Entry head;
    
    public DelimitedListController( final char delimiter )
    {
        this.delimiter = delimiter;
        this.head = null;
    }

    @Override
    public final List<T> refresh( final List<T> contents )
    {
        final List<String> strings = split( read() );
        final int count = strings.size();
        
        if( count == 0 )
        {
            this.head = null;
        }
        else
        {
            if( this.head == null )
            {
                this.head = new Entry( this );
            }
            
            int i = 0;
            Entry prev = null;
            Entry entry = this.head;
            
            while( i < count && entry != null )
            {
                entry.value = strings.get( i );
                
                i++;
                prev = entry;
                entry = entry.next;
            }
            
            entry = prev;
            
            while( entry.next != null )
            {
                entry.next.remove();
            }
            
            while( i < count )
            {
                entry = entry.insertAfter();
                entry.value = strings.get( i );
                i++;
            }
        }
        
        final List<Entry> base = new ArrayList<Entry>();
        
        if( this.head != null )
        {
            Entry entry = this.head;
            
            while( entry != null )
            {
                base.add( entry );
                entry = entry.next;
            }
        }
        
        final List<T> result = refresh( contents, base );
        
        for( T entry : result )
        {
            entry.refresh();
        }
        
        return result;
    }
    
    @Override
    public final T createNewElement( final ModelElementType type )
    {
        getModelElement().getModel().validateEdit();
        
        Entry entry = this.head;
        
        if( entry == null )
        {
            entry = new Entry( this );
            this.head = entry;
        }
        else
        {
            while( entry.next != null )
            {
                entry = entry.next;
            }
            
            entry = entry.insertAfter();
        }
        
        save();
        
        return wrap( entry );
    }

    @Override
    public final void swap( final T x,
                            final T y )
    {
        final Entry a = unwrap( x );
        final Entry b = unwrap( y );
        
        if( a.next == b )
        {
            final Entry aPrev = a.prev;
            final Entry bNext = b.next;
            
            a.prev = b;
            a.next = bNext;
            
            b.prev = aPrev;
            b.next = a;
            
            if( aPrev != null )
            {
                aPrev.next = b;
            }
            
            if( bNext != null )
            {
                bNext.prev = a;
            }
            
            if( a == this.head )
            {
                this.head = b;
            }
        }
        else if( b.next == a )
        {
            swap( y, x );
        }
        else
        {
            throw new UnsupportedOperationException();
        }
        
        save();
    }

    protected abstract String read();
    
    protected abstract void write( String str );
    
    private void save()
    {
        final String str;
        
        if( this.head == null )
        {
            str = null;
        }
        else
        {
            final StringBuilder buf = new StringBuilder();
            
            for( Entry entry = this.head; entry != null; entry = entry.next )
            {
                if( entry != this.head )
                {
                    buf.append( this.delimiter );
                }
                
                final String val = entry.getValue();
                
                if( val != null )
                {
                    buf.append( val );
                }
            }
            
            str = buf.toString();
        }
        
        write( str );
    }
    
    private List<String> split( final String str )
    {
        final List<String> list = new ArrayList<String>();
        
        if( str != null )
        {
            StringBuilder buf = new StringBuilder();
            
            for( int i = 0, n = str.length(); i < n; i++ )
            {
                final char ch = str.charAt( i );
                
                if( ch == this.delimiter )
                {
                    list.add( buf.toString() );
                    buf = new StringBuilder();
                }
                else
                {
                    buf.append( ch );
                }
            }
            
            list.add( buf.toString() );
        }
        
        return list;
    }

}
