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

package org.eclipse.sapphire.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class BundleLocator
{
    private BundleLocator()
    {
        // This class is not meant to be instantiated.
    }
    
    public static Bundle find( final String bundleSymbolicName )
    {
        final BundleContext context = FrameworkUtil.getBundle( BundleLocator.class ).getBundleContext();
        
        Bundle bundle = null;
        
        for( Bundle candidate : context.getBundles() )
        {
            final int state = candidate.getState();
            
            if( state != Bundle.UNINSTALLED && candidate.getSymbolicName().equals( bundleSymbolicName ) )
            {
                if( bundle == null || bundle.getVersion().compareTo( candidate.getVersion() ) < 0 )
                {
                    bundle = candidate;
                }
            }
        }
        
        return bundle;
    }
    
}
