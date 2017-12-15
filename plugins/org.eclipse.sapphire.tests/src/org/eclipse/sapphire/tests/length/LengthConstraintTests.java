/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.length;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.services.FactsAggregationService;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests @Length feature.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class LengthConstraintTests extends SapphireTestCase
{
    private static final String TEN = "1234567890";
    private static final String FIFTY = TEN + TEN + TEN + TEN + TEN;
    private static final String HUNDRED = FIFTY + FIFTY;
    private static final String LONG = "Sapphire aims to raise UI writing to a higher level of abstraction. The core premise is that the basic building block of UI should not be a widget (text box, label, button, etc.), but rather a property editor. Unlike a widget, a property editor analyzes metadata associated with a given property, renders the appropriate widgets to edit that property and wires up data binding. Data is synchronized, validation is passed from the model to the UI, content assistance is made available, etc.";
    
    @Test

    public void FactsMinValue() throws Exception
    {
        try( TestElement element = TestElement.TYPE.instantiate() )
        {
            assertEquals
            (
                set( "Minimum length is 8" ),
                element.getMinValue().service( FactsAggregationService.class ).facts()
            );
        }
    }

    @Test

    public void FactsMaxValue() throws Exception
    {
        try( TestElement element = TestElement.TYPE.instantiate() )
        {
            assertEquals
            (
                set( "Maximum length is 50" ),
                element.getMaxValue().service( FactsAggregationService.class ).facts()
            );        
        }
    }

    @Test

    public void FactsMinMaxValue() throws Exception
    {
        try( TestElement element = TestElement.TYPE.instantiate() )
        {
            assertEquals
            (
                set( "Minimum length is 17", "Maximum length is 297" ),
                element.getMinMaxValue().service( FactsAggregationService.class ).facts()
            );        
        }
    }

    @Test

    public void FactsMinOneList() throws Exception
    {
        try( TestElement element = TestElement.TYPE.instantiate() )
        {
            assertEquals
            (
                set( "Must have at least one" ),
                element.getMinOneList().service( FactsAggregationService.class ).facts()
            );
        }
    }

    @Test

    public void FactsMinTwoList() throws Exception
    {
        try( TestElement element = TestElement.TYPE.instantiate() )
        {
            assertEquals
            (
                set( "Must have at least 2 items" ),
                element.getMinTwoList().service( FactsAggregationService.class ).facts()
            );
        }
    }

    @Test

    public void FactsMaxList() throws Exception
    {
        try( TestElement element = TestElement.TYPE.instantiate() )
        {
            assertEquals
            (
                set( "Must have at most 12 items" ),
                element.getMaxList().service( FactsAggregationService.class ).facts()
            );
        }
    }

    @Test

    public void FactsMinMaxList() throws Exception
    {
        try( TestElement element = TestElement.TYPE.instantiate() )
        {
            assertEquals
            (
                set( "Must have at least one", "Must have at most 15 items" ),
                element.getMinMaxList().service( FactsAggregationService.class ).facts()
            );
        }
    }

    @Test
    
    public void ValidationMinValue() throws Exception
    {
        try( TestElement element = TestElement.TYPE.instantiate() )
        {
            assertValidationOk( element.getMinValue() );
            
            element.setMinValue( "abc" );
            assertValidationError( element.getMinValue(), "Minimum length is 8" );
            
            element.setMinValue( "1234567" );
            assertValidationError( element.getMinValue(), "Minimum length is 8" );
            
            element.setMinValue( "12345678" );
            assertValidationOk( element.getMinValue() );

            element.setMinValue( LONG );
            assertValidationOk( element.getMinValue() );
        }
    }
    
    @Test
    
    public void ValidationMaxValue() throws Exception
    {
        try( TestElement element = TestElement.TYPE.instantiate() )
        {
            assertValidationOk( element.getMaxValue() );
            
            element.setMaxValue( "abc" );
            assertValidationOk( element.getMaxValue() );

            element.setMaxValue( TEN + TEN + TEN + TEN + TEN );
            assertValidationOk( element.getMaxValue() );

            element.setMaxValue( TEN + TEN + TEN + TEN + TEN + "1" );
            assertValidationError( element.getMaxValue(), "Maximum length is 50" );
            
            element.setMaxValue( LONG );
            assertValidationError( element.getMaxValue(), "Maximum length is 50" );
        }
    }
    
    @Test
    
    public void ValidationMinMaxValue() throws Exception
    {
        try( TestElement element = TestElement.TYPE.instantiate() )
        {
            assertValidationOk( element.getMinMaxValue() );
            
            element.setMinMaxValue( "abc" );
            assertValidationError( element.getMinMaxValue(), "Minimum length is 17" );
            
            element.setMinMaxValue( TEN + "123456" );
            assertValidationError( element.getMinMaxValue(), "Minimum length is 17" );
            
            element.setMinMaxValue( TEN + "1234567" );
            assertValidationOk( element.getMinMaxValue() );

            element.setMinMaxValue( HUNDRED );
            assertValidationOk( element.getMinMaxValue() );

            element.setMinMaxValue( HUNDRED + HUNDRED + FIFTY + TEN + TEN + TEN + TEN + "1234567" );
            assertValidationOk( element.getMinMaxValue() );
            
            element.setMinMaxValue( HUNDRED + HUNDRED + FIFTY + TEN + TEN + TEN + TEN + "12345678" );
            assertValidationError( element.getMinMaxValue(), "Maximum length is 297" );
            
            element.setMinMaxValue( LONG );
            assertValidationError( element.getMinMaxValue(), "Maximum length is 297" );
        }
    }
    
    @Test
    
    public void ValidationMinOneList() throws Exception
    {
        try( TestElement element = TestElement.TYPE.instantiate() )
        {
            assertValidationError( element.getMinOneList(), "Must have at least one" );
            
            add( element.getMinOneList(), 1 );
            assertValidationOk( element.getMinOneList() );
            
            add( element.getMinOneList(), 23 );
            assertValidationOk( element.getMinOneList() );
            
            element.getMinOneList().clear();
            assertValidationError( element.getMinOneList(), "Must have at least one" );
        }
    }
    
    @Test
    
    public void ValidationMinTwoList() throws Exception
    {
        try( TestElement element = TestElement.TYPE.instantiate() )
        {
            assertValidationError( element.getMinTwoList(), "Must have at least 2 items" );
            
            add( element.getMinTwoList(), 1 );
            assertValidationError( element.getMinTwoList(), "Must have at least 2 items" );
            
            add( element.getMinTwoList(), 1 );
            assertValidationOk( element.getMinTwoList() );
            
            add( element.getMinTwoList(), 23 );
            assertValidationOk( element.getMinTwoList() );
            
            element.getMinTwoList().clear();
            assertValidationError( element.getMinTwoList(), "Must have at least 2 items" );
        }
    }
    
    @Test
    
    public void ValidationMaxList() throws Exception
    {
        try( TestElement element = TestElement.TYPE.instantiate() )
        {
            assertValidationOk( element.getMaxList() );
            
            add( element.getMaxList(), 3 );
            assertValidationOk( element.getMaxList() );
            
            add( element.getMaxList(), 9 );
            assertValidationOk( element.getMaxList() );
            
            add( element.getMaxList(), 1 );
            assertValidationError( element.getMaxList(), "Must have at most 12 items" );
            
            add( element.getMaxList(), 57 );
            assertValidationError( element.getMaxList(), "Must have at most 12 items" );
            
            element.getMaxList().clear();
            assertValidationOk( element.getMaxList() );
        }
    }
    
    @Test
    
    public void ValidationMinMaxList() throws Exception
    {
        try( TestElement element = TestElement.TYPE.instantiate() )
        {
            assertValidationError( element.getMinMaxList(), "Must have at least one" );
            
            add( element.getMinMaxList(), 1 );
            assertValidationOk( element.getMinMaxList() );
            
            add( element.getMinMaxList(), 3 );
            assertValidationOk( element.getMinMaxList() );
            
            add( element.getMinMaxList(), 11 );
            assertValidationOk( element.getMinMaxList() );
            
            add( element.getMinMaxList(), 1 );
            assertValidationError( element.getMinMaxList(), "Must have at most 15 items" );
            
            add( element.getMinMaxList(), 57 );
            assertValidationError( element.getMinMaxList(), "Must have at most 15 items" );
            
            element.getMinMaxList().clear();
            assertValidationError( element.getMinMaxList(), "Must have at least one" );
        }
    }
    
    private static void add( final ElementList<?> list, final int items )
    {
        for( int i = 0; i < items; i++ )
        {
            list.insert();
        }
    }
    
}
