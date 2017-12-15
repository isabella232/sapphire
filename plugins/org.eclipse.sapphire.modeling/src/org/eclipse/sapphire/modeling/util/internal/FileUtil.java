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

package org.eclipse.sapphire.modeling.util.internal;

import java.io.File;

import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.StatusException;
import org.eclipse.sapphire.modeling.util.NLS;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class FileUtil
{
    public static void mkdirs( final File f ) throws StatusException
    {
        if( f.exists() )
        {
            if( f.isFile() )
            {
                final String msg = NLS.bind( Resources.locationIsFile, f.getAbsolutePath() );
                throw new StatusException( Status.createErrorStatus( msg ) );
            }
        }
        else
        {
            mkdirs( f.getParentFile() );
            
            final boolean isSuccessful = f.mkdir();
            
            if( ! isSuccessful )
            {
                final String msg = NLS.bind( Resources.failedToCreateDirectory, f.getAbsolutePath() );
                throw new StatusException( Status.createErrorStatus( msg ) );
            }
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String failedToCreateDirectory;
        public static String locationIsFile;
        
        static
        {
            initializeMessages( FileUtil.class.getName(), Resources.class );
        }
    }

}
