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

import org.junit.Test;

/**
 * Tests for the division operator.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DivisionOperatorTests extends AbstractOperatorTests
{
    @Test
    
    public void testDivisionOperator1()
    {
        test( "${ 32 / 5 }", new BigDecimal( "6" ) );
    }
    
    @Test
    
    public void testDivisionOperator2()
    {
        test( "${ 322 / 5 / 7 }", new BigDecimal( "9" ) );
    }
    
    @Test
    
    public void testDivisionOperator3()
    {
        test( "${ 32.2 / 5 }", new BigDecimal( "6.4" ) );
    }
    
    @Test
    
    public void testDivisionOperator4()
    {
        test( "${ 32 div 5 }", new BigDecimal( "6" ) );
    }
    
    @Test
    
    public void testDivisionOperator5()
    {
        test( "${ 322 div 5 div 7 }", new BigDecimal( "9" ) );
    }
    
    @Test
    
    public void testDivisionOperator6()
    {
        test( "${ 32.2 div 5 }", new BigDecimal( "6.4" ) );
    }
    
    @Test

    public void testDivisionOperator7()
    {
        test( "${ Integer3 / 2 }", new Double( "1.5" ) );
    }
    
    @Test

    public void testDivisionOperator8()
    {
        test( "${ 2 / Integer5 }", new BigDecimal( "0" ) );
    }
    
    @Test
    
    public void testDivisionOperator9()
    {
        test( "${ Integer3 / Integer5 }", new Double( "0.6" ) );
    }

}

