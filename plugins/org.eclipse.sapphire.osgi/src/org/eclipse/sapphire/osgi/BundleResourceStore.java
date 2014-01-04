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

package org.eclipse.sapphire.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import org.eclipse.sapphire.Context;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.UrlResourceStore;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.localization.StandardLocalizationService;
import org.osgi.framework.Bundle;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class BundleResourceStore extends UrlResourceStore
{
    @Text( "Could not find resource \"{1}\" in bundle \"{0}\"." )
    private static LocalizableText couldNotFindBundleResource;
    
    static
    {
        LocalizableText.init( BundleResourceStore.class );
    }

    private final String bundleId;
    private final String path;
    private Bundle bundle;
    private Context context;
    
    public BundleResourceStore( final String bundleId,
                                final String path )
    
        throws ResourceStoreException
        
    {
        super( toUrl( bundleId, path, true ) );
        
        this.bundleId = bundleId;
        this.path = path;
    }
    
    public final Bundle bundle()
    {
        if( this.bundle == null )
        {
            this.bundle = BundleLocator.find( this.bundleId );
        }
        
        return this.bundle;
    }
    
    @Override
    public boolean isOutOfDate()
    {
        return false;
    }
    
    @Override
    protected LocalizationService initLocalizationService( final Locale locale )
    {
        return new StandardLocalizationService( locale )
        {
            @Override
            protected boolean load( final Locale locale,
                                    final Map<String,String> keyToText )
            {
                final String bundleId = BundleResourceStore.this.bundleId;
                final String path = BundleResourceStore.this.path;
                final int lastDot = path.lastIndexOf( '.' );
                
                if( lastDot != -1 )
                {
                    String resFilePath = path.substring( 0, lastDot );
                    final String localeString = locale.toString();
                    
                    if( localeString.length() > 0 )
                    {
                        resFilePath = resFilePath + "_" + localeString;
                    }
                    
                    resFilePath = resFilePath + ".properties";
                    
                    final URL resFileUrl = toUrl( bundleId, resFilePath, false );
                    
                    if( resFileUrl != null )
                    {
                        try
                        {
                            final InputStream stream = resFileUrl.openStream();
                            
                            try
                            {
                                return parse( stream, keyToText );
                            }
                            finally
                            {
                                try
                                {
                                    stream.close();
                                }
                                catch( IOException e ) {}
                            }
                        }
                        catch( IOException e )
                        {
                            return false;
                        }
                    }
                }
                
                return false;
            }
        };
    }

    private static final URL toUrl( final String bundleId,
                                    final String path,
                                    final boolean throwExceptionOnNotFound )
    {
        final Bundle bundle = BundleLocator.find( bundleId );
        final URL url = bundle.getResource( path );
        
        if( url == null && throwExceptionOnNotFound )
        {
            final String msg = couldNotFindBundleResource.format( bundleId, path );
            throw new IllegalArgumentException( msg );
        }
        
        return url;
    }
    
    @Override
    public <A> A adapt( final Class<A> adapterType )
    {
        if( adapterType == Context.class )
        {
            if( this.context == null )
            {
                this.context = BundleBasedContext.adapt( bundle() );
            }
            
            return adapterType.cast( this.context );
        }
        else if( adapterType == Bundle.class )
        {
            return adapterType.cast( bundle() );
        }

        return super.adapt( adapterType );
    }

}
