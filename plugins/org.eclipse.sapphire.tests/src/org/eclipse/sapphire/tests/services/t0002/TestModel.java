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

package org.eclipse.sapphire.tests.services.t0002;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.annotations.AbsolutePath;
import org.eclipse.sapphire.modeling.annotations.FileExtensions;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestModel extends Element
{
    ElementType TYPE = new ElementType( TestModel.class );
    
    // *** FilePath1 ***
    
    @Type( base = Path.class )
    @ValidFileSystemResourceType( FileSystemResourceType.FILE )
    @AbsolutePath
    
    ValueProperty PROP_FILE_PATH_1 = new ValueProperty( TYPE, "FilePath1" );
    
    Value<Path> getFilePath1();
    void setFilePath1( String value );
    void setFilePath1( Path value );
    
    // *** FilePath2 ***
    
    @Type( base = Path.class )
    @ValidFileSystemResourceType( FileSystemResourceType.FILE )
    @FileExtensions( expr = "png" )
    @AbsolutePath
    
    ValueProperty PROP_FILE_PATH_2 = new ValueProperty( TYPE, "FilePath2" );
    
    Value<Path> getFilePath2();
    void setFilePath2( String value );
    void setFilePath2( Path value );
    
    // *** FilePath3 ***
    
    @Type( base = Path.class )
    @ValidFileSystemResourceType( FileSystemResourceType.FILE )
    @FileExtensions( expr = "png,gif,jpeg" )
    @AbsolutePath
    
    ValueProperty PROP_FILE_PATH_3 = new ValueProperty( TYPE, "FilePath3" );
    
    Value<Path> getFilePath3();
    void setFilePath3( String value );
    void setFilePath3( Path value );
    
    // *** FilePath4 ***
    
    @Type( base = Path.class )
    @ValidFileSystemResourceType( FileSystemResourceType.FILE )
    @FileExtensions( expr = "${ LossyCompression ? 'jpeg' : 'png,gif' }" )
    @AbsolutePath
    
    ValueProperty PROP_FILE_PATH_4 = new ValueProperty( TYPE, "FilePath4" );
    
    Value<Path> getFilePath4();
    void setFilePath4( String value );
    void setFilePath4( Path value );
    
    // *** FilePath5 ***
    
    @Type( base = Path.class )
    @ValidFileSystemResourceType( FileSystemResourceType.FILE )
    @FileExtensions( expr = "${ empty Parent() ? 'jpeg' : ( Parent().LossyCompression ? 'jpeg' : 'png,gif' ) }" )
    @AbsolutePath
    
    ValueProperty PROP_FILE_PATH_5 = new ValueProperty( TYPE, "FilePath5" );
    
    Value<Path> getFilePath5();
    void setFilePath5( String value );
    void setFilePath5( Path value );
    
    // *** FilePath6 ***
    
    @Type( base = Path.class )
    @ValidFileSystemResourceType( FileSystemResourceType.FILE )
    @Service( impl = CustomFileExtensionsService.class )
    @AbsolutePath
    
    ValueProperty PROP_FILE_PATH_6 = new ValueProperty( TYPE, "FilePath6" );
    
    Value<Path> getFilePath6();
    void setFilePath6( String value );
    void setFilePath6( Path value );
    
    // *** LossyCompression ***
    
    @Type( base = Boolean.class )

    ValueProperty PROP_LOSSY_COMPRESSION = new ValueProperty( TYPE, "LossyCompression" );
    
    Value<Boolean> getLossyCompression();
    void setLossyCompression( String value );
    void setLossyCompression( Boolean value );
    
}
