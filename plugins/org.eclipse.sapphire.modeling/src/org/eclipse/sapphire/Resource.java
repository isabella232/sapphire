/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.sapphire.modeling.CorruptedResourceExceptionInterceptor;
import org.eclipse.sapphire.modeling.ElementPropertyBinding;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.localization.SourceLanguageLocalizationService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class Resource implements Disposable
{
    private final Resource parent;
    private Element element;
    private final Map<Property,PropertyBinding> bindings = new HashMap<Property,PropertyBinding>();
    private CorruptedResourceExceptionInterceptor corruptedResourceExceptionInterceptor;
    private final Map<Locale,LocalizationService> localizationServices = new HashMap<Locale,LocalizationService>();
    
    public Resource( final Resource parent )
    {
        this.parent = parent;
    }
    
    public void init( final Element element )
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
    
    public final Element element()
    {
        return this.element;
    }
    
    public final PropertyBinding binding( final PropertyDef property )
    {
        return binding( this.element.property( property ) );
    }
    
    public final ValuePropertyBinding binding( final ValueProperty property )
    {
        return (ValuePropertyBinding) binding( (PropertyDef) property );
    }
    
    public final ElementPropertyBinding binding( final ElementProperty property )
    {
        return (ElementPropertyBinding) binding( (PropertyDef) property );
    }
    
    public final ListPropertyBinding binding( final ListProperty property )
    {
        return (ListPropertyBinding) binding( (PropertyDef) property );
    }
    
    public final PropertyBinding binding( final Property property )
    {
        PropertyBinding binding = this.bindings.get( property );
        
        if( binding == null )
        {
            binding = createBinding( property );
            
            if( binding == null )
            {
                throw new IllegalArgumentException();
            }
            
            binding.init( property );
            
            this.bindings.put( property, binding );
        }
        
        return binding;
    }
    
    protected abstract PropertyBinding createBinding( final Property property );
    
    /**
     * @throws ResourceStoreException  
     */
    
    public void save() throws ResourceStoreException
    {
        final Resource root = root();
        
        if( this != root )
        {
            root.save();
        }
    }
    
    public <A> A adapt( final Class<A> adapterType )
    {
        A result = Sapphire.service( MasterConversionService.class ).convert( this, adapterType );
        
        if( result == null )
        {
            if( adapterType.isInstance( this ) )
            {
                result = adapterType.cast( this );
            }
            else if( adapterType == LocalizationService.class )
            {
                result = adapterType.cast( getLocalizationService() );
            }
            else if( this.parent != null )
            {
                result = this.parent.adapt( adapterType );
            }
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
    
    @Override
    public void dispose()
    {
        for( PropertyBinding binding : this.bindings.values() )
        {
            try
            {
                binding.dispose();
            }
            catch( Exception e )
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
        }
    }

}
