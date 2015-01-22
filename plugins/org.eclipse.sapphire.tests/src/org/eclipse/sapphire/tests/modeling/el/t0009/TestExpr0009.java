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

package org.eclipse.sapphire.tests.modeling.el.t0009;

import java.math.BigDecimal;

import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests Scale function.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestExpr0009 extends TestExpr
{
    @Test
    
    public void test()
    {
        final FunctionContext context = new FunctionContext();
        
        // Increasing the scale.
        
        testForExpectedValue( context, "${ Scale( 2, 1 ) }", new BigDecimal( "2.0" ) );
        testForExpectedValue( context, "${ Scale( 2, 2 ) }", new BigDecimal( "2.00" ) );
        testForExpectedValue( context, "${ Scale( 2.1, 2 ) }", new BigDecimal( "2.10" ) );
        testForExpectedValue( context, "${ Scale( 2.1, 4 ) }", new BigDecimal( "2.1000" ) );
        
        // Rounding to a fractional number.
        
        testForExpectedValue( context, "${ Scale( 2.1234, 2 ) }", new BigDecimal( "2.12" ) );
        testForExpectedValue( context, "${ Scale( 2.1254, 2 ) }", new BigDecimal( "2.13" ) );
        
        // Rounding to a whole number.
        
        testForExpectedValue( context, "${ Scale( 2.1234, 0 ) }", new BigDecimal( "2" ) );
        testForExpectedValue( context, "${ Scale( 2.6254, 0 ) }", new BigDecimal( "3" ) );
        
        // Negative scale.
        
        testForExpectedValue( context, "${ Scale( 23.6254, -1 ) }", new BigDecimal( "2E+1" ) );
        testForExpectedValue( context, "${ Scale( 145844.6254, -3 ) }", new BigDecimal( "146E+3" ) );
    }
    
}

