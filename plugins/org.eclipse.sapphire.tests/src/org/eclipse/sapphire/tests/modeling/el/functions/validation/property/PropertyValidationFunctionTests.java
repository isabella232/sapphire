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

package org.eclipse.sapphire.tests.modeling.el.functions.validation.property;

import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests Validation function for properties.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PropertyValidationFunctionTests extends TestExpr
{
    @Test
    
    public void testValidationFunction()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        try( FunctionResult fr = ExpressionLanguageParser.parse( "${ IntegerValue.Validation }" ).evaluate( context ) )
        {
            assertValidationOk( (Status) fr.value() );
            
            element.setIntegerValue( 3 );
            assertValidationOk( (Status) fr.value() );
            
            element.setIntegerValue( "abc" );
            assertValidationError( (Status) fr.value(), "\"abc\" is not a valid integer" );
            
            element.setIntegerValue( 4 );
            assertValidationOk( (Status) fr.value() );
        }
    }
    
    @Test

    public void testValidationFunctionNull()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        try( FunctionResult fr = ExpressionLanguageParser.parse( "${ Validation( null ) }" ).evaluate( context ) )
        {
            final Status st = fr.status();
            
            assertEquals( Status.Severity.ERROR, st.severity() );
            assertEquals( "Function Validation does not accept nulls in position 0.", st.message() );
        }
    }
    
    @Test

    public void testValidationFunctionWrongType()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        try( FunctionResult fr = ExpressionLanguageParser.parse( "${ Validation( 'abc' ) }" ).evaluate( context ) )
        {
            final Status st = fr.status();
            
            assertEquals( Status.Severity.ERROR, st.severity() );
            assertEquals( "Function Validation( java.lang.String ) is undefined.", st.message() );
        }
    }

}
