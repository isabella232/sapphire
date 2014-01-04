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
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.sapphire.util.IdentityHashSet;
import org.eclipse.sapphire.util.SetFactory;

/**
 * An index provides an efficient way to lookup elements in a list by a property value.
 * 
 * <p>To create an index, use the {@link ElementList#index(ValueProperty)} method.</p>
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class Index<T extends Element>
{
    @Text( "{0} property is already disposed." )
    private static LocalizableText propertyAlreadyDisposed;
    
    static
    {
        LocalizableText.init( Index.class );
    }

    private final ElementList<T> list;
    private final ValueProperty property;
    private Map<String,Object> keyToElements;
    private Map<Element,String> elementToKey;
    private Listener listener;
    private ListenerContext listeners;
    
    Index( final ElementList<T> list, final ValueProperty property )
    {
        this.list = list;
        this.property = property;
    }
    
    private void initialize()
    {
        if( this.keyToElements == null )
        {
            this.listener = new FilteredListener<PropertyContentEvent>()
            {
                @Override
                protected void handleTypedEvent( final PropertyContentEvent event )
                {
                    Index.this.handle( event );
                }
            };
            
            this.list.attach( this.listener );
            
            this.keyToElements = new HashMap<String,Object>();
            this.elementToKey = new IdentityHashMap<Element,String>();
            
            for( final Element element : this.list )
            {
                insert( element );
                
                element.attach( this.listener );
                element.property( this.property ).attach( this.listener );
            }
        }
    }
    
    /**
     * Returns the indexed list.
     * 
     * @return the indexed list
     */
    
    public ElementList<?> list()
    {
        return this.list;
    }
    
    /**
     * Returns the property that is used as the key by this index.
     * 
     * @return the property that is used as the key by this index
     */
    
    public ValueProperty property()
    {
        return this.property;
    }
    
    /**
     * Returns an element corresponding to the given key. If multiple elements are found, no guarantees
     * are made as to which of these elements will be returned.
     * 
     * @param key the key to use for the lookup
     * @return an element corresponding to the given key or null
     * @throws IllegalArgumentException if key is null
     * @throws IllegalStateException if the list property is disposed
     */
    
    @SuppressWarnings( "unchecked" )
    
    public T element( final String key )
    {
        if( key == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( this.list.root() )
        {
            assertNotDisposed();
            initialize();
            
            final Object obj = this.keyToElements.get( key );
            
            if( obj != null )
            {
                if( obj instanceof Element )
                {
                    return (T) obj;
                }
                else
                {
                    return ( (Set<T>) obj ).iterator().next();
                }
            }
        }
        
        return null;
    }
    
    /**
     * Returns all the elements corresponding to the given key.
     * 
     * @param key the key to use for the lookup
     * @return all the element corresponding to the given key or an empty set
     * @throws IllegalArgumentException if key is null
     * @throws IllegalStateException if the list property is disposed
     */
    
    @SuppressWarnings( "unchecked" )
    
    public Set<T> elements( final String key )
    {
        if( key == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( this.list.root() )
        {
            assertNotDisposed();
            initialize();
            
            final Object obj = this.keyToElements.get( key );
            
            if( obj != null )
            {
                if( obj instanceof Element )
                {
                    return SetFactory.singleton( (T) obj );
                }
                else
                {
                    return Collections.unmodifiableSet( new IdentityHashSet<T>( (Set<T>) obj ) );
                }
            }
        }
        
        return SetFactory.empty();
    }
    
    /**
     * Attaches a listener to this index.
     * 
     * @param listener the listener
     * @throws IllegalArgumentException if the listener is null
     * @throws IllegalStateException if the list property is disposed
     */
    
    public void attach( final Listener listener )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( this.list.root() )
        {
            assertNotDisposed();
            
            if( this.listeners == null )
            {
                this.listeners = new ListenerContext();
                this.listeners.coordinate( ( (ElementImpl) this.list.element() ).listeners() );
            }
            
            this.listeners.attach( listener );
        }
    }
    
    /**
     * Detaches a listener from this index.
     * 
     * @param listener the listener
     * @throws IllegalArgumentException if the listener is null
     */
    
    public void detach( final Listener listener )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( this.list.root() )
        {
            if( this.listeners != null )
            {
                this.listeners.detach( listener );
            }
        }
    }
    
    private void handle( final PropertyContentEvent event )
    {
        synchronized( this.list.root() )
        {
            boolean changed = false;
            
            final Property property = event.property();
            
            if( property instanceof Value )
            {
                final Element element = property.element();
                
                remove( element );
                insert( element );
                
                changed = true;
            }
            else
            {
                for( final Element element : this.list )
                {
                    if( ! this.elementToKey.containsKey( element ) )
                    {
                        insert( element );
                        
                        element.attach( this.listener );
                        element.property( this.property ).attach( this.listener );
                        
                        changed = true;
                    }
                }
                
                List<Element> disposed = null;
                
                for( final Element element : this.elementToKey.keySet() )
                {
                    if( element.disposed() )
                    {
                        if( disposed == null )
                        {
                            disposed = new ArrayList<Element>( 1 );
                        }
                        
                        disposed.add( element );
                    }
                }
                
                if( disposed != null )
                {
                    for( final Element element : disposed )
                    {
                        remove( element );
                    }
                    
                    changed = true;
                }
            }
            
            if( changed )
            {
                if( this.listeners != null )
                {
                    this.listeners.broadcast( new Event() );
                }
            }
        }
    }
    
    private void insert( final Element element )
    {
        if( element == null )
        {
            throw new IllegalStateException();
        }
        
        final String key = element.property( this.property ).text();
        
        if( key != null )
        {
            final Object object = this.keyToElements.get( key );
            
            if( object == null )
            {
                this.keyToElements.put( key, element );
            }
            else if( object instanceof Element )
            {
                final Set<Element> set = new IdentityHashSet<Element>();
                
                set.add( (Element) object );
                set.add( element );
                
                this.keyToElements.put( key, set );
            }
            else
            {
                @SuppressWarnings( "unchecked" )
                final Set<Element> set = (Set<Element>) object;
                
                set.add( element );
            }
            
            this.elementToKey.put( element, key );
        }
    }
    
    private void remove( final Element element )
    {
        if( element == null )
        {
            throw new IllegalStateException();
        }
        
        final String key = this.elementToKey.remove( element );
        
        if( key != null )
        {
            final Object object = this.keyToElements.get( key );
            
            if( object != null )
            {
                if( object instanceof Element )
                {
                    this.keyToElements.remove( key );
                }
                else
                {
                    final Set<?> set = (Set<?>) object;
                    
                    set.remove( element );
                    
                    if( set.size() == 1 )
                    {
                        this.keyToElements.put( key, set.iterator().next() );
                    }
                }
            }
        }
    }
    
    private void assertNotDisposed()
    {
        if( this.list.disposed() )
        {
            final String msg = propertyAlreadyDisposed.format( this.list.name() );
            throw new IllegalStateException( msg );
        }
    }
    
}
