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

package org.eclipse.sapphire;

import java.io.File;
import java.io.IOException;

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

    public static void mkdirs( final File f ) throws IOException
    {
        if( f.exists() )
        {
            if( f.isFile() )
            {
                throw new IOException( locationIsFile.format( f.getAbsolutePath() ) );
            }
        }
        else
        {
            mkdirs( f.getParentFile() );
            
            final boolean isSuccessful = f.mkdir();
            
            if( ! isSuccessful )
            {
                throw new IOException( failedToCreateDirectory.format( f.getAbsolutePath() ) );
            }
        }
    }

}
