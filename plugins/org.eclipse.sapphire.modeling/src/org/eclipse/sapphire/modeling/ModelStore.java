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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ModelStore
{
    private static final Locale ROOT_LOCALE = new Locale( "" );

    private Map<String,String> defaultResourcesReverseLookup = null;
    private Map<Locale,Map<String,String>> localizedResources = null;
    
    public abstract void open() throws IOException;
    public abstract void save() throws IOException;
    
    public boolean validateEdit()
    {
        return true;
    }
    
    public boolean isOutOfDate()
    {
        return true;
    }
    
    public String getLocalizedText( final String text,
                                    final Locale locale )
    {
        if( this.defaultResourcesReverseLookup == null )
        {
            final Map<String,String> defaultResources = loadLocalizedResources( ROOT_LOCALE );
            
            if( defaultResources == null )
            {
                return text;
            }
            
            this.defaultResourcesReverseLookup = new HashMap<String,String>();
            
            for( Map.Entry<String,String> entry : defaultResources.entrySet() )
            {
                this.defaultResourcesReverseLookup.put( entry.getValue(), entry.getKey() );
            }
            
            this.localizedResources = new HashMap<Locale,Map<String,String>>();
        }
        
        final String key = this.defaultResourcesReverseLookup.get( text );
        
        if( key != null )
        {
            final Map<String,String> resourcesForLocale = getLocalizedResources( locale );
            final String localizedText = resourcesForLocale.get( key );
            
            if( localizedText != null )
            {
                return localizedText;
            }
        }
        
        return text;
    }
    
    protected Map<String,String> loadLocalizedResources( final Locale locale )
    {
        return null;
    }
    
    private Map<String,String> getLocalizedResources( final Locale locale )
    {
        Map<String,String> localizedResources = this.localizedResources.get( locale );
        
        if( localizedResources == null )
        {
            localizedResources = loadLocalizedResources( locale );
            
            if( localizedResources == null )
            {
                if( locale.getVariant().length() > 0 )
                {
                    localizedResources = getLocalizedResources( new Locale( locale.getLanguage(), locale.getCountry() ) );
                }
                else if( locale.getCountry().length() > 0 )
                {
                    localizedResources = getLocalizedResources( new Locale( locale.getLanguage() ) );
                }
                else
                {
                    localizedResources = Collections.emptyMap();
                }
            }
            
            this.localizedResources.put( locale, localizedResources );
        }
        
        return localizedResources;
    }
    
}
