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

package org.eclipse.sapphire.tests.modeling.el.t0014;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;

/**
 * Tests VersionMatches and SapphireVersionMatches functions along with String to Version type cast and
 * String to VersionConstraint type cast.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestExpr0014 extends TestExpr
{
    private TestExpr0014( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestExpr0014" );

        suite.addTest( new TestExpr0014( "testVersionMatchesFunction" ) );
        suite.addTest( new TestExpr0014( "testSapphireVersionMatchesFunction" ) );
        
        return suite;
    }
    
    public void testVersionMatchesFunction()
    {
        final FunctionContext context = new FunctionContext();
        
        testForExpectedValue( context, "${ VersionMatches( \"1.2.3\", \"[1.2.3-2.0)\" ) }", Boolean.TRUE );
        testForExpectedValue( context, "${ VersionMatches( \"3.5\", \"[1.2.3-2.0)\" ) }", Boolean.FALSE );
    }
    
    public void testSapphireVersionMatchesFunction()
    {
        final FunctionContext context = new FunctionContext();
        
        testForExpectedValue( context, "${ SapphireVersionMatches( \"[0.6.8-0.6.9)\" ) }", Boolean.TRUE );
        testForExpectedValue( context, "${ SapphireVersionMatches( \"[0.5-0.6)\" ) }", Boolean.FALSE );
    }

}

