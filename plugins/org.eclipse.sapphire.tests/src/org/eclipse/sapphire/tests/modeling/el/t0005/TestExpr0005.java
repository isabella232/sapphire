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

package org.eclipse.sapphire.tests.modeling.el.t0005;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests List function.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestExpr0005 extends TestExpr
{
    @Test
    
    public void testEmpty()
    {
        testForExpectedValue( new FunctionContext(), "${ List() }", list() );
    }
    
    @Test
    
    public void testSingleton()
    {
        testForExpectedValue( new FunctionContext(), "${ List( 'x' ) }", list( "x" ) );
    }
    
    @Test
    
    public void testMultiple()
    {
        testForExpectedValue( new FunctionContext(), "${ List( 'x', 'y', 'z' ) }", list( "x", "y", "z" ) );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    
    public void testVaried()
    {
        testForExpectedValue( new FunctionContext(), "${ List( 'x', 123, 123.456 ) }", list( "x", new BigInteger( "123" ), new BigDecimal( "123.456" ) ) );
    }
    
}

