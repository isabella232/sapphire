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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.sapphire.modeling.annotations.ModelPropertyValidator;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ModelElementList<T extends IModelElement>

    extends ModelParticle
    implements List<T>, IModelParticle

{
    private final ListProperty property;
    private ModelElementListController<T> controller;
    private List<T> data;
    private boolean copyOnNextChange;
    private boolean ignoreRefresh;
    private IStatus valres;
    private ModelElementListener listMemberListener;
    
    public ModelElementList( final IModelElement parent,
                             final ListProperty property )
    {
        super( parent );
        
        this.property = property;
        this.data = Collections.emptyList();
        this.copyOnNextChange = true;
        this.ignoreRefresh = false;
        this.valres = null;
        
        this.listMemberListener = new ModelElementListener()
        {
            @Override
            public void validationStateChanged( final ValidationStateChangeEvent event )
            {
                refreshValidationResult( true );
            }
        };
    }
    
    public final void init( final ModelElementListController<T> controller )
    {
        this.controller = controller;
    }

    @Override
    public IModelElement getParent()
    {
        return (IModelElement) super.getParent();
    }
    
    public ListProperty getParentProperty()
    {
        return this.property;
    }
    
    public synchronized IStatus validate()
    {
        if( this.valres == null )
        {
            refreshValidationResult( false );
        }
        
        return this.valres;
    }

    public boolean refresh()
    {
        boolean changed = false;
        
        synchronized( this )
        {
            if( ! this.ignoreRefresh )
            {
                final List<T> oldListElements = Collections.unmodifiableList( this.data );
                final List<T> newListElements = new ArrayList<T>( this.controller.refresh( oldListElements ) );
                
                boolean refreshNeeded = false;
                
                if( newListElements != oldListElements )
                {
                    final int newListElementsSize = newListElements.size();
                    
                    if( newListElementsSize != oldListElements.size() )
                    {
                        refreshNeeded = true;
                    }
                    else
                    {
                        for( int i = 0; i < newListElementsSize; i++ )
                        {
                            if( newListElements.get( i ) != oldListElements.get( i ) )
                            {
                                refreshNeeded = true;
                                break;
                            }
                        }
                    }
                }
                
                if( refreshNeeded )
                {
                    for( T modelElement : oldListElements )
                    {
                        boolean retained = false;

                        for( T x : newListElements )
                        {
                            if( x == modelElement )
                            {
                                retained = true;
                                break;
                            }
                        }

                        if( ! retained )
                        {
                            try
                            {
                                modelElement.dispose();
                            }
                            catch( Exception e )
                            {
                                SapphireModelingFrameworkPlugin.log( e );
                            }
                        }
                    }
                    
                    this.data = newListElements;
                    
                    for( T modelElement : this )
                    {
                        modelElement.addListener( this.listMemberListener );
                    }
                    
                    this.copyOnNextChange = false;
                    changed = true;
                }
            }
        }
        
        if( changed )
        {
            getParent().notifyPropertyChangeListeners( this.property );
        }
        
        refreshValidationResult( true );
        
		return changed;
    }
    
    @SuppressWarnings( "unchecked" )
    
    private void refreshValidationResult( final boolean notifyListenersIfChanged )
    {
        final SapphireMultiStatus st = new SapphireMultiStatus();
        
        final ModelPropertyValidator<?> validator = this.property.getValidator();
        
        if( validator != null )
        {
            st.add( ( (ModelPropertyValidator<Object>) validator ).validate( this ) );
        }
        
        for( T item : this )
        {
            st.add( item.validate() );
        }
        
        if( this.valres == null )
        {
            this.valres = st;
        }
        else if( ! this.valres.equals( st ) )
        {
            this.valres = st;
            
            if( notifyListenersIfChanged )
            {
                getParent().notifyPropertyChangeListeners( this.property );
            }
        }
    }
    
    public void handleElementRemovedEvent()
    {
        refresh();
        this.controller.handleElementRemovedEvent();
    }
    
    public T addNewElement()
    {
        return addNewElement( this.property.getAllPossibleTypes().get( 0 ) );
    }
    
    public T addNewElement( final ModelElementType type )
    {
        final T newElement;
        
        synchronized( this )
        {
            this.ignoreRefresh = true;
            
            try
            {
                newElement = this.controller.createNewElement( type );
                
                if( this.copyOnNextChange )
                {
                    this.data = new ArrayList<T>( this.data );
                    this.copyOnNextChange = false;
                }

                newElement.addListener( this.listMemberListener );
                
                // On the surface, the following add call is incorrect due to an assumption that
                // all list controllers will always add the new item to the end of the list. However,
                // this method ends with a refresh call and if position at the end of the list is
                // not correct, it will be adjusted.
                
                this.data.add( newElement );
            }
            finally
            {
                this.ignoreRefresh = false;
            }
            
            if( refresh() == false )
            {
                getParent().notifyPropertyChangeListeners( this.property );
            }
        }
        
        return newElement;
    }
    
    public void moveUp( final T modelElement )
    {
        synchronized( this )
        {
            final int index = indexOf( modelElement );
            
            if( index == -1 )
            {
                throw new IllegalArgumentException();
            }
            
            if( index > 0 )
            {
                final T previousModelElement = this.data.get( index - 1 );
                swap( modelElement, previousModelElement );
            }
        }
    }
    
    public void moveDown( final T modelElement )
    {
        synchronized( this )
        {
            final int index = indexOf( modelElement );
            
            if( index == -1 )
            {
                throw new IllegalArgumentException();
            }
            
            if( index < this.data.size() - 1 )
            {
                final T nextModelElement = this.data.get( index + 1 );
                swap( modelElement, nextModelElement );
            }
        }
    }
    
    public void swap( final T a,
                      final T b )
    {
        synchronized( this )
        {
            if( this.data.indexOf( a ) == -1 || this.data.indexOf( b ) == -1 )
            {
                throw new IllegalArgumentException();
            }

            this.ignoreRefresh = true;
            
            try
            {
                this.controller.swap( a, b );
            }
            finally
            {
                this.ignoreRefresh = false;
            }
            
            refresh();
        }
    }

    public void clear()
    {
        List<T> entries = new ArrayList<T>(this);

        for( T entry : entries )
        {
            ( (IRemovable) entry ).remove();
        }
        
        refresh();
    }

    public synchronized T get( final int index )
    {
        return this.data.get( index );
    }

    public synchronized int indexOf( final Object object )
    {
        return this.data.indexOf( object );
    }

    public synchronized int lastIndexOf( final Object object )
    {
        return this.data.lastIndexOf( object );
    }

    public synchronized boolean contains( final Object object )
    {
        return this.data.contains( object );
    }

    public synchronized boolean containsAll( final Collection<?> collection )
    {
        return this.data.containsAll( collection );
    }

    public synchronized boolean isEmpty()
    {
        return this.data.isEmpty();
    }

    public synchronized int size()
    {
        return this.data.size();
    }

    public synchronized Iterator<T> iterator()
    {
        this.copyOnNextChange = true;
        return new Itr<T>( this.data.iterator() );
    }

    public synchronized ListIterator<T> listIterator()
    {
        this.copyOnNextChange = true;
        return new ListItr<T>( this.data.listIterator() );
    }

    public synchronized ListIterator<T> listIterator( final int index )
    {
        this.copyOnNextChange = true;
        return new ListItr<T>( this.data.listIterator( index ) );
    }

    public List<T> subList( final int fromIndex,
                            final int toIndex )
    {
        throw new UnsupportedOperationException();
    }

    public synchronized Object[] toArray()
    {
        return this.data.toArray();
    }

    public synchronized <E> E[] toArray( E[] array )
    {
        return this.data.toArray( array );
    }
    
    public boolean add( final T object )
    {
        throw new UnsupportedOperationException();
    }

    public void add( final int index,
                     final T element )
    {
        throw new UnsupportedOperationException();
    }

    public boolean addAll( final Collection<? extends T> collection )
    {
        throw new UnsupportedOperationException();
    }

    public boolean addAll( final int index,
                           final Collection<? extends T> collection )
    {
        throw new UnsupportedOperationException();
    }

    public boolean remove( final Object object )
    {
        throw new UnsupportedOperationException();
    }

    public T remove( final int index )
    {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll( final Collection<?> collection )
    {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll( final Collection<?> collection )
    {
        throw new UnsupportedOperationException();
    }

    public T set( final int index,
                  final T element )
    {
        throw new UnsupportedOperationException();
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
