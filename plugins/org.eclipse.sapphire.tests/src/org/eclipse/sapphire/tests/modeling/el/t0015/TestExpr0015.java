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

package org.eclipse.sapphire.tests.modeling.el.t0015;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;

/**
 * Tests Enabled function.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestExpr0015 extends TestExpr
{
    private TestExpr0015( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestExpr0015" );

        suite.addTest( new TestExpr0015( "testEnabledFunction" ) );
        
        return suite;
    }
    
    public void testEnabledFunction()
    {
        final RootElement root = RootElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( root );
        
        testForExpectedValue( context, "${ Enabled( 'Value' ) }", Boolean.FALSE );
        
        root.setEnableValue( true );
        testForExpectedValue( context, "${ Enabled( 'Value' ) }", Boolean.TRUE );
        
        root.setEnableValue( false );
        testForExpectedValue( context, "${ Enabled( 'Value' ) }", Boolean.FALSE );
        
        testForExpectedValue( context, "${ Enabled( 'Child' ) }", Boolean.FALSE );

        root.setEnableChild( true );
        testForExpectedValue( context, "${ Enabled( 'Child' ) }", Boolean.TRUE );
        
        final ChildElement child = root.getChild();
        
        testForExpectedValue( context, "${ Enabled( Child, 'Value' ) }", Boolean.FALSE );
        
        child.setEnableValue( true );
        testForExpectedValue( context, "${ Enabled( Child, 'Value' ) }", Boolean.TRUE );
        
        child.setEnableValue( false );
        testForExpectedValue( context, "${ Enabled( Child, 'Value' ) }", Boolean.FALSE );
        
        testForExpectedValue( context, "${ Enabled( null ) }", null );
        testForExpectedValue( context, "${ Enabled( 'Aklkksjdlfksd' ) }", null );
        testForExpectedValue( context, "${ Enabled( Child, null ) }", null );
        testForExpectedValue( context, "${ Enabled( Child, 'Aslkdjfsldkj' ) }", null );
        testForExpectedValue( context, "${ Enabled( null, 'Value' ) }", null );
        testForExpectedValue( context, "${ Enabled( null, null ) }", null );
        
        testForExpectedError( context, "${ Enabled() }", "Enabled() function does not support 0 arguments." );
        testForExpectedError( context, "${ Enabled( null, null, null ) }", "Enabled() function does not support 3 arguments." );
        
        testForExpectedError( new FunctionContext(), "${ Enabled( 'Abc') }", "Context element not found." );
    }

}

