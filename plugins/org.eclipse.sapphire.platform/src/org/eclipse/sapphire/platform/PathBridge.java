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

package org.eclipse.sapphire.platform;

import org.eclipse.core.runtime.IPath;
import org.eclipse.sapphire.modeling.Path;

/**
 * Bridges between Sapphire and Eclipse path API.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PathBridge
{
    private PathBridge()
    {
        // This class is not meant to be instantiated.
    }
    
    public static IPath create( final Path path )
    {
        return new org.eclipse.core.runtime.Path( path.toString() );
    }
    
    public static Path create( final IPath path )
    {
        return new Path( path.toString() );
    }
    
}
