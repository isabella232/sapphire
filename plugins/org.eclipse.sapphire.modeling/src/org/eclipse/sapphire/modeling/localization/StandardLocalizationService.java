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

package org.eclipse.sapphire.modeling.localization;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.eclipse.sapphire.modeling.CapitalizationType;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class StandardLocalizationService

    extends LocalizationService
    
{
    private static final Locale NULL_LOCALE = new Locale( "" );
    
    private final Locale locale;
    private Map<String,String> sourceLangToTranslation;
    
    public StandardLocalizationService( final Locale locale )
    {
        this.locale = locale;
    }
    
    protected final synchronized void init()
    {
        if( this.sourceLangToTranslation == null )
        {
            final Map<String,String> keyToSource = load( NULL_LOCALE );
            final Map<String,String> keyToTranslation = load( this.locale );
            
            this.sourceLangToTranslation = new HashMap<String,String>();
            
            for( Map.Entry<String,String> entry : keyToSource.entrySet() )
            {
                final String key = entry.getKey();
                final String source = entry.getValue();
                final String translation = keyToTranslation.get( key );
                
                if( translation != null && ! source.equals( translation ) )
                {
                    this.sourceLangToTranslation.put( source, translation );
                }
            }
        }
    }
    
    protected final Map<String,String> load( final Locale locale )
    {
        Locale l = locale;
        
        final Map<String,String> keyToTranslation = new HashMap<String,String>();
        
        if( load( l, keyToTranslation ) )
        {
            return keyToTranslation;
        }
        
        final String variant = l.getVariant();
        
        if( variant != null && variant.length() > 0 )
        {
            l = new Locale( l.getLanguage(), l.getCountry() );
            
            if( load( l, keyToTranslation ) )
            {
                return keyToTranslation;
            }
        }
        
        final String country = l.getCountry();
        
        if( country != null && country.length() > 0 )
        {
            l = new Locale( l.getLanguage() );
            
            if( load( l, keyToTranslation ) )
            {
                return keyToTranslation;
            }
        }
        
        final String language = l.getLanguage();
        
        if( language != null && language.length() > 0 )
        {
            load( NULL_LOCALE, keyToTranslation );
        }
        
        return keyToTranslation;
    }
    
    protected abstract boolean load( Locale locale, Map<String,String> keyToText );
    
    protected static final boolean parse( final InputStream in,
                                          final Map<String,String> keyToText )
    {
        final Properties props = new Properties();
        
        try
        {
            try
            {
                props.load( in );
            }
            finally
            {
                try
                {
                    in.close();
                }
                catch( IOException e ) {}
            }
        }
        catch( IOException e )
        {
            return false;
        }
        
        for( Map.Entry<Object,Object> entry : props.entrySet() )
        {
            keyToText.put( (String) entry.getKey(), (String) entry.getValue() );
        }
        
        return true;
    }
    
    @Override
    public String text( final String sourceLangString,
                        final CapitalizationType capitalizationType,
                        final boolean includeMnemonic )
    {
        init();
        
        String result = this.sourceLangToTranslation.get( sourceLangString );
        
        if( result == null )
        {
            result = sourceLangString;
        }
        
        result = transform( result, capitalizationType, includeMnemonic );
        
        return result;
    }
    
}
