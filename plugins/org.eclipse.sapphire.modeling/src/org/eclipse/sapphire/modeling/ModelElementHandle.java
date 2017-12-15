/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [376531] Need ability to distinguish between switch among heterogeneous elements 
 ******************************************************************************/

package org.eclipse.sapphire.modeling;

import java.util.SortedSet;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.services.PossibleTypesService;
import org.eclipse.sapphire.services.ValidationAggregationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ModelElementHandle<T extends IModelElement>
{
    private final ModelElement parent;
    private final ElementProperty property;
    private final PossibleTypesService possibleTypesService;
    private final ElementBindingImpl binding;
    private T element;
    private Status validationStateLocal;
    private Status validationStateFull;
    
    public ModelElementHandle( final IModelElement parent,
                               final ElementProperty property )
    {
        this.parent = (ModelElement) parent;
        this.property = property;
        this.possibleTypesService = parent.service( property, PossibleTypesService.class );
        this.binding = parent.resource().binding( property );
    }
    
    public void init()
    {
        refresh();
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
            final SortedSet<ModelElementType> possible = this.possibleTypesService.types();
            
            if( possible.size() > 1 )
            {
                throw new IllegalArgumentException();
            }
            
            return element( true, possible.first() );
        }
        
        return element( false, null );
    }
    
    public T element( final boolean createIfNecessary,
                      final ModelElementType type )
    {
        final SortedSet<ModelElementType> possible = this.possibleTypesService.types();
        
        if( type != null && ! possible.contains( type ) )
        {
            throw new IllegalArgumentException();
        }
        
        boolean changed = false;
        
        if( createIfNecessary )
        {
            ModelElementType t = type;
            
            if( t == null )
            {
                if( possible.size() > 1 )
                {
                    throw new IllegalArgumentException();
                }
                
                t = possible.first();
            }
            
            synchronized( this )
            {
                if( this.element == null )
                {
                    refresh();
                }
                
                if( this.element == null || this.element.type() != t )
                {
                    final Resource resource = this.binding.create( t );
                    this.element = t.instantiate( this.parent, this.property, resource );
                    this.element.initialize();
                    
                    this.element.attach
                    (
                        new FilteredListener<ElementValidationEvent>()
                        {
                            @Override
                            protected void handleTypedEvent( final ElementValidationEvent event )
                            {
                                refreshValidationResult();
                            }
                        }
                    );
                    
                    changed = true;
                }
            }
        }
        else
        {
            synchronized( this )
            {
                if( this.element != null && type != null && this.element.type() != type )
                {
                    throw new IllegalArgumentException();
                }
            }
        }
        
        if( changed )
        {
            if( ! ( this.property instanceof ImpliedElementProperty ) ) 
            {
                this.parent.broadcastPropertyContentEvent( this.property );
            }
            
            refreshValidationResult();
        }
        
        return this.element;
    }
    
    public Status validation()
    {
        return validation( true );
    }
    
    public Status validation( final boolean includeChildValidation )
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
            return this.parent.enabled( this.property );
        }
    }
    
    public boolean remove()
    {
        synchronized( this )
        {
            boolean changed = false;

            if( this.element != null )
            {
                this.binding.remove();
                changed = refresh();
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
        boolean changed = false;
        
        synchronized( this )
        {
            final Resource oldResource = ( this.element == null ? null : this.element.resource() );
            final Resource newResource = this.binding.read();
            
            if( newResource != oldResource )
            {
                if( this.element != null )
                {
                    try
                    {
                        this.element.dispose();
                    }
                    catch( Exception e )
                    {
                        LoggingService.log( e );
                    }
                }
                
                if( newResource == null )
                {
                    this.element = null;
                }
                else
                {
                    final ModelElementType type = this.binding.type( newResource );
                    this.element = type.instantiate( this.parent, this.property, newResource );
                    
                    this.element.attach
                    (
                        new FilteredListener<ElementValidationEvent>()
                        {
                            @Override
                            protected void handleTypedEvent( final ElementValidationEvent event )
                            {
                                refreshValidationResult();
                            }
                        }
                    );
                }
                
                changed = true;
            }
        }
        
        if( changed )
        {
            if( ! ( this.property instanceof ImpliedElementProperty ) )
            {
                this.parent.broadcastPropertyContentEvent( this.property );
            }
        }
        
        changed = changed | refreshValidationResult();
        
        return changed;
    }
    
    private boolean refreshValidationResult()
    {
        boolean changed = false;
        Status before = null;
        Status after = null;
        
        synchronized( this )
        {
            final Status newValidationStateLocal;
            final Status.CompositeStatusFactory newValidationStateFullFactory = Status.factoryForComposite();
            
            if( enabled() )
            {
                newValidationStateLocal = parent().service( this.property, ValidationAggregationService.class ).validation();
                
                newValidationStateFullFactory.merge( newValidationStateLocal );
                
                if( this.element != null )
                {
                    newValidationStateFullFactory.merge( this.element.validation() );
                }
            }
            else
            {
                newValidationStateLocal = Status.createOkStatus();
            }
            
            final Status newValidationStateFull = newValidationStateFullFactory.create();
            final Status oldValidationStateFull = this.validationStateFull;
    
            if( ! newValidationStateFull.equals( oldValidationStateFull ) )
            {
                this.validationStateLocal = newValidationStateLocal;
                this.validationStateFull = newValidationStateFull;
                
                if( oldValidationStateFull != null )
                {
                    changed = true;
                    before = oldValidationStateFull;
                    after = this.validationStateFull;
                }
            }
        }
        
        if( changed )
        {
            ( (ModelElement) parent() ).broadcastPropertyValidationEvent( this.property, before, after );
        }
        
        return changed;
    }
    
}
