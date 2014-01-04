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

package org.eclipse.sapphire.tests.modeling.el.t0008;

import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests InstanceOf function.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestExpr0008 extends TestExpr
{
    @Test
    
    public void testLiteral2()
    {
        final FunctionContext context = new FunctionContext();
        testForExpectedValue( context, "${ InstanceOf( 12345, 'java.math.BigInteger' ) }", true );
    }

    @Test
    
    public void testLiteral3()
    {
        final FunctionContext context = new FunctionContext();
        testForExpectedValue( context, "${ InstanceOf( 12345, 'java.lang.Number' ) }", true );
    }
    
    @Test

    public void testValueProperty()
    {
        final TestModelRoot root = TestModelRoot.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( root );

        root.setInteger( 123 );
        
        testForExpectedValue( context, "${ InstanceOf( Integer, 'java.lang.Integer' ) }", true );
    }
    
    @Test
    
    public void testElementProperty()
    {
        final TestModelRoot root = TestModelRoot.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( root );

        root.getElement().content( true, TestModelElementA.TYPE );
        
        testForExpectedValue( context, "${ InstanceOf( Element, 'org.eclipse.sapphire.tests.modeling.el.t0008.TestModelElementA' ) }", true );

        root.getElement().content( true, TestModelElementB.TYPE );
        
        testForExpectedValue( context, "${ InstanceOf( Element, 'org.eclipse.sapphire.tests.modeling.el.t0008.TestModelElementB' ) }", true );
        
        root.getElement().clear();
        
        testForExpectedValue( context, "${ InstanceOf( Element, 'org.eclipse.sapphire.tests.modeling.el.t0008.TestModelElementB' ) }", false );
    }
    
    @Test

    public void testNull1()
    {
        final FunctionContext context = new FunctionContext();
        testForExpectedValue( context, "${ InstanceOf( null, null ) }", false );
    }
    
    @Test
    
    public void testNull2()
    {
        final FunctionContext context = new FunctionContext();
        testForExpectedValue( context, "${ InstanceOf( 'x', null ) }", false );
    }
    
    @Test

    public void testNull3()
    {
        final FunctionContext context = new FunctionContext();
        testForExpectedValue( context, "${ InstanceOf( null, 'java.lang.String' ) }", false );
    }
    
    @Test

    public void testLiteral1()
    {
        final FunctionContext context = new FunctionContext();
        testForExpectedValue( context, "${ InstanceOf( 'x', 'java.lang.String' ) }", true );
    }
    
    @Test

    public void testUnknownType()
    {
        final FunctionContext context = new FunctionContext();
        testForExpectedValue( context, "${ InstanceOf( 12345, 'java.lang.FooBar' ) }", false );
    }

}

