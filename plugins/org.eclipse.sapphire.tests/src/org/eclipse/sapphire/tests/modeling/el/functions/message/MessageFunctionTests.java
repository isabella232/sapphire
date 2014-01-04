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

package org.eclipse.sapphire.tests.modeling.el.functions.message;

import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.eclipse.sapphire.tests.modeling.el.functions.severity.TestElement;
import org.junit.Test;

/**
 * Tests Message function.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class MessageFunctionTests extends TestExpr
{
    @Test
    
    public void testMessageFunction()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        final FunctionResult fr = ExpressionLanguageParser.parse( "${ IntegerValue.Validation.Message }" ).evaluate( context );
        
        try
        {
            assertEquals( "ok", fr.value() );
            
            element.setIntegerValue( 3 );
            assertEquals( "ok", fr.value() );
            
            element.setIntegerValue( "abc" );
            assertEquals( "\"abc\" is not a valid integer", fr.value() );
            
            element.setIntegerValue( 4 );
            assertEquals( "ok", fr.value() );
        }
        finally
        {
            fr.dispose();
        }
    }
    
    @Test

    public void testMessageFunctionNull()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        final FunctionResult fr = ExpressionLanguageParser.parse( "${ Message( null ) }" ).evaluate( context );
        
        try
        {
            final Status st = fr.status();
            
            assertEquals( Status.Severity.ERROR, st.severity() );
            assertEquals( "Function Message does not accept nulls in position 0.", st.message() );
        }
        finally
        {
            fr.dispose();
        }
    }
    
    @Test

    public void testMessageFunctionWrongType()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        final FunctionResult fr = ExpressionLanguageParser.parse( "${ Message( 'abc' ) }" ).evaluate( context );
        
        try
        {
            final Status st = fr.status();
            
            assertEquals( Status.Severity.ERROR, st.severity() );
            assertEquals( "Function Message( java.lang.String ) is undefined.", st.message() );
        }
        finally
        {
            fr.dispose();
        }
    }
    
}
