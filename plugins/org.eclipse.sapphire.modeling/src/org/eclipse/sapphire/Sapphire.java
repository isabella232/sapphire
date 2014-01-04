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

package org.eclipse.sapphire;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class Sapphire
{
    private static final String VERSION_QUALIFIER_SUFFIX = ".qualifier";
    
    private static boolean devmode = Boolean.parseBoolean( System.getProperty( "sapphire.dev.mode" ) );
    
    private static Version version;
    private static ServiceContext services;
    
    /**
     * This class is not meant to be instantiated.
     */
    
    private Sapphire()
    {
    }
    
    public static boolean isDevMode()
    {
        return devmode;
    }
    
    /**
     * Determines the version of Sapphire.
     * 
     * @return the version of Sapphire
     */
    
    public static synchronized Version version()
    {
        if( version == null )
        {
            Enumeration<URL> itr = null;
            
            try
            {
                itr = Sapphire.class.getClassLoader().getResources( "META-INF/MANIFEST.MF" );
            }
            catch( Exception e )
            {
                Sapphire.service( LoggingService.class ).log( e );
                version = new Version( 0 );
            }
            
            if( itr != null )
            {
                while( itr.hasMoreElements() && version == null )
                {
                    InputStream stream = null;
                    Manifest manifest = null;
                    
                    try
                    {
                        stream = itr.nextElement().openStream();
                        manifest = new Manifest( stream );
                    }
                    catch( IOException e )
                    {
                        // Do not actually want to log this as we could be reading some other JAR's corrupted manifest.
                    }
                    finally
                    {
                        if( stream != null )
                        {
                            try
                            {
                                stream.close();
                            }
                            catch( IOException e ) {}
                        }
                    }
                    
                    if( manifest != null )
                    {
                        final Attributes attributes = manifest.getMainAttributes();
                        final String bundleSymbolicName = attributes.getValue( "Bundle-SymbolicName" );
                        
                        if( bundleSymbolicName != null && bundleSymbolicName.equals( "org.eclipse.sapphire.modeling" ) )
                        {
                            String bundleVersion = attributes.getValue( "Bundle-Version" );
                            
                            if( bundleVersion != null )
                            {
                                bundleVersion = bundleVersion.trim();
                                
                                if( bundleVersion.endsWith( VERSION_QUALIFIER_SUFFIX ) )
                                {
                                    bundleVersion = bundleVersion.substring( 0, bundleVersion.length() - VERSION_QUALIFIER_SUFFIX.length() );
                                }
                                
                                try
                                {
                                    version = new Version( bundleVersion );
                                }
                                catch( IllegalArgumentException e )
                                {
                                    Sapphire.service( LoggingService.class ).log( e );
                                }
                            }
                            
                            if( version == null )
                            {
                                version = new Version( 0 );
                            }
                        }
                    }
                }
            }
        }
        
        return version;
    }
    
    /**
     * Returns the service of the specified type from the root service context.
     * 
     * @param <S> the type of the service
     * @param type the type of the service
     * @return the service or <code>null</code> if not available
     */
    
    public static <S extends Service> S service( final Class<S> type )
    {
        return services().service( type );
    }

    /**
     * Returns services of the specified type from the root service context.
     * 
     * @param <S> the type of the service
     * @param type the type of the service
     * @return the list of services or an empty list if none are available
     */
    
    public static <S extends Service> List<S> services( final Class<S> type )
    {
        return services().services( type );
    }
    
    /**
     * Returns the root service context.
     * 
     * @return the root service context
     */
    
    public static synchronized ServiceContext services()
    {
        if( services == null )
        {
            services = new ServiceContext( ServiceContext.ID_ROOT );
        }
        
        return services;
    }
    
}
