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
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;
import org.osgi.framework.Bundle;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class BundleResourceModelStore

    extends UrlModelStore
    
{
    private final String bundleId;
    private final String path;
    
    public BundleResourceModelStore( final String bundleId,
                                     final String path )
    {
        super( toUrl( bundleId, path, true ) );
        
        this.bundleId = bundleId;
        this.path = path;
    }
    
    @Override
    public boolean isOutOfDate()
    {
        return false;
    }
    
    @Override
    protected Map<String,String> loadLocalizedResources( final Locale locale )
    {
        final int lastDot = this.path.lastIndexOf( '.' );
        
        if( lastDot != -1 )
        {
            String resFilePath = this.path.substring( 0, lastDot );
            final String localeString = locale.toString();
            
            if( localeString.length() > 0 )
            {
                resFilePath = resFilePath + "_" + localeString;
            }
            
            resFilePath = resFilePath + ".properties";
            
            final URL resFileUrl = toUrl( this.bundleId, resFilePath, false );
            
            if( resFileUrl != null )
            {
                final Properties props = new Properties();
                
                try
                {
                    final InputStream stream = resFileUrl.openStream();
                    
                    try
                    {
                        props.load( stream );
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
                    SapphireModelingFrameworkPlugin.log( e );
                }
                
                final Map<String,String> resources = new HashMap<String,String>();
                
                for( Map.Entry<Object,Object> entry : props.entrySet() )
                {
                    resources.put( (String) entry.getKey(), (String) entry.getValue() );
                }
                
                return resources;
            }
        }
        
        return null;
    }

    private static final URL toUrl( final String bundleId,
                                    final String path,
                                    final boolean throwExceptionOnNotFound )
    {
        final Bundle bundle = Platform.getBundle( bundleId );
        final URL url = FileLocator.find( bundle, new Path( path ), null );
        
        if( url == null && throwExceptionOnNotFound )
        {
            final String msg = NLS.bind( Resources.couldNotFindBundleResource, bundleId, path );
            throw new IllegalArgumentException( msg );
        }
        
        return url;
    }
    
    private static final class Resources
    
        extends NLS
    
    {
        public static String couldNotFindBundleResource;
        
        static
        {
            initializeMessages( BundleResourceModelStore.class.getName(), Resources.class );
        }
    }
    

}
