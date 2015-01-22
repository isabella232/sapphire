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

package org.eclipse.sapphire.modeling;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.sapphire.Context;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class ExtensionsLocator
{
    protected static final String DEFAULT_PATH = "META-INF/sapphire-extension.xml";
    
    private static ExtensionsLocator instance;
    
    public static synchronized final ExtensionsLocator instance()
    {
        if( instance == null )
        {
            final ClassLoader cl = ExtensionsLocator.class.getClassLoader();
            
            Class<?> implClass = null;
            
            try
            {
                implClass = cl.loadClass( "org.eclipse.sapphire.modeling.ExtensionsLocatorFactory" );
            }
            catch( ClassNotFoundException e )
            {
                // Intentionally ignoring. The discrete locator implementation is optional.
            }
            
            if( implClass != null )
            {
                try
                {
                    final Factory factory = (Factory) implClass.newInstance();
                    
                    if( factory.applicable() )
                    {
                        instance = factory.create();
                    }
                }
                catch( Exception e )
                {
                    // Problem here means a bug in the discrete locator implementation. Log the problem and fail over
                    // to the default locator logic.
                    
                    Sapphire.service( LoggingService.class ).log( e );
                }
            }
            
            if( instance == null )
            {
                instance = new ExtensionsLocator()
                {
                    @Override
                    public List<Handle> find()
                    {
                        final List<Handle> handles = new ArrayList<Handle>();
                        
                        try
                        {
                            final Enumeration<URL> urls = cl.getResources( DEFAULT_PATH );

                            while( urls.hasMoreElements() )
                            {
                                final URL url = urls.nextElement();

                                if( url != null )
                                {
                                    handles.add( new Handle( url, Context.adapt( cl ) ) );
                                }
                            }
                        }
                        catch( IOException e )
                        {
                            Sapphire.service( LoggingService.class ).log( e );
                        }
                        
                        return Collections.unmodifiableList( handles );
                    }
                };
            }
        }

        return instance;
    }
    
    public abstract List<Handle> find();

    public static final class Handle
    {
        private final URL extension;
        private final Context context;
        
        public Handle( final URL extension,
                       final Context context )
        {
            this.extension = extension;
            this.context = context;
        }
        
        public URL extension()
        {
            return this.extension;
        }
        
        public Context context()
        {
            return this.context;
        }
    }
    
    public static abstract class Factory
    {
        public abstract boolean applicable();
        public abstract ExtensionsLocator create();
    }

}
