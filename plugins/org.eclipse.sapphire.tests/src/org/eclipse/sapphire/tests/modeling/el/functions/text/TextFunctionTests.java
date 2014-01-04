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

package org.eclipse.sapphire.tests.modeling.el.functions.text;

import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests Text function.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TextFunctionTests extends TestExpr
{
    @Test
    
    public void testTextFunction()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        FunctionResult fr = ExpressionLanguageParser.parse( "${ IntegerValue.Text }" ).evaluate( context );
        
        try
        {
            assertNull( fr.value() );
            
            element.setIntegerValue( 3 );
            assertEquals( "3", fr.value() );

            element.setIntegerValue( "abc" );
            assertEquals( "abc", fr.value() );
        }
        finally
        {
            fr.dispose();
        }

        fr = ExpressionLanguageParser.parse( "${ IntegerValueWithDefault.Text }" ).evaluate( context );
        
        try
        {
            assertEquals( "1", fr.value() );
            
            element.setIntegerValueWithDefault( 3 );
            assertEquals( "3", fr.value() );

            element.setIntegerValueWithDefault( "abc" );
            assertEquals( "abc", fr.value() );
        }
        finally
        {
            fr.dispose();
        }
    }
    
    @Test

    public void testTextFunctionNull()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        final FunctionResult fr = ExpressionLanguageParser.parse( "${ Text( null ) }" ).evaluate( context );
        
        try
        {
            final Status st = fr.status();
            
            assertEquals( Status.Severity.ERROR, st.severity() );
            assertEquals( "Function Text does not accept nulls in position 0.", st.message() );
        }
        finally
        {
            fr.dispose();
        }
    }
    
    @Test

    public void testTextFunctionWrongType()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        final FunctionResult fr = ExpressionLanguageParser.parse( "${ Text( 'abc' ) }" ).evaluate( context );
        
        try
        {
            final Status st = fr.status();
            
            assertEquals( Status.Severity.ERROR, st.severity() );
            assertEquals( "Function Text( java.lang.String ) is undefined.", st.message() );
        }
        finally
        {
            fr.dispose();
        }
    }

}
