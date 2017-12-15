/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.services.t0001;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.AbsolutePath;
import org.eclipse.sapphire.modeling.annotations.CountConstraint;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Fact;
import org.eclipse.sapphire.modeling.annotations.Facts;
import org.eclipse.sapphire.modeling.annotations.FileExtensions;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.NumericRange;
import org.eclipse.sapphire.modeling.annotations.ReadOnly;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;
import org.eclipse.sapphire.tests.EmptyModelElement;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface TestRootElement extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( TestRootElement.class );
    
    // *** NoFacts ***
    
    ValueProperty PROP_NO_FACTS = new ValueProperty( TYPE, "NoFacts" );
    
    Value<String> getNoFacts();
    void setNoFacts( String value );
    
    // *** DefaultValue ***
    
    @DefaultValue( text = "123" )
    
    ValueProperty PROP_DEFAULT_VALUE = new ValueProperty( TYPE, "DefaultValue" );
    
    Value<String> getDefaultValue();
    void setDefaultValue( String value );
    
    // *** NumericRangeMin ***
    
    @Type( base = Integer.class )
    @NumericRange( min = "1" )
    
    ValueProperty PROP_NUMERIC_RANGE_MIN = new ValueProperty( TYPE, "NumericRangeMin" );
    
    Value<Integer> getNumericRangeMin();
    void setNumericRangeMin( String value );
    void setNumericRangeMin( Integer value );
    
    // *** NumericRangeMax ***
    
    @Type( base = Integer.class )
    @NumericRange( max = "100" )

    ValueProperty PROP_NUMERIC_RANGE_MAX = new ValueProperty( TYPE, "NumericRangeMax" );
    
    Value<Integer> getNumericRangeMax();
    void setNumericRangeMax( String value );
    void setNumericRangeMax( Integer value );
    
    // *** NumericRangeMinMax ***
    
    @Type( base = Integer.class )
    @NumericRange( min = "1", max = "100" )
    
    ValueProperty PROP_NUMERIC_RANGE_MIN_MAX = new ValueProperty( TYPE, "NumericRangeMinMax" );
    
    Value<Integer> getNumericRangeMinMax();
    void setNumericRangeMinMax( String value );
    void setNumericRangeMinMax( Integer value );
    
    // *** RequiredValue ***
    
    @Required
    
    ValueProperty PROP_REQUIRED_VALUE = new ValueProperty( TYPE, "RequiredValue" );
    
    Value<String> getRequiredValue();
    void setRequiredValue( String value );
    
    // *** RequiredElement ***
    
    @Type( base = EmptyModelElement.class )
    @Required
    
    ElementProperty PROP_REQUIRED_ELEMENT = new ElementProperty( TYPE, "RequiredElement" );
    
    ModelElementHandle<EmptyModelElement> getRequiredElement();
    
    // *** ReadOnly ***
    
    @ReadOnly
    
    ValueProperty PROP_READ_ONLY = new ValueProperty( TYPE, "ReadOnly" );
    
    Value<String> getReadOnly();
    
    // *** CountConstraintAtLeastOne ***
    
    @Type( base = EmptyModelElement.class )
    @CountConstraint( min = 1 )
    
    ListProperty PROP_COUNT_CONSTRAINT_AT_LEAST_ONE = new ListProperty( TYPE, "CountConstraintAtLeastOne" );
    
    ModelElementList<EmptyModelElement> getCountConstraintAtLeastOne();
    
    // *** CountConstraintMin ***
    
    @Type( base = EmptyModelElement.class )
    @CountConstraint( min = 2 )
    
    ListProperty PROP_COUNT_CONSTRAINT_MIN = new ListProperty( TYPE, "CountConstraintMin" );
    
    ModelElementList<EmptyModelElement> getCountConstraintMin();
    
    // *** CountConstraintMax ***
    
    @Type( base = EmptyModelElement.class )
    @CountConstraint( max = 200 )
    
    ListProperty PROP_COUNT_CONSTRAINT_MAX = new ListProperty( TYPE, "CountConstraintMax" );
    
    ModelElementList<EmptyModelElement> getCountConstraintMax();
    
    // *** CountConstraintMinMax ***
    
    @Type( base = EmptyModelElement.class )
    @CountConstraint( min = 2, max = 200 )

    ListProperty PROP_COUNT_CONSTRAINT_MIN_MAX = new ListProperty( TYPE, "CountConstraintMinMax" );
    
    ModelElementList<EmptyModelElement> getCountConstraintMinMax();
    
    // *** AbsolutePath ***
    
    @Type( base = Path.class )
    @AbsolutePath
    
    ValueProperty PROP_ABSOLUTE_PATH = new ValueProperty( TYPE, "AbsolutePath" );
    
    Value<Path> getAbsolutePath();
    void setAbsolutePath( String value );
    void setAbsolutePath( Path value );
    
    // *** MustExist ***
    
    @Type( base = Path.class )
    @MustExist
    
    ValueProperty PROP_MUST_EXIST = new ValueProperty( TYPE, "MustExist" );
    
    Value<Path> getMustExist();
    void setMustExist( String value );
    void setMustExist( Path value );
    
    // *** MustExistAbsolutePath ***
    
    @Type( base = Path.class )
    @MustExist
    @AbsolutePath
    
    ValueProperty PROP_MUST_EXIST_ABSOLUTE_PATH = new ValueProperty( TYPE, "MustExistAbsolutePath" );
    
    Value<Path> getMustExistAbsolutePath();
    void setMustExistAbsolutePath( String value );
    void setMustExistAbsolutePath( Path value );
    
    // *** NoDuplicates ***
    
    @Type( base = TestNoDuplicatesChildElement.class )
    
    ListProperty PROP_NO_DUPLICATES = new ListProperty( TYPE, "NoDuplicates" );
    
    ModelElementList<TestNoDuplicatesChildElement> getNoDuplicates();
    
    // *** FileExtensionsOne ***
    
    @Type( base = Path.class )
    @FileExtensions( expr = "xml" )
    
    ValueProperty PROP_FILE_EXTENSIONS_ONE = new ValueProperty( TYPE, "FileExtensionsOne" );
    
    Value<Path> getFileExtensionsOne();
    void setFileExtensionsOne( String value );
    void setFileExtensionsOne( Path value );
    
    // *** FileExtensionsTwo ***
    
    @Type( base = Path.class )
    @FileExtensions( expr = "xml,java" )

    ValueProperty PROP_FILE_EXTENSIONS_TWO = new ValueProperty( TYPE, "FileExtensionsTwo" );
    
    Value<Path> getFileExtensionsTwo();
    void setFileExtensionsTwo( String value );
    void setFileExtensionsTwo( Path value );
    
    // *** FileExtensionsThree ***
    
    @Type( base = Path.class )
    @FileExtensions( expr = "xml,java,jsp" )

    ValueProperty PROP_FILE_EXTENSIONS_THREE = new ValueProperty( TYPE, "FileExtensionsThree" );
    
    Value<Path> getFileExtensionsThree();
    void setFileExtensionsThree( String value );
    void setFileExtensionsThree( Path value );
    
    // *** FileExtensionsMany ***
    
    @Type( base = Path.class )
    @FileExtensions( expr = "xml,java,jsp,jspx" )

    ValueProperty PROP_FILE_EXTENSIONS_MANY = new ValueProperty( TYPE, "FileExtensionsMany" );
    
    Value<Path> getFileExtensionsMany();
    void setFileExtensionsMany( String value );
    void setFileExtensionsMany( Path value );
    
    // *** ValidFileSystemResourceTypeFile ***
    
    @Type( base = Path.class )
    @ValidFileSystemResourceType( FileSystemResourceType.FILE )
    
    ValueProperty PROP_VALID_FILE_SYSTEM_RESOURCE_TYPE_FILE = new ValueProperty( TYPE, "ValidFileSystemResourceTypeFile" );
    
    Value<Path> getValidFileSystemResourceTypeFile();
    void setValidFileSystemResourceTypeFile( String value );
    void setValidFileSystemResourceTypeFile( Path value );
    
    // *** ValidFileSystemResourceTypeFolder ***
    
    @Type( base = Path.class )
    @ValidFileSystemResourceType( FileSystemResourceType.FOLDER )
    
    ValueProperty PROP_VALID_FILE_SYSTEM_RESOURCE_TYPE_FOLDER = new ValueProperty( TYPE, "ValidFileSystemResourceTypeFolder" );
    
    Value<Path> getValidFileSystemResourceTypeFolder();
    void setValidFileSystemResourceTypeFolder( String value );
    void setValidFileSystemResourceTypeFolder( Path value );
    
    // *** Deprecated ***
    
    @Deprecated
    
    ValueProperty PROP_DEPRECATED = new ValueProperty( TYPE, "Deprecated" );
    
    Value<String> getDeprecated();
    void setDeprecated( String value );
    
    // *** StaticFact ***
    
    @Fact( statement = "First static fact.")
    @Facts( { @Fact( statement = "Second static fact." ), @Fact( statement = "Third static fact." ) } )
    
    ValueProperty PROP_STATIC_FACT = new ValueProperty( TYPE, "StaticFact" );
    
    Value<String> getStaticFact();
    void setStaticFact( String value );

}
