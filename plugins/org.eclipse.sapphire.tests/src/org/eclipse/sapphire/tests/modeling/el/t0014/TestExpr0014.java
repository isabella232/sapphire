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

package org.eclipse.sapphire.tests.modeling.el.t0014;

import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests VersionMatches and SapphireVersionMatches functions along with String to Version type cast and
 * String to VersionConstraint type cast.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestExpr0014 extends TestExpr
{
    @Test
    
    public void testVersionMatchesFunction()
    {
        final FunctionContext context = new FunctionContext();
        
        testForExpectedValue( context, "${ VersionMatches( '1.2.3', '[1.2.3-2.0)' ) }", Boolean.TRUE );
        testForExpectedValue( context, "${ VersionMatches( '3.5', '[1.2.3-2.0)' ) }", Boolean.FALSE );
    }
    
    @Test
    
    public void testSapphireVersionMatchesFunction()
    {
        final FunctionContext context = new FunctionContext();
        
        testForExpectedValue( context, "${ SapphireVersionMatches( '[8.3-8.3.1)' ) }", Boolean.TRUE );
        testForExpectedValue( context, "${ SapphireVersionMatches( '[0.5-0.6)' ) }", Boolean.FALSE );
    }

}

