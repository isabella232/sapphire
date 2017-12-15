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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.localization.SourceLanguageLocalizationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class Resource
{
    private final Resource parent;
    private IModelElement element;
    private final Map<ModelProperty,BindingImpl> bindings = new HashMap<ModelProperty,BindingImpl>();
    private CorruptedResourceExceptionInterceptor corruptedResourceExceptionInterceptor;
    private final Map<Locale,LocalizationService> localizationServices = new HashMap<Locale,LocalizationService>();
    
    public Resource( final Resource parent )
    {
        this.parent = parent;
    }
    
    public void init( final IModelElement element )
    {
        if( this.element != null )
        {
            throw new IllegalStateException();
        }
        
        this.element = element;
    }

    public Resource parent()
    {
        return this.parent;
    }
    
    public Resource root()
    {
        if( this.parent == null )
        {
            return this;
        }
        else
        {
            return this.parent.root();
        }
    }
    
    public final IModelElement element()
    {
        return this.element;
    }
    
    public final ValueBindingImpl binding( final ValueProperty property )
    {
        return (ValueBindingImpl) binding( (ModelProperty) property );
    }
    
    public final ElementBindingImpl binding( final ElementProperty property )
    {
        return (ElementBindingImpl) binding( (ModelProperty) property );
    }
    
    public final ListBindingImpl binding( final ListProperty property )
    {
        return (ListBindingImpl) binding( (ModelProperty) property );
    }
    
    public final BindingImpl binding( final ModelProperty property )
    {
        BindingImpl binding = this.bindings.get( property );
        
        if( binding == null )
        {
            binding = createBinding( property );
            
            if( binding == null )
            {
                throw new IllegalArgumentException();
            }
            
            this.bindings.put( property, binding );
        }
        
        return binding;
    }
    
    protected abstract BindingImpl createBinding( final ModelProperty property );
    
    /**
     * @throws ResourceStoreException  
     */
    
    public void save() 
    
        throws ResourceStoreException
        
    {
        final Resource root = root();
        
        if( this != root )
        {
            root.save();
        }
    }
    
    @SuppressWarnings( "unchecked" )
    public <A> A adapt( final Class<A> adapterType )
    {
        A result = null;
        
        if( adapterType.isInstance( this ) )
        {
            result = (A) this;
        }
        else if( adapterType == LocalizationService.class )
        {
            result = (A) getLocalizationService();
        }
        else if( this.parent != null )
        {
            result = this.parent.adapt( adapterType );
        }
        
        return result;
    }
    
    public boolean isOutOfDate()
    {
        final Resource root = root();
        
        if( this != root )
        {
            return root.isOutOfDate();
        }
        
        return false;
    }
    
    public final LocalizationService getLocalizationService()
    {
        return getLocalizationService( Locale.getDefault() );
    }

    public final LocalizationService getLocalizationService( final Locale locale )
    {
        synchronized( this.localizationServices )
        {
            LocalizationService service = this.localizationServices.get( locale );
            
            if( service == null )
            {
                service = initLocalizationService( locale );
                
                if( service != null )
                {
                    this.localizationServices.put( locale, service );
                }
            }
        
            return service;
        }
    }
    
    protected LocalizationService initLocalizationService( final Locale locale )
    {
        final Resource root = root();
        
        if( this != root )
        {
            return root.getLocalizationService( locale );
        }
        
        return SourceLanguageLocalizationService.INSTANCE;
    }

    public final void setCorruptedResourceExceptionInterceptor( final CorruptedResourceExceptionInterceptor interceptor )
    {
        this.corruptedResourceExceptionInterceptor = interceptor;
    }
    
    protected final boolean validateCorruptedResourceRecovery()
    {
        if( this.corruptedResourceExceptionInterceptor != null )
        {
            return this.corruptedResourceExceptionInterceptor.shouldAttemptRepair();
        }
        
        return false;
    }

}
