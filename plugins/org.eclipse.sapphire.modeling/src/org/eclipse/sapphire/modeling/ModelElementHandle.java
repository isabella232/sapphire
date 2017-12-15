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

import java.util.List;


/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ModelElementHandle<T extends IModelElement>
{
    private final IModelElement parent;
    private final ElementProperty property;
    private final ElementBindingImpl binding;
    private final ModelPropertyListener listener;
    private T element;
    private Status validationStateLocal;
    private Status validationStateFull;
    
    public ModelElementHandle( final IModelElement parent,
                               final ElementProperty property )
    {
        this.parent = parent;
        this.property = property;
        this.binding = parent.resource().binding( property );
        
        this.listener = new ModelPropertyListener()
        {
            @Override
            public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
            {
                refresh();
            }
        };
    }
    
    public void init()
    {
        refreshInternal();
    }
    
    public IModelElement root()
    {
        return (IModelElement) this.parent.root();
    }
    
    public IModelElement parent()
    {
        return this.parent;
    }
    
    public T element()
    {
        return element( false );
    }
    
    public T element( final boolean createIfNecessary )
    {
        if( createIfNecessary )
        {
            final List<ModelElementType> types = this.property.getAllPossibleTypes();
            
            if( types.size() > 1 )
            {
                throw new IllegalArgumentException();
            }
            
            return element( true, types.get( 0 ) );
        }
        
        return element( false, null );
    }
    
    public T element( final boolean createIfNecessary,
                      final ModelElementType type )
    {
        if( type != null && ! this.property.getAllPossibleTypes().contains( type ) )
        {
            throw new IllegalArgumentException();
        }
        
        boolean changed = false;
        
        if( createIfNecessary )
        {
            ModelElementType t = type;
            
            if( t == null )
            {
                final List<ModelElementType> types = this.property.getAllPossibleTypes();
                
                if( types.size() > 1 )
                {
                    throw new IllegalArgumentException();
                }
                
                t = types.get( 0 );
            }
            
            synchronized( this )
            {
                if( this.element == null )
                {
                    refresh();
                }
                
                if( this.element != null && this.element.getModelElementType() != t )
                {
                    removeInternal();
                }
                
                if( this.element == null )
                {
                    final Resource resource = this.binding.create( t );
                    this.element = t.instantiate( this.parent, this.property, resource );
                    
                    for( ModelProperty property : t.getProperties() )
                    {
                        this.element.addListener( this.listener, property.getName() );
                    }
                    
                    refreshValidationState();
                    
                    changed = true;
                }
            }
        }
        else
        {
            synchronized( this )
            {
                if( this.element != null && type != null && this.element.getModelElementType() != type )
                {
                    throw new IllegalArgumentException();
                }
            }
        }
        
        if( changed )
        {
            this.parent.notifyPropertyChangeListeners( this.property );
        }
        
        return this.element;
    }
    
    public Status validate()
    {
        return validate( true );
    }
    
    public Status validate( final boolean includeChildValidation )
    {
        synchronized( this )
        {
            return ( includeChildValidation ? this.validationStateFull : this.validationStateLocal );
        }
    }
    
    public boolean enabled()
    {
        synchronized( this )
        {
            return this.parent.isPropertyEnabled( this.property );
        }
    }
    
    public void remove()
    {
        final boolean changed = removeInternal();
        
        if( changed )
        {
            this.parent.notifyPropertyChangeListeners( this.property );
        }
    }
    
    private boolean removeInternal()
    {
        synchronized( this )
        {
            boolean changed = false;

            if( this.element != null )
            {
                this.binding.remove();
                changed = refreshInternal();
            }
            
            return changed;
        }
    }
    
    public boolean removable()
    {
        synchronized( this )
        {
            return this.binding.removable();
        }
    }

    public boolean refresh()
    {
        final boolean changed = refreshInternal();
        
        if( changed )
        {
            this.parent.notifyPropertyChangeListeners( this.property );
        }
        
        return changed;
    }
    
    private boolean refreshInternal()
    {
        synchronized( this )
        {
            boolean changed = false;
            
            final Resource oldResource = ( this.element == null ? null : this.element.resource() );
            final Resource newResource = this.binding.read();
            
            if( newResource != oldResource )
            {
                if( newResource == null )
                {
                    this.element = null;
                }
                else
                {
                    final ModelElementType type = this.binding.type( newResource );
                    this.element = type.instantiate( this.parent, this.property, newResource );
                }
                
                changed = true;
            }
            
            changed = refreshValidationState() || changed;
            
            return changed;
        }
    }
    
    private boolean refreshValidationState()
    {
        final Status oldValidationStateFull = this.validationStateFull;
        final Status.CompositeStatusFactory newValidationStateLocalFactory = Status.factoryForComposite();
        final Status.CompositeStatusFactory newValidationStateFullFactory = Status.factoryForComposite();
        
        if( enabled() )
        {
            for( ModelPropertyValidationService<?> svc : parent().services( this.property, ModelPropertyValidationService.class ) )
            {
                Status st = null;
                
                try
                {
                    st = svc.validate();
                }
                catch( Exception e )
                {
                    LoggingService.log( e );
                }
                
                if( st != null )
                {
                    newValidationStateLocalFactory.merge( st );
                    newValidationStateFullFactory.merge( st );
                }
            }
            
            if( this.element != null )
            {
                newValidationStateFullFactory.merge( this.element.validate() );
            }
        }
        
        final Status newValidationStateLocal = newValidationStateLocalFactory.create();
        final Status newValidationStateFull = newValidationStateFullFactory.create();

        if( ! newValidationStateFull.equals( oldValidationStateFull ) )
        {
            this.validationStateLocal = newValidationStateLocal;
            this.validationStateFull = newValidationStateFull;
            return ( oldValidationStateFull != null );
        }
        
        return false;
    }
    
}
