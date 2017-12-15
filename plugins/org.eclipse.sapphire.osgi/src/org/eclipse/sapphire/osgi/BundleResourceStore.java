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

package org.eclipse.sapphire.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import org.eclipse.sapphire.modeling.ClassLocator;
import org.eclipse.sapphire.modeling.ResourceLocator;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.UrlResourceStore;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.localization.StandardLocalizationService;
import org.eclipse.sapphire.modeling.util.NLS;
import org.osgi.framework.Bundle;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class BundleResourceStore

    extends UrlResourceStore
    
{
    private final String bundleId;
    private final String path;
    private Bundle bundle;
    private ResourceLocator resourceLocator;
    private ClassLocator classLocator;
    
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
            final String msg = NLS.bind( Resources.couldNotFindBundleResource, bundleId, path );
            throw new IllegalArgumentException( msg );
        }
        
        return url;
    }
    
    @Override
    @SuppressWarnings( "unchecked" )
    
    public <A> A adapt( final Class<A> adapterType )
    {
        if( adapterType == ResourceLocator.class )
        {
            if( this.resourceLocator == null )
            {
                this.resourceLocator = new ResourceLocator()
                {
                    @Override
                    public URL find( final String name )
                    {
                        return bundle().getResource( name );
                    }
                };
            }
            
            return (A) this.resourceLocator;
        }
        else if( adapterType == ClassLocator.class )
        {
            if( this.classLocator == null )
            {
                this.classLocator = new ClassLocator()
                {
                    @Override
                    public Class<?> find( final String name )
                    {
                        try
                        {
                            return bundle().loadClass( name );
                        }
                        catch( ClassNotFoundException e )
                        {
                            // Intentionally converting ClassNotFoundException to null return.
                        }

                        return null;
                    }
                };
            }
            
            return (A) this.classLocator;
        }
        else if( adapterType == Bundle.class )
        {
            return (A) bundle();
        }

        return super.adapt( adapterType );
    }

    private static final class Resources extends NLS
    {
        public static String couldNotFindBundleResource;
        
        static
        {
            initializeMessages( BundleResourceStore.class.getName(), Resources.class );
        }
    }

}
