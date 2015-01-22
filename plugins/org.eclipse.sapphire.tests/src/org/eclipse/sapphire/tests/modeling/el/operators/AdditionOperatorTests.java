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

package org.eclipse.sapphire.tests.modeling.el.operators;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

/**
 * Tests for the addition operator.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class AdditionOperatorTests extends AbstractOperatorTests
{
    @Test
    
    public void testAdditionOperator1()
    {
        test( "${ 3 + 5 }", new BigInteger( "8" ) );
    }
    
    @Test
    
    public void testAdditionOperator2()
    {
        test( "${ 3 + 5 + 7 }", new BigInteger( "15" ) );
    }
    
    @Test
    
    public void testAdditionOperator3()
    {
        test( "${ 3.2 + 5 }", new BigDecimal( "8.2" ) );
    }
    
    @Test

    public void testAdditionOperator4()
    {
        test( "${ Integer3 + 7 }", new BigInteger( "10" ) );
    }
    
    @Test

    public void testAdditionOperator5()
    {
        test( "${ 7 + Integer5 }", new BigInteger( "12" ) );
    }
    
    @Test
    
    public void testAdditionOperator6()
    {
        test( "${ Integer3 + Integer5 }", Long.valueOf( "8" ) );
    }

}

