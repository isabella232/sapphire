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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.sapphire.modeling.annotations.ModelPropertyValidator;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ModelElementHandle<T extends IModelElement>
{
    private final IModelElement parent;
    private final ElementProperty property;
    private final ElementBindingImpl binding;
    private T element;
    private Boolean enabledState;
    private IStatus validationState;
    
    public ModelElementHandle( final IModelElement parent,
                               final ElementProperty property )
    {
        this.parent = parent;
        this.property = property;
        this.binding = parent.resource().binding( property );
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
                    remove();
                }
                
                if( this.element == null )
                {
                    final Resource resource = this.binding.create( t );
                    this.element = t.instantiate( this.parent, this.property, resource );
                    
                    refreshEnabledState();
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
    
    public IStatus validate()
    {
        synchronized( this )
        {
            return this.validationState;
        }
    }
    
    public boolean enabled()
    {
        synchronized( this )
        {
            return this.enabledState;
        }
    }
    
    public void remove()
    {
        boolean changed = false;

        synchronized( this )
        {
            if( this.element != null )
            {
                this.binding.remove();
                changed = refreshInternal();
            }
        }
        
        if( changed )
        {
            this.parent.notifyPropertyChangeListeners( this.property );
        }
    }
    
    public boolean removable()
    {
        synchronized( this )
        {
            return this.binding.removable();
        }
    }

    public void refresh()
    {
        final boolean changed = refreshInternal();
        
        if( changed )
        {
            this.parent.notifyPropertyChangeListeners( this.property );
        }
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
            
            changed = refreshEnabledState() || changed;
            changed = refreshValidationState() || changed;
            
            return changed;
        }
    }
    
    private boolean refreshEnabledState()
    {
        final Boolean oldEnabledState = this.enabledState;
        final Boolean newEnabledState = this.parent.service( this.property, EnablementService.class ).isEnabled();
        
        this.enabledState = newEnabledState;
        
        return ( oldEnabledState != null && ! oldEnabledState.equals( newEnabledState ) );
    }
    
    @SuppressWarnings( "unchecked" )
    
    private boolean refreshValidationState()
    {
        final IStatus oldValidationState = this.validationState;
        final SapphireMultiStatus newValidationState = new SapphireMultiStatus();
        
        if( enabled() )
        {
            final ModelPropertyValidator<T> validator = (ModelPropertyValidator<T>) this.property.getValidator();
            
            if( validator != null )
            {
                newValidationState.merge( validator.validate( this.element ) );
            }
            
            if( this.element != null )
            {
                newValidationState.merge( this.element.validate() );
            }
        }
        
        if( ! newValidationState.equals( oldValidationState ) )
        {
            this.validationState = newValidationState;
            return ( oldValidationState != null );
        }
        
        return false;
    }
    
}
