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

package org.eclipse.sapphire.tests.modeling.el.t0016;

import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests UpperCase and LowerCase functions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestExpr0016 extends TestExpr
{
    @Test
    
    public void testUpperCaseFunction()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        testForExpectedValue( context, "${ UpperCase( 'abc' ) }", "ABC" );
        testForExpectedValue( context, "${ 'abc'.UpperCase() }", "ABC" );
        
        element.setValue( "Test" );
        
        testForExpectedValue( context, "${ UpperCase( Value ) }", "TEST" );
        testForExpectedValue( context, "${ Value.UpperCase() }", "TEST" );
    }
    
    @Test

    public void testLowerCaseFunction()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        testForExpectedValue( context, "${ LowerCase( 'ABC' ) }", "abc" );
        testForExpectedValue( context, "${ 'ABC'.LowerCase() }", "abc" );
        
        element.setValue( "Test" );
        
        testForExpectedValue( context, "${ LowerCase( Value ) }", "test" );
        testForExpectedValue( context, "${ Value.LowerCase() }", "test" );
    }

}

