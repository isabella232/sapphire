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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListPropertyBinding;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyBinding;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.ValuePropertyBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class DelimitedListBindingImpl extends ListPropertyBinding
{
    private ListEntryResource head;
    
    @Override
    public ElementType type( final Resource resource )
    {
        return property().definition().getType();
    }

    @Override
    public final List<Resource> read()
    {
        final List<String> strings = split( readListString() );
        final int count = strings.size();
        
        if( count == 0 )
        {
            this.head = null;
        }
        else
        {
            if( this.head == null )
            {
                this.head = createListEntryResource();
            }
            
            int i = 0;
            ListEntryResource prev = null;
            ListEntryResource entry = this.head;
            
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
        
        final List<Resource> base = new ArrayList<Resource>();
        
        if( this.head != null )
        {
            ListEntryResource entry = this.head;
            
            while( entry != null )
            {
                base.add( entry );
                entry = entry.next;
            }
        }
        
        return base;
    }
    
    @Override
    public final Resource insert( final ElementType type,
                                  final int position )
    {
        ListEntryResource entry;
        
        if( this.head == null )
        {
            entry = createListEntryResource();
            this.head = entry;
        }
        else
        {
            if( position == 0 )
            {
                entry = this.head.insertBefore();
            }
            else
            {
                entry = this.head;
                
                for( int i = 0, n = position - 1; i < n; i++ )
                {
                    entry = entry.next;
                }
                
                entry = entry.insertAfter();
            }
        }
        
        writeListString();
        
        return entry;
    }
    
    @Override
    public void move( final Resource resource, 
                      final int position )
    {
        final ListEntryResource x = (ListEntryResource) resource;
        
        ListEntryResource y = this.head;
        ListEntryResource yPrev = null;

        for( int i = 0; i < position; i++ )
        {
            yPrev = y;
            y = y.next;
        }
        
        if( x != y && x.next != y )
        {
            x.remove();
            
            x.next = y;
            x.prev = yPrev;
            
            if( x.prev == null )
            {
                this.head = x;
            }
            else
            {
                x.prev.next = x;
            }
            
            if( y != null )
            {
                y.prev = x;
            }
            
            writeListString();
        }
    }

    @Override
    public void remove( final Resource resource )
    {
        ( (ListEntryResource) resource ).remove();
        writeListString();
    }
    
    protected char getDelimiter()
    {
        return ',';
    }
    
    protected abstract String readListString();
    
    protected abstract void writeListString( String str );
    
    private void writeListString()
    {
        final char delimiter = getDelimiter();
        final String str;
        
        if( this.head == null )
        {
            str = null;
        }
        else
        {
            final StringBuilder buf = new StringBuilder();
            
            for( ListEntryResource entry = this.head; entry != null; entry = entry.next )
            {
                if( entry != this.head )
                {
                    buf.append( delimiter );
                }
                
                final String val = entry.value;
                
                if( val != null )
                {
                    buf.append( val );
                }
            }
            
            str = buf.toString();
        }
        
        writeListString( str );
    }
    
    protected ListEntryResource createListEntryResource()
    {
        return new DefaultListEntryResource();
    }
    
    private List<String> split( final String str )
    {
        final char delimiter = getDelimiter();
        final List<String> list = new ArrayList<String>();
        
        if( str != null )
        {
            StringBuilder buf = new StringBuilder();
            
            for( int i = 0, n = str.length(); i < n; i++ )
            {
                final char ch = str.charAt( i );
                
                if( ch == delimiter )
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
    
    public abstract class ListEntryResource extends Resource
    {
        private ListEntryResource prev;
        private ListEntryResource next;
        private String value;
        
        public ListEntryResource()
        {
            super( DelimitedListBindingImpl.this.property().element().resource() );
        }
        
        public final String getValue()
        {
            return this.value;
        }
        
        public final void setValue( final String value )
        {
            final List<String> segments = split( value );
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
                    final ListEntryResource entry = insertAfter();
                    entry.setValue( segments.get( i ) );
                }
            }
            
            writeListString();
        }
        
        public final void remove()
        {
            if( this == DelimitedListBindingImpl.this.head )
            {
                DelimitedListBindingImpl.this.head = this.next;
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
        }

        private ListEntryResource insertBefore()
        {
            final ListEntryResource entry = createListEntryResource();
            
            entry.next = this;
            entry.prev = this.prev;
            this.prev = entry;
            
            if( entry.prev == null )
            {
                DelimitedListBindingImpl.this.head = entry;
            }
            else
            {
                entry.prev.next = entry;
            }
            
            return entry;
        }

        private ListEntryResource insertAfter()
        {
            final ListEntryResource entry = createListEntryResource();
    
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

    private final class DefaultListEntryResource extends ListEntryResource
    {
        private ValueProperty listEntryProperty;
        
        public DefaultListEntryResource()
        {
            final ElementType listEntryType = property().definition().getType();
            
            for( PropertyDef prop : listEntryType.properties() )
            {
                if( this.listEntryProperty != null )
                {
                    throw new IllegalStateException();
                }
                
                if( prop instanceof ValueProperty )
                {
                    this.listEntryProperty = (ValueProperty) prop;
                }
                else
                {
                    throw new IllegalStateException();
                }
            }
            
            if( this.listEntryProperty == null )
            {
                throw new IllegalStateException();
            }
        }
        
        @Override
        protected PropertyBinding createBinding( final Property property )
        {
            if( property.definition() == this.listEntryProperty )
            {
                return new ValuePropertyBinding()
                {
                    @Override
                    public String read()
                    {
                        return getValue();
                    }
                    
                    @Override
                    public void write( final String value )
                    {
                        setValue( value );
                    }
                };
            }
            
            return null;
        }
    }
    
}
