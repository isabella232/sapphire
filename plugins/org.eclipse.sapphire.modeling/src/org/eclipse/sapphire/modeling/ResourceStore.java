/******************************************************************************
 * Copyright (c) 2012 Oracle
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

public abstract class ResourceStore
{
    private final Map<Locale,LocalizationService> localizationServices = new HashMap<Locale,LocalizationService>();
    
    @SuppressWarnings( "unchecked" )
    public <A> A adapt( final Class<A> adapterType )
    {
        A result = null;
        
        if( adapterType == LocalizationService.class )
        {
            result = (A) getLocalizationService();
        }
        
        return result;
    }

    /**
     * @throws ResourceStoreException  
     */
    
    public void save() 
    
        throws ResourceStoreException
        
    {
        // The default implementation doesn't do anything.
    }

    public void validateEdit() 
        
        throws ValidateEditException
        
    {
        validateSave();
    }
    
    public void validateSave()
    
        throws ValidateEditException
        
    {
        // The default implementation doesn't do anything.
    }
    
    public boolean isOutOfDate()
    {
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
        return SourceLanguageLocalizationService.INSTANCE;
    }
    
    public void dispose()
    {
    }
    
}
