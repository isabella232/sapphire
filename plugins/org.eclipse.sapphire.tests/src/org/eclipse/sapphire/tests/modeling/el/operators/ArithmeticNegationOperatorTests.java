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
 * Tests for the arithmetic negation operator.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ArithmeticNegationOperatorTests extends AbstractOperatorTests
{
    @Test
    
    public void testArithmeticNegationOperator1()
    {
        test( "${ -5 }", new BigInteger( "-5" ) );
    }
    
    @Test

    public void testArithmeticNegationOperator2()
    {
        test( "${ -5.23 }", new BigDecimal( "-5.23" ) );
    }
    
    @Test

    public void testArithmeticNegationOperator3()
    {
        test( "${ -Integer3 }", Integer.valueOf( "-3" ) );
    }

}

