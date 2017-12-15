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

package org.eclipse.sapphire.tests.modeling.el.t0005;

import java.math.BigDecimal;
import java.math.BigInteger;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;

/**
 * Tests List function.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestExpr0005

    extends TestExpr
    
{
    private TestExpr0005( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestExpr0005" );

        suite.addTest( new TestExpr0005( "testEmpty" ) );
        suite.addTest( new TestExpr0005( "testSingleton" ) );
        suite.addTest( new TestExpr0005( "testMultiple" ) );
        suite.addTest( new TestExpr0005( "testVaried" ) );
        
        return suite;
    }
    
    public void testEmpty()
    {
        testForExpectedValue( new FunctionContext(), "${ List() }", list() );
    }
    
    public void testSingleton()
    {
        testForExpectedValue( new FunctionContext(), "${ List( 'x' ) }", list( "x" ) );
    }
    
    public void testMultiple()
    {
        testForExpectedValue( new FunctionContext(), "${ List( 'x', 'y', 'z' ) }", list( "x", "y", "z" ) );
    }
    
    @SuppressWarnings( "unchecked" )
    
    public void testVaried()
    {
        testForExpectedValue( new FunctionContext(), "${ List( 'x', 123, 123.456 ) }", list( "x", new BigInteger( "123" ), new BigDecimal( "123.456" ) ) );
    }
    
}

