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

package org.eclipse.sapphire.tests.modeling.el.t0008;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;

/**
 * Tests InstanceOf function.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestExpr0008

    extends TestExpr
    
{
    private TestExpr0008( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestExpr0008" );

        suite.addTest( new TestExpr0008( "testLiteral1" ) );
        suite.addTest( new TestExpr0008( "testLiteral2" ) );
        suite.addTest( new TestExpr0008( "testLiteral3" ) );
        suite.addTest( new TestExpr0008( "testValueProperty" ) );
        suite.addTest( new TestExpr0008( "testElementProperty" ) );
        suite.addTest( new TestExpr0008( "testNull1" ) );
        suite.addTest( new TestExpr0008( "testNull2" ) );
        suite.addTest( new TestExpr0008( "testNull3" ) );
        suite.addTest( new TestExpr0008( "testUnknownType" ) );
        
        return suite;
    }
    
    public void testLiteral2()
    {
        final FunctionContext context = new FunctionContext();
        testForExpectedValue( context, "${ InstanceOf( 12345, 'java.math.BigInteger' ) }", true );
    }

    public void testLiteral3()
    {
        final FunctionContext context = new FunctionContext();
        testForExpectedValue( context, "${ InstanceOf( 12345, 'java.lang.Number' ) }", true );
    }

    public void testValueProperty()
    {
        final TestModelRoot root = TestModelRoot.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( root );

        root.setInteger( 123 );
        
        testForExpectedValue( context, "${ InstanceOf( Integer, 'java.lang.Integer' ) }", true );
    }
    
    public void testElementProperty()
    {
        final TestModelRoot root = TestModelRoot.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( root );

        root.getElement().element( true, TestModelElementA.TYPE );
        
        testForExpectedValue( context, "${ InstanceOf( Element, 'org.eclipse.sapphire.tests.modeling.el.t0008.TestModelElementA' ) }", true );

        root.getElement().element( true, TestModelElementB.TYPE );
        
        testForExpectedValue( context, "${ InstanceOf( Element, 'org.eclipse.sapphire.tests.modeling.el.t0008.TestModelElementB' ) }", true );
        
        root.getElement().remove();
        
        testForExpectedValue( context, "${ InstanceOf( Element, 'org.eclipse.sapphire.tests.modeling.el.t0008.TestModelElementB' ) }", false );
    }

    public void testNull1()
    {
        final FunctionContext context = new FunctionContext();
        testForExpectedValue( context, "${ InstanceOf( null, null ) }", false );
    }
    
    public void testNull2()
    {
        final FunctionContext context = new FunctionContext();
        testForExpectedValue( context, "${ InstanceOf( 'x', null ) }", false );
    }

    public void testNull3()
    {
        final FunctionContext context = new FunctionContext();
        testForExpectedValue( context, "${ InstanceOf( null, 'java.lang.String' ) }", false );
    }

    public void testLiteral1()
    {
        final FunctionContext context = new FunctionContext();
        testForExpectedValue( context, "${ InstanceOf( 'x', 'java.lang.String' ) }", true );
    }

    public void testUnknownType()
    {
        final FunctionContext context = new FunctionContext();
        testForExpectedValue( context, "${ InstanceOf( 12345, 'java.lang.FooBar' ) }", false );
    }

}

