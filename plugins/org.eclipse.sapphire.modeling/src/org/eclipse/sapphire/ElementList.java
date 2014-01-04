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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.SortedSet;

import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelPath.AllDescendentsSegment;
import org.eclipse.sapphire.modeling.ModelPath.PropertySegment;
import org.eclipse.sapphire.modeling.ModelPath.TypeFilterSegment;
import org.eclipse.sapphire.services.PossibleTypesService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ElementList<T extends Element> extends Property implements List<T>
{
    private List<T> content;
    private Map<ValueProperty,Index<T>> indexes;
    
    public ElementList( final Element element, final ListProperty property )
    {
        super( element, property );
    }
    
    /**
     * Returns a reference to ElementList.class that is parameterized with the given type.
     * 
     * <p>Example:</p>
     * 
     * <p><code>Class&lt;ElementList&lt;Item>> cl = ElementList.of( Item.class );</code></p>
     *  
     * @param type the type
     * @return a reference to ElementList.class that is parameterized with the given type
     */
    
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    
    public static <TX extends Element> Class<ElementList<TX>> of( final Class<TX> type )
    {
        return (Class) ElementList.class;
    }
    
    @Override
    public void refresh()
    {
        synchronized( root() )
        {
            init();
    
            refreshContent( false );
            
            if( this.content != null )
            {
                for( T element : this.content )
                {
                    element.refresh();
                }
            }
            
            refreshEnablement( false );
            refreshValidation( false );
        }
    }
    
    private void refreshContent( final boolean onlyIfNotInitialized )
    {
        boolean initialized = ( ( this.initialization & CONTENT_INITIALIZED ) != 0 );
        
        if( ! initialized || ! onlyIfNotInitialized )
        {
            final ListPropertyBinding binding = binding();
            final List<? extends Resource> freshResources = binding.read();
            final int freshContentSize = freshResources.size();
            
            boolean proceed;
            
            initialized = ( ( this.initialization & CONTENT_INITIALIZED ) != 0 );
            
            if( initialized )
            {
                if( this.content.size() == freshContentSize )
                {
                    proceed = false;
                    
                    for( int i = 0; i < freshContentSize; i++ )
                    {
                        if( this.content.get( i ).resource() != freshResources.get( i ) )
                        {
                            proceed = true;
                            break;
                        }
                    }
                }
                else
                {
                    proceed = true;
                }
            }
            else
            {
                proceed = true;
            }
            
            if( proceed )
            {
                final List<T> freshContent = new ArrayList<T>( freshContentSize );
                
                for( Resource resource : freshResources )
                {
                    T element = null;
                    
                    if( this.content != null )
                    {
                        for( T x : this.content )
                        {
                            if( resource == x.resource() )
                            {
                                element = x;
                                break;
                            }
                        }
                    }
                    
                    if( element == null )
                    {
                        final ElementType type = binding.type( resource );
                        element = type.instantiate( this, resource );
                    }
                    
                    freshContent.add( element );
                }
                
                final List<T> toBeDisposed = new ArrayList<T>( 1 );
                
                if( this.content != null )
                {
                    for( T x : this.content )
                    {
                        boolean retained = false;
    
                        for( T y : freshContent )
                        {
                            if( x == y )
                            {
                                retained = true;
                                break;
                            }
                        }
    
                        if( ! retained )
                        {
                            toBeDisposed.add( x );
                        }
                    }
                }
                
                PropertyContentEvent event = null;
                
                this.content = freshContent;
                
                if( initialized )
                {
                    event = new PropertyContentEvent( this );
                }
                else
                {
                    this.initialization |= CONTENT_INITIALIZED;
                }
                
                for( Element x : toBeDisposed )
                {
                    try
                    {
                        x.dispose();
                    }
                    catch( Exception e )
                    {
                        Sapphire.service( LoggingService.class ).log( e );
                    }
                }
                
                broadcast( event );
            }
        }
    }
    
    @Override
    public ListProperty definition()
    {
        return (ListProperty) super.definition();
    }
    
    @Override
    protected ListPropertyBinding binding()
    {
        return (ListPropertyBinding) super.binding();
    }
    
    @Override
    public void attach( final Listener listener, final ModelPath path )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( path == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            assertNotDisposed();
            
            if( path.length() > 0 )
            {
                final ModelPath.Segment head = path.head();
                
                if( head instanceof AllDescendentsSegment || head instanceof PropertySegment || head instanceof TypeFilterSegment )
                {
                    attach( listener );
                    attach( new PropagationListener( listener, path ) );
                    
                    for( Element element : this )
                    {
                        element.attach( listener, path );
                    }
                    
                    return;
                }
            }
            
            super.attach( listener, path );
        }
    }
    
    @Override
    public void detach( final Listener listener, final ModelPath path )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( path == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            if( path.length() > 0 )
            {
                final ModelPath.Segment head = path.head();
                
                if( head instanceof AllDescendentsSegment || head instanceof PropertySegment || head instanceof TypeFilterSegment )
                {
                    detach( listener );
                    detach( new PropagationListener( listener, path ) );
                    
                    for( Element element : this )
                    {
                        element.detach( listener, path );
                    }
                    
                    return;
                }
            }
            
            super.detach( listener, path );
        }
    }
    
    public T insert()
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            ensureNotReadOnly();
            
            return insert$( (ElementType) null, size$() );
        }
    }
    
    public T insert( final int position )
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            ensureNotReadOnly();
            
            return insert$( (ElementType) null, position );
        }
    }
    
    public T insert( final ElementType type )
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            ensureNotReadOnly();
            
            return insert$( type, size$() );
        }
    }
    
    public <C extends Element> C insert( final Class<C> cl )
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            ensureNotReadOnly();
            
            return insert$( cl, size$() );
        }
    }
    
    public T insert( final ElementType type, final int position )
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            ensureNotReadOnly();
            
            return insert$( type, position );
        }
    }
    
    private T insert$( final ElementType type, final int position )
    {
        final SortedSet<ElementType> possible = service( PossibleTypesService.class ).types();
        
        ElementType t = type;
        
        if( t == null )
        {
            if( possible.size() > 1 )
            {
                throw new IllegalArgumentException();
            }
            
            t = possible.first();
        }
        else if( ! possible.contains( t ) )
        {
            throw new IllegalArgumentException();
        }
        
        final Resource resource = binding().insert( t, position );
        
        refresh();
        
        T element = null;
        
        for( T x : this.content )
        {
            if( x.resource() == resource )
            {
                element = x;
                element.initialize();
                break;
            }
        }
        
        if( element == null )
        {
            throw new IllegalStateException();
        }
        
        return element;
    }
    
    public <C extends Element> C insert( final Class<C> cl, final int position )
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            ensureNotReadOnly();
            
            return insert$( cl, position );
        }
    }
    
    @SuppressWarnings( "unchecked" )
    
    private <C extends Element> C insert$( final Class<C> cl, final int position )
    {
        ElementType type = null;
        
        if( cl != null )
        {
            type = ElementType.read( cl );
            
            if( type == null )
            {
                throw new IllegalArgumentException();
            }
        }
        
        return (C) insert$( type, position );
    }
    
    @Override
    public void copy( final Element source )
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            ensureNotReadOnly();
            
            if( source == null )
            {
                throw new IllegalArgumentException();
            }
        
            final Property p = source.property( name() );
            
            if( p instanceof ElementList )
            {
                clear$();
                
                final SortedSet<ElementType> possibleTypes = service( PossibleTypesService.class ).types();
                
                for( Element sourceChildElement : (ElementList<?>) p )
                {
                    final ElementType sourceChildElementType = sourceChildElement.type();
                    
                    if( possibleTypes.contains( sourceChildElementType ) )
                    {
                        final Element targetChildElement = insert$( sourceChildElement.type(), size$() );
                        targetChildElement.copy( sourceChildElement );
                    }
                }
            }
        }
    }

    public void move( final Element element, final int position )
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            ensureNotReadOnly();
            
            if( position < 0 || position > size() )
            {
                throw new IllegalArgumentException();
            }
            
            final int oldPosition = indexOf$( element );
            
            if( oldPosition == -1 )
            {
                throw new IllegalArgumentException();
            }
            
            if( position != oldPosition )
            {
                binding().move( element.resource(), position );
                refresh();
            }
        }
    }

    public void moveUp( final Element element )
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            ensureNotReadOnly();
            
            final int index = indexOf$( element );
            
            if( index == -1 )
            {
                throw new IllegalArgumentException();
            }
            
            if( index > 0 )
            {
                final T previous = this.content.get( index - 1 );
                swap$( element, previous );
            }
        }
    }
    
    public void moveDown( final Element element )
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            ensureNotReadOnly();
            
            final int index = indexOf$( element );
            
            if( index == -1 )
            {
                throw new IllegalArgumentException();
            }
            
            if( index < this.content.size() - 1 )
            {
                final T next = this.content.get( index + 1 );
                swap$( element, next );
            }
        }
    }
    
    public void swap( final Element a, final Element b )
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            ensureNotReadOnly();
            
            swap$( a, b );
        }
    }

    private void swap$( final Element a, final Element b )
    {
        final int aPosition = indexOf$( a );
        final int bPosition = indexOf$( b );
        
        if( aPosition == -1 || bPosition == -1 )
        {
            throw new IllegalArgumentException();
        }
        
        if( aPosition != bPosition )
        {
            final ListPropertyBinding binding = binding();
            binding.move( a.resource(), bPosition );
            binding.move( b.resource(), aPosition );
            refresh();
        }
    }
    
    public boolean remove( final Object object )
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            ensureNotReadOnly();
            
            return remove$( object );
        }
    }

    private boolean remove$( final Object object )
    {
        if( contains$( object ) )
        {
            final Resource resource = ( (Element) object ).resource();
            binding().remove( resource );
            refresh();
            
            return true;
        }
        
        return false;
    }

    public T remove( final int index )
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            ensureNotReadOnly();
            
            final T element = this.content.get( index );
            remove$( element );
            
            return element;
        }
    }

    public boolean removeAll( final Collection<?> collection )
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            ensureNotReadOnly();
        
            boolean changed = false;
            
            for( Object object : collection )
            {
                changed = remove$( object ) || changed;
            }
            
            return changed;
        }
    }

    public boolean retainAll( final Collection<?> collection )
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            ensureNotReadOnly();
            
            boolean changed = false;
            
            for( T element : this )
            {
                if( ! collection.contains( element ) )
                {
                    changed = remove$( element ) || changed;
                }
            }
            
            return changed;
        }
    }
    
    @Override
    public void clear()
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            ensureNotReadOnly();
            
            clear$();
        }
    }

    private void clear$()
    {
        for( T element : this.content )
        {
            remove$( element );
        }
    }

    public T get( final int index )
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );

            return this.content.get( index );
        }
    }

    public int indexOf( final Object object )
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            
            return indexOf$( object );
        }
    }

    private int indexOf$( final Object object )
    {
        int index = -1;
        
        for( int i = 0, n = this.content.size(); i < n; i++ )
        {
            if( this.content.get( i ) == object )
            {
                index = i;
                break;
            }
        }
        
        return index;
    }

    public int lastIndexOf( final Object object )
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );

            int index = -1;
            
            for( int i = 0, n = this.content.size(); i < n; i++ )
            {
                if( this.content.get( i ) == object )
                {
                    index = i;
                }
            }
            
            return index;
        }
    }

    public boolean contains( final Object object )
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            
            return contains$( object );
        }
    }

    private boolean contains$( final Object object )
    {
        for( Object x : this.content )
        {
            if( x == object )
            {
                return true;
            }
        }
        
        return false;
    }

    public boolean containsAll( final Collection<?> collection )
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            
            for( Object x : collection )
            {
                if( ! contains$( x ) )
                {
                    return false;
                }
            }
            
            return true;
        }
    }

    public boolean isEmpty()
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            
            return this.content.isEmpty();
        }
    }
    
    @Override
    public boolean empty()
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            
            return this.content.isEmpty();
        }
    }

    public int size()
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );

            return size$();
        }
    }

    private int size$()
    {
        return this.content.size();
    }
    
    public Iterator<T> iterator()
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );

            return new Itr<T>( this.content.iterator() );
        }
    }

    public ListIterator<T> listIterator()
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            
            return new ListItr<T>( this.content.listIterator() );
        }
    }

    public ListIterator<T> listIterator( final int index )
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            
            return new ListItr<T>( this.content.listIterator( index ) );
        }
    }

    public List<T> subList( final int fromIndex,
                            final int toIndex )
    {
        throw new UnsupportedOperationException();
    }

    public Object[] toArray()
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );

            return this.content.toArray();
        }
    }

    public <E> E[] toArray( E[] array )
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            
            return this.content.toArray( array );
        }
    }
    
    public boolean add( final T object )
    {
        throw new UnsupportedOperationException();
    }

    public void add( final int index, final T element )
    {
        throw new UnsupportedOperationException();
    }

    public boolean addAll( final Collection<? extends T> collection )
    {
        throw new UnsupportedOperationException();
    }

    public boolean addAll( final int index, final Collection<? extends T> collection )
    {
        throw new UnsupportedOperationException();
    }

    public T set( final int index, final T element )
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Returns an index with the specified value property as the key. The index is created if it doesn't exist already.
     * 
     * @param property the key property
     * @return the index
     * @throws IllegalArgumentException if the property is null; if the property does not belong to the list entry type
     */
    
    public Index<T> index( final ValueProperty property )
    {
        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        return index( property.name() );
    }
    
    /**
     * Returns an index with the specified value property as the key. The index is created if it doesn't exist already.
     * 
     * @param property the key property
     * @return the index
     * @throws IllegalArgumentException if the property is null; if the property does not exist in the list entry type;
     *   if the property is a path; if the property is not a value property
     */
    
    public Index<T> index( final String property )
    {
        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( property.indexOf( '/' ) != -1 )
        {
            throw new IllegalArgumentException();
        }
        
        final ElementType entryType = definition().getType();
        final PropertyDef p = entryType.property( property );
        
        if( p == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( ! ( p instanceof ValueProperty ) )
        {
            throw new IllegalArgumentException();
        }
        
        final ValueProperty vp = (ValueProperty) p;
        
        synchronized( root() )
        {
            assertNotDisposed();
            
            if( this.indexes == null )
            {
                this.indexes = new HashMap<ValueProperty,Index<T>>();
            }
            
            Index<T> index = this.indexes.get( vp );
            
            if( index == null )
            {
                index = new Index<T>( this, vp );
                this.indexes.put( vp, index );
            }
            
            return index;
        }
    }
    
    @Override
    protected void disposeOther()
    {
        if( this.content != null )
        {
            for( final T element : this.content )
            {
                element.dispose();
            }
            
            this.content = null;
        }
        
        this.indexes = null;
    }

    private void ensureNotReadOnly()
    {
        if( definition().isReadOnly() )
        {
            throw new UnsupportedOperationException();
        }
    }
    
    private static final class PropagationListener extends FilteredListener<PropertyContentEvent>
    {
        private final Listener listener;
        private final ModelPath path;
        
        public PropagationListener( final Listener listener, final ModelPath path )
        {
            this.listener = listener;
            this.path = path;
        }
        
        @Override
        public boolean equals( final Object obj )
        {
            if( obj instanceof PropagationListener )
            {
                final PropagationListener pl = (PropagationListener) obj;
                return this.listener.equals( pl.listener ) && this.path.equals( pl.path );
            }
            
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return this.listener.hashCode() ^ this.path.hashCode();
        }
        
        @Override
        protected void handleTypedEvent( final PropertyContentEvent event )
        {
            for( Element element : (ElementList<?>) event.property() )
            {
                element.attach( this.listener, this.path );
            }
        }
    }

    private static class Itr<T> implements Iterator<T>
    {
        private final Iterator<T> baseIterator;
        
        public Itr( final Iterator<T> baseIterator )
        {
            this.baseIterator = baseIterator;
        }
        
        public boolean hasNext()
        {
            return this.baseIterator.hasNext();
        }

        public T next()
        {
            return this.baseIterator.next();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
    
    private static final class ListItr<T> extends Itr<T> implements ListIterator<T>
    {
        private final ListIterator<T> baseIterator;
        
        public ListItr( final ListIterator<T> baseIterator )
        {
            super( baseIterator );
            this.baseIterator = baseIterator;
        }

        public int nextIndex()
        {
            return this.baseIterator.nextIndex();
        }

        public boolean hasPrevious()
        {
            return this.baseIterator.hasPrevious();
        }

        public T previous()
        {
            return this.baseIterator.previous();
        }

        public int previousIndex()
        {
            return this.baseIterator.previousIndex();
        }

        public void add( final T object )
        {
            throw new UnsupportedOperationException();
        }

        public void set( final T object )
        {
            throw new UnsupportedOperationException();
        }
    }

}
