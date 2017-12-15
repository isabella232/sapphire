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

package org.eclipse.sapphire.samples.gallery;

import org.eclipse.core.runtime.IPath;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.AbsolutePath;
import org.eclipse.sapphire.modeling.annotations.BasePathsProvider;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.annotations.ValidFileExtensions;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;
import org.eclipse.sapphire.modeling.xml.IModelElementForXml;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBindingMapping;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.samples.gallery.ui.CustomBasePathsProvider;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateXmlBinding

public interface IBrowseSupportGallery

    extends IModelElementForXml
    
{
    ModelElementType TYPE = new ModelElementType( IBrowseSupportGallery.class );
    
    // *** AbsoluteFilePath ***
    
    @Type( base = IPath.class )
    @Label( standard = "absolute file path" )
    @AbsolutePath
    @ValidFileSystemResourceType( FileSystemResourceType.FILE )
    @ValidFileExtensions( { "jar", "zip" } )
    @MustExist
    @XmlBinding( path = "absolute-file-path" )
    
    ValueProperty PROP_ABSOLUTE_FILE_PATH = new ValueProperty( TYPE, "AbsoluteFilePath" );
    
    Value<IPath> getAbsoluteFilePath();
    void setAbsoluteFilePath( String value );
    void setAbsoluteFilePath( IPath value );

    // *** AbsoluteFolderPath ***
    
    @Type( base = IPath.class )
    @Label( standard = "absolute folder path" )
    @AbsolutePath
    @ValidFileSystemResourceType( FileSystemResourceType.FOLDER )
    @MustExist
    @XmlBinding( path = "absolute-folder-path" )
    
    ValueProperty PROP_ABSOLUTE_FOLDER_PATH = new ValueProperty( TYPE, "AbsoluteFolderPath" );
    
    Value<IPath> getAbsoluteFolderPath();
    void setAbsoluteFolderPath( String value );
    void setAbsoluteFolderPath( IPath value );
    
    // *** RelativeFilePath ***
    
    @Type( base = IPath.class )
    @Label( standard = "relative file path" )
    @BasePathsProvider( CustomBasePathsProvider.class )
    @ValidFileSystemResourceType( FileSystemResourceType.FILE )
    @ValidFileExtensions( "dll" )
    @MustExist
    @XmlBinding( path = "relative-folder-path" )
    
    ValueProperty PROP_RELATIVE_FILE_PATH = new ValueProperty( TYPE, "RelativeFilePath" );
    
    Value<IPath> getRelativeFilePath();
    void setRelativeFilePath( String value );
    void setRelativeFilePath( IPath value );
    
    // *** MultiOptionPath ***
    
    @Type( base = IPath.class )
    @Label( standard = "multi option path" )
    @XmlBinding( path = "multi-option-path" )

    ValueProperty PROP_MULTI_OPTION_PATH = new ValueProperty( TYPE, "MultiOptionPath" );
    
    Value<IPath> getMultiOptionPath();
    void setMultiOptionPath( String value );
    void setMultiOptionPath( IPath value );
    
    // *** List ***
    
    @Type( base = IBrowseSupportGalleryListEntry.class )
    @ListPropertyXmlBinding( mappings = { @ListPropertyXmlBindingMapping( element = "list-entry", type = IBrowseSupportGalleryListEntry.class ) } )
    @Label( standard = "list" )
    
    ListProperty PROP_LIST = new ListProperty( TYPE, "List" );
    
    ModelElementList<IBrowseSupportGalleryListEntry> getList();
    
}
