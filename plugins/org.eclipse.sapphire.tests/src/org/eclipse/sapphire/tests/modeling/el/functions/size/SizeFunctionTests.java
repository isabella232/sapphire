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

package org.eclipse.sapphire.tests.modeling.el.functions.size;

import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests Size function.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SizeFunctionTests extends TestExpr
{
    @Test
    
    public void testSizeFunctionOnList()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        final FunctionResult fr = ExpressionLanguageParser.parse( "${ List.Size }" ).evaluate( context );
        
        try
        {
            assertEquals( 0, fr.value() );
            
            element.getList().insert();
            assertEquals( 1, fr.value() );
            
            element.getList().insert();
            assertEquals( 2, fr.value() );
            
            element.getList().clear();
            assertEquals( 0, fr.value() );
        }
        finally
        {
            fr.dispose();
        }
    }
    
    @Test

    public void testSizeFunctionOnValue()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        final FunctionResult fr = ExpressionLanguageParser.parse( "${ Value.Size }" ).evaluate( context );
        
        try
        {
            assertEquals( 0, fr.value() );
            
            element.setValue( "abc" );
            assertEquals( 3, fr.value() );
            
            element.setValue( null );
            assertEquals( 0, fr.value() );
        }
        finally
        {
            fr.dispose();
        }
    }
    
    @Test

    public void testSizeFunctionOnString()
    {
        final FunctionContext context = new FunctionContext();
        
        final FunctionResult fr = ExpressionLanguageParser.parse( "${ Size( 'abcdef' ) }" ).evaluate( context );
        
        try
        {
            assertEquals( 6, fr.value() );
        }
        finally
        {
            fr.dispose();
        }
    }
    
    @Test

    public void testSizeFunctionNull()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        final FunctionResult fr = ExpressionLanguageParser.parse( "${ Size( null ) }" ).evaluate( context );
        
        try
        {
            final Status st = fr.status();
            
            assertEquals( Status.Severity.ERROR, st.severity() );
            assertEquals( "Function Size does not accept nulls in position 0.", st.message() );
        }
        finally
        {
            fr.dispose();
        }
    }
    
    @Test

    public void testSizeFunctionWrongType()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        final FunctionResult fr = ExpressionLanguageParser.parse( "${ Size( 3 ) }" ).evaluate( context );
        
        try
        {
            final Status st = fr.status();
            
            assertEquals( Status.Severity.ERROR, st.severity() );
            assertEquals( "Function Size cannot be applied to a java.math.BigInteger object.", st.message() );
        }
        finally
        {
            fr.dispose();
        }
    }

}
