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

package org.eclipse.sapphire.modeling.util.internal;

import java.io.File;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.StatusException;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class FileUtil
{
    @Text( "Failed to create directory \"{0}\"." )
    private static LocalizableText failedToCreateDirectory;
    
    @Text( "Location \"{0}\" is a file." )
    private static LocalizableText locationIsFile;
    
    static
    {
        LocalizableText.init( FileUtil.class );
    }

    public static void mkdirs( final File f ) throws StatusException
    {
        if( f.exists() )
        {
            if( f.isFile() )
            {
                final String msg = locationIsFile.format( f.getAbsolutePath() );
                throw new StatusException( Status.createErrorStatus( msg ) );
            }
        }
        else
        {
            mkdirs( f.getParentFile() );
            
            final boolean isSuccessful = f.mkdir();
            
            if( ! isSuccessful )
            {
                final String msg = failedToCreateDirectory.format( f.getAbsolutePath() );
                throw new StatusException( Status.createErrorStatus( msg ) );
            }
        }
    }

}
