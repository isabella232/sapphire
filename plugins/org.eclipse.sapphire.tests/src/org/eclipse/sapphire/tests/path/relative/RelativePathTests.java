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

package org.eclipse.sapphire.tests.path.relative;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests relative path.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class RelativePathTests extends SapphireTestCase
{
    @Test
    
    public void MustExistValidation_WhenRootChanges() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            final IProject project = project();
            
            final IFolder aFolder = project.getFolder( "a" );
            final String aFolderPath = aFolder.getLocation().toString();
            aFolder.create( true, true, null );

            final IFolder bFolder = project.getFolder( "b" );
            final String bFolderPath = bFolder.getLocation().toString();
            final IFile bFile = bFolder.getFile( "abc.bin" );
            bFolder.create( true, true, null );
            bFile.create( new ByteArrayInputStream( new byte[] { 0, 1 } ), true, null );
            
            element.setRootPath( aFolderPath );
            element.setRelativePath( "abc.bin" );
            
            assertValidationError( element.getRelativePath(), "File or folder \"abc.bin\" does not exist" );
            
            element.setRootPath( bFolderPath );
            
            assertValidationOk( element.getRelativePath() );
        }
        finally
        {
            element.dispose();
        }
    }

}
