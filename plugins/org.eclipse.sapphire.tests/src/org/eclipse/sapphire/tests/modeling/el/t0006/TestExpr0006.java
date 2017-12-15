/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.el.t0006;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;

/**
 * Tests IN operator.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestExpr0006

    extends TestExpr
    
{
    private TestExpr0006( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestExpr0006" );

        suite.addTest( new TestExpr0006( "test" ) );
        
        return suite;
    }
    
    public void test()
    {
        final FunctionContext context = new FunctionContext();
        
        testForExpectedValue( context, "${ 'x' IN List( 'x', 'y', 'z' ) }", true );
        testForExpectedValue( context, "${ 'y' IN List( 'x', 'y', 'z' ) }", true );
        testForExpectedValue( context, "${ 'z' IN List( 'x', 'y', 'z' ) }", true );

        testForExpectedValue( context, "${ 'a' IN List( 'x', 'y', 'z' ) }", false );
        
        testForExpectedValue( context, "${ 'y' IN 'x,y,z' }", true );
        testForExpectedValue( context, "${ 'a' IN 'x,y,z' }", false );
        
        testForExpectedValue( context, "${ null IN null }", false );
        testForExpectedValue( context, "${ 'x' IN null }", false );
        testForExpectedValue( context, "${ null IN List( 'x' ) }", false );
    }
    
}

