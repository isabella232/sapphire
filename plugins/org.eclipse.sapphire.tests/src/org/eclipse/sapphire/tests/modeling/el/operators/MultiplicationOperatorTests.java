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

package org.eclipse.sapphire.tests.modeling.el.operators;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

/**
 * Tests for the multiplication operator.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class MultiplicationOperatorTests extends AbstractOperatorTests
{
    @Test
    
    public void testMultiplicationOperator1()
    {
        test( "${ 3 * 5 }", new BigInteger( "15" ) );
    }
    
    @Test
    
    public void testMultiplicationOperator2()
    {
        test( "${ 3 * 5 * 7 }", new BigInteger( "105" ) );
    }
    
    @Test
    
    public void testMultiplicationOperator3()
    {
        test( "${ 3.2 * 5 }", new BigDecimal( "16.0" ) );
    }

    @Test
    
    public void testMultiplicationOperator4()
    {
        test( "${ 3.2 * 6}", new BigDecimal( "19.2" ) );
    }

    @Test
    
    public void testMultiplicationOperator5()
    {
        test( "${ Integer3 * 7 }", new BigInteger( "21" ) );
    }

    @Test
    
    public void testMultiplicationOperator6()
    {
        test( "${ 7 * Integer5 }", new BigInteger( "35" ) );
    }
    
    @Test
    
    public void testMultiplicationOperator7()
    {
        test( "${ Integer3 * Integer5 }", new Long( "15" ) );
    }

}

