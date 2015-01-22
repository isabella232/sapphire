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

package org.eclipse.sapphire.tests.services.t0002;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.sapphire.services.FileExtensionsService;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests FileExtensionsService and @FileExtensions annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestServices0002 extends SapphireTestCase
{
    @Test
    
    public void testNoFileExtensionsService() throws Exception
    {
        final TestModel model = TestModel.TYPE.instantiate();
        final FileExtensionsService service = model.property( TestModel.PROP_FILE_PATH_1 ).service( FileExtensionsService.class );
        
        assertNull( service );
    }

    @Test
    
    public void testSingleFileExtension() throws Exception
    {
        final TestModel model = TestModel.TYPE.instantiate();
        final FileExtensionsService service = model.property( TestModel.PROP_FILE_PATH_2 ).service( FileExtensionsService.class );
        
        assertNotNull( service );
        assertEquals( list( "png" ), service.extensions() );
    }

    @Test
    
    public void testMultipleFileExtensions() throws Exception
    {
        final TestModel model = TestModel.TYPE.instantiate();
        final FileExtensionsService service = model.property( TestModel.PROP_FILE_PATH_3 ).service( FileExtensionsService.class );
        
        assertNotNull( service );
        assertEquals( list( "png", "gif", "jpeg" ), service.extensions() );
    }
    
    @Test
    
    public void testFileExtensionsExpr1() throws Exception
    {
        final TestModel model = TestModel.TYPE.instantiate();
        final FileExtensionsService service = model.property( TestModel.PROP_FILE_PATH_4 ).service( FileExtensionsService.class );
        
        assertNotNull( service );
        
        model.setLossyCompression( true );
        assertEquals( list( "jpeg" ), service.extensions() );

        model.setLossyCompression( false );
        assertEquals( list( "png", "gif" ), service.extensions() );
    }
    
    @Test
    
    public void testFileExtensionsExpr2() throws Exception
    {
        final TestModelRoot root = TestModelRoot.TYPE.instantiate();
        final TestModel model = root.getList().insert();
        final FileExtensionsService service = model.property( TestModel.PROP_FILE_PATH_5 ).service( FileExtensionsService.class );
        
        assertNotNull( service );
        
        root.setLossyCompression( true );
        assertEquals( list( "jpeg" ), service.extensions() );

        root.setLossyCompression( false );
        assertEquals( list( "png", "gif" ), service.extensions() );
    }
    
    @Test
    
    public void testFileExtensionsExpr3() throws Exception
    {
        final TestModelRoot root = TestModelRoot.TYPE.instantiate();
        final TestModel model = root.getElement().content( true );
        final FileExtensionsService service = model.property( TestModel.PROP_FILE_PATH_5 ).service( FileExtensionsService.class );
        
        assertNotNull( service );
        
        root.setLossyCompression( true );
        assertEquals( list( "jpeg" ), service.extensions() );

        root.setLossyCompression( false );
        assertEquals( list( "png", "gif" ), service.extensions() );
    }

    @Test
    
    public void testFileExtensionsExpr4() throws Exception
    {
        final TestModelRoot root = TestModelRoot.TYPE.instantiate();
        final TestModel model = root.getElementImplied();
        final FileExtensionsService service = model.property( TestModel.PROP_FILE_PATH_5 ).service( FileExtensionsService.class );
        
        assertNotNull( service );
        
        root.setLossyCompression( true );
        assertEquals( list( "jpeg" ), service.extensions() );

        root.setLossyCompression( false );
        assertEquals( list( "png", "gif" ), service.extensions() );
    }
    
    @Test

    public void testCustomFileExtensionsService() throws Exception
    {
        final TestModel model = TestModel.TYPE.instantiate();
        final FileExtensionsService service = model.property( TestModel.PROP_FILE_PATH_6 ).service( FileExtensionsService.class );
        
        assertNotNull( service );
        assertEquals( list( "avi", "mpeg" ), service.extensions() );
    }
    
    @Test
    
    public void testValidation() throws Exception
    {
        final TestModel model = TestModel.TYPE.instantiate();
        final FileExtensionsService service = model.property( TestModel.PROP_FILE_PATH_4 ).service( FileExtensionsService.class );
        
        final IProject project = project();
        
        final IFile txtFile = project.getFile( "file.txt" );
        final String txtFilePath = txtFile.getLocation().toOSString();
        txtFile.create( new ByteArrayInputStream( new byte[ 0 ] ), true, new NullProgressMonitor() );
        
        final IFile jpegFile = project.getFile( "file.jpeg" );
        final String jpegFilePath = jpegFile.getLocation().toOSString();
        jpegFile.create( new ByteArrayInputStream( new byte[ 0 ] ), true, new NullProgressMonitor() );
        
        final IFile pngFile = project.getFile( "file.png" );
        final String pngFilePath = pngFile.getLocation().toOSString();
        pngFile.create( new ByteArrayInputStream( new byte[ 0 ] ), true, new NullProgressMonitor() );
        
        assertNotNull( service );
        
        model.setLossyCompression( true );
        assertEquals( list( "jpeg" ), service.extensions() );
        
        model.setFilePath4( txtFilePath );
        assertValidationError( model.getFilePath4(), "File \"file.txt\" has an invalid extension. Only \"jpeg\" extension is allowed" );
        
        model.setFilePath4( jpegFilePath );
        assertValidationOk( model.getFilePath4() );

        model.setLossyCompression( false );
        assertEquals( list( "png", "gif" ), service.extensions() );

        assertValidationError( model.getFilePath4(), "File \"file.jpeg\" has an invalid extension. Only extensions \"png\" and \"gif\" are allowed" );

        model.setFilePath4( pngFilePath );
        assertValidationOk( model.getFilePath4() );
    }
    
}
