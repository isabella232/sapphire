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

import java.net.URL;
import java.util.List;

import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.util.ListFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * Locates Sapphire extensions in an OSGi system by scanning all bundles.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ExtensionsLocatorFactory extends ExtensionsLocator.Factory
{
    @Override
    public boolean applicable()
    {
        return ( FrameworkUtil.getBundle( ExtensionsLocatorFactory.class ) != null );
    }

    @Override
    public ExtensionsLocator create()
    {
        return new ExtensionsLocator()
        {
            private List<Handle> handles;
            
            @Override
            public synchronized List<Handle> find()
            {
                if( this.handles == null )
                {
                    final BundleContext context = FrameworkUtil.getBundle( ExtensionsLocatorFactory.class ).getBundleContext();
                    final ListFactory<Handle> handlesListFactory = ListFactory.start();
                    
                    for( final Bundle bundle : context.getBundles() )
                    {
                        final int state = bundle.getState();
                        
                        if( state == Bundle.RESOLVED || state == Bundle.STARTING || state == Bundle.ACTIVE )
                        {
                            final URL url = bundle.getEntry( DEFAULT_PATH );
                            
                            if( url != null )
                            {
                                boolean ok = false;
                                
                                try
                                {
                                    // Verify that the bundle is using this version of Sapphire before trying to
                                    // load its extensions.
                                    
                                    ok = ( bundle.loadClass( Sapphire.class.getName() ) == Sapphire.class );
                                }
                                catch( Exception e ) {}

                                if( ok )
                                {
                                    handlesListFactory.add( new Handle( url, BundleBasedContext.adapt( bundle ) ) );
                                }
                            }
                        }
                    }
                    
                    this.handles = handlesListFactory.result();
                }
                
                return this.handles;
            }
        };
    }

}
