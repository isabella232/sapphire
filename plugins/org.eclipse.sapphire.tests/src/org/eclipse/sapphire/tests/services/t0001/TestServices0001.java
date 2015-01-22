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

package org.eclipse.sapphire.tests.services.t0001;

import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.services.FactsAggregationService;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests operation of FactsService implementations.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestServices0001 extends SapphireTestCase
{
    @Test
    
    public void testPlain() throws Exception
    {
        test( TestRootElement.PROP_PLAIN );
    }

    @Test
    
    public void testSensitive() throws Exception
    {
        test( TestRootElement.PROP_SENSITIVE );
    }

    @Test
    
    public void testDefaultValue() throws Exception
    {
        test( TestRootElement.PROP_DEFAULT_VALUE, "Default value is \"123\"" );
    }

    @Test
    
    public void testDefaultValueSensitive() throws Exception
    {
        test( TestRootElement.PROP_DEFAULT_VALUE_SENSITIVE, "Has default value" );
    }

    @Test
    
    public void testNumericRangeMin() throws Exception
    {
        test( TestRootElement.PROP_NUMERIC_RANGE_MIN, "Minimum value is 1" );
    }
    
    @Test
    
    public void testNumericRangeMax() throws Exception
    {
        test( TestRootElement.PROP_NUMERIC_RANGE_MAX, "Maximum value is 100" );
    }

    @Test
    
    public void testNumericRangeMinMax() throws Exception
    {
        test( TestRootElement.PROP_NUMERIC_RANGE_MIN_MAX, "Minimum value is 1", "Maximum value is 100" );
    }

    @Test
    
    public void testRequiredValue() throws Exception
    {
        test( TestRootElement.PROP_REQUIRED_VALUE, "Must be specified" );
    }

    @Test
    
    public void testRequiredValueWithDefault() throws Exception
    {
        test( TestRootElement.PROP_REQUIRED_VALUE_WITH_DEFAULT, "Default value is \"123\"" );
    }
    
    @Test
    
    public void testRequiredElement() throws Exception
    {
        test( TestRootElement.PROP_REQUIRED_ELEMENT, "Must be specified" );
    }
    
    @Test
    
    public void testReadOnly() throws Exception
    {
        test( TestRootElement.PROP_READ_ONLY, "Cannot be modified" );
    }
    
    @Test
    
    public void testCountConstraintAtLeastOne() throws Exception
    {
        test( TestRootElement.PROP_COUNT_CONSTRAINT_AT_LEAST_ONE, "Must have at least one" );
    }
    
    @Test
    
    public void testCountConstraintMin() throws Exception
    {
        test( TestRootElement.PROP_COUNT_CONSTRAINT_MIN, "Must have at least 2 items" );
    }
    
    @Test
    
    public void testCountConstraintMax() throws Exception
    {
        test( TestRootElement.PROP_COUNT_CONSTRAINT_MAX, "Must have at most 200 items" );
    }
    
    @Test
    
    public void testCountConstraintMinMax() throws Exception
    {
        test( TestRootElement.PROP_COUNT_CONSTRAINT_MIN_MAX, "Must have at least 2 items", "Must have at most 200 items" );
    }
    
    @Test
    
    public void testAbsolutePath() throws Exception
    {
        test( TestRootElement.PROP_ABSOLUTE_PATH, "Must be an absolute path" );
    }

    @Test
    
    public void testMustExist() throws Exception
    {
        test( TestRootElement.PROP_MUST_EXIST, "Must exist" );
    }
    
    @Test
    
    public void testMustExistAbsolutePath() throws Exception
    {
        test( TestRootElement.PROP_MUST_EXIST_ABSOLUTE_PATH, "Must be an absolute path", "Must exist" );
    }
    
    @Test
    
    public void testUnique() throws Exception
    {
        final TestRootElement root = TestRootElement.TYPE.instantiate();
        final TestUniqueChildElement child = root.getUnique().insert();
                
        test( child.property( TestUniqueChildElement.PROP_UNIQUE ), "Must be unique" );
    }

    @Test
    
    public void testFileExtensionsOne() throws Exception
    {
        test( TestRootElement.PROP_FILE_EXTENSIONS_ONE, "Must have \"xml\" file extension" );
    }
    
    @Test
    
    public void testFileExtensionsTwo() throws Exception
    {
        test( TestRootElement.PROP_FILE_EXTENSIONS_TWO, "Must have either \"xml\" or \"java\" file extension" );
    }
    
    @Test
    
    public void testFileExtensionsThree() throws Exception
    {
        test( TestRootElement.PROP_FILE_EXTENSIONS_THREE, "Must have either \"xml\", \"java\" or \"jsp\" file extension" );
    }
    
    @Test
    
    public void testFileExtensionsMany() throws Exception
    {
        test( TestRootElement.PROP_FILE_EXTENSIONS_MANY, "Must have one of these file extensions: \"xml\", \"java\", \"jsp\", \"jspx\"" );
    }

    @Test
    
    public void testValidFileSystemResourceTypeFile() throws Exception
    {
        test( TestRootElement.PROP_VALID_FILE_SYSTEM_RESOURCE_TYPE_FILE, "Must be a file" );
    }
    
    @Test
    
    public void testValidFileSystemResourceTypeFolder() throws Exception
    {
        test( TestRootElement.PROP_VALID_FILE_SYSTEM_RESOURCE_TYPE_FOLDER, "Must be a folder" );
    }
    
    @Test
    @SuppressWarnings( "deprecation" )
    
    public void testDeprecated() throws Exception
    {
        test( TestRootElement.PROP_DEPRECATED, "Deprecated" );
    }
    
    @Test
    
    public void testStatic() throws Exception
    {
        test( TestRootElement.PROP_STATIC_FACT, "First static fact.", "Second static fact.", "Third static fact." );
    }

    private static void test( final PropertyDef property,
                              final String... factsExpected )
    {
        test( TestRootElement.TYPE.instantiate().property( property ), factsExpected );
    }
    
    private static void test( final Property property,
                              final String... factsExpected )
    {
        assertEquals( set( factsExpected ), property.service( FactsAggregationService.class ).facts() );
    }

}
