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

@SuppressWarnings( "unchecked" )

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ModelElementList<T extends IModelElement>

    extends ModelParticle
    implements List<T>, IModelParticle

{
    private final ListProperty property;
    private ListBindingImpl binding;
    private List<IModelElement> data;
    private IStatus valres;
    private ModelElementListener listMemberListener;
    
    public ModelElementList( final IModelElement parent,
                             final ListProperty property )
    {
        super( parent, parent.resource() );
        
        this.property = property;
        this.data = Collections.emptyList();
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
    
    public final void init( final ListBindingImpl binding )
    {
        this.binding = binding;
        refresh( true );
    }

    @Override
    public IModelElement parent()
    {
        return (IModelElement) super.parent();
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
        return refresh( false );
    }
    
    private boolean refresh( final boolean isInitialRefresh )
    {
        if( this.binding == null )
        {
            throw new IllegalStateException();
        }
        
        boolean changed = false;
        
        synchronized( this )
        {
            final List<Resource> newResources = this.binding.read();
            final int newContentSize = newResources.size();
            
            boolean refreshNeeded;
            
            if( this.data.size() == newContentSize )
            {
                refreshNeeded = false;
                
                for( int i = 0; i < newContentSize; i++ )
                {
                    if( this.data.get( i ).resource() != newResources.get( i ) )
                    {
                        refreshNeeded = true;
                        break;
                    }
                }
            }
            else
            {
                refreshNeeded = true;
            }
            
            if( refreshNeeded )
            {
                final List<IModelElement> newContent = new ArrayList<IModelElement>( newContentSize );
                
                for( Resource resource : newResources )
                {
                    IModelElement modelElement = null;
                    
                    for( IModelElement x : this.data )
                    {
                        if( resource == x.resource() )
                        {
                            modelElement = x;
                            break;
                        }
                    }
                    
                    if( modelElement == null )
                    {
                        final ModelElementType type = this.binding.type( resource );
                        modelElement = type.instantiate( this, this.property, resource );
                    }
                    
                    newContent.add( modelElement );
                }
                
                for( IModelElement x : this.data )
                {
                    boolean retained = false;

                    for( IModelElement y : newContent )
                    {
                        if( x == y )
                        {
                            retained = true;
                            break;
                        }
                    }

                    if( ! retained )
                    {
                        try
                        {
                            x.dispose();
                        }
                        catch( Exception e )
                        {
                            SapphireModelingFrameworkPlugin.log( e );
                        }
                    }
                }
                
                this.data = newContent;
                
                for( T modelElement : this )
                {
                    modelElement.addListener( this.listMemberListener );
                }
                
                changed = true;
            }
        }
        
        if( changed && ! isInitialRefresh )
        {
            parent().notifyPropertyChangeListeners( this.property );
        }
        
        refreshValidationResult( ! isInitialRefresh );
        
        return changed;
    }
    
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
                parent().notifyPropertyChangeListeners( this.property );
            }
        }
    }
    
    public T addNewElement()
    {
        return addNewElement( this.property.getAllPossibleTypes().get( 0 ) );
    }
    
    public T addNewElement( final ModelElementType type )
    {
        T newElement = null;
        
        synchronized( this )
        {
            final Resource newResource = this.binding.add( type );
            
            refresh();
            
            for( IModelElement element : this.data )
            {
                if( element.resource() == newResource )
                {
                    newElement = (T) element;
                    break;
                }
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
                final T previousModelElement = (T) this.data.get( index - 1 );
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
                final T nextModelElement = (T) this.data.get( index + 1 );
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

            this.binding.swap( a.resource(), b.resource() );
            refresh();
        }
    }

    public synchronized boolean remove( final Object object )
    {
        if( contains( object ) )
        {
            final Resource resource = ( (IModelElement) object ).resource();
            this.binding.remove( resource );
            refresh();
            
            return true;
        }
        
        return false;
    }

    public synchronized T remove( final int index )
    {
        final IModelElement element = this.data.get( index );
        remove( element );
        return (T) element;
    }

    public synchronized boolean removeAll( final Collection<?> collection )
    {
        boolean changed = false;
        
        for( Object object : collection )
        {
            changed = remove( object ) || changed;
        }
        
        return changed;
    }

    public synchronized boolean retainAll( final Collection<?> collection )
    {
        boolean changed = false;
        
        for( IModelElement element : this )
        {
            if( ! collection.contains( element ) )
            {
                changed = remove( element ) || changed;
            }
        }
        
        return changed;
    }

    public synchronized void clear()
    {
        for( IModelElement element : this )
        {
            remove( element );
        }
    }

    public synchronized T get( final int index )
    {
        return (T) this.data.get( index );
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
        return new Itr<T>( (Iterator<T>) this.data.iterator() );
    }

    public synchronized ListIterator<T> listIterator()
    {
        return new ListItr<T>( (ListIterator<T>) this.data.listIterator() );
    }

    public synchronized ListIterator<T> listIterator( final int index )
    {
        return new ListItr<T>( (ListIterator<T>) this.data.listIterator( index ) );
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
