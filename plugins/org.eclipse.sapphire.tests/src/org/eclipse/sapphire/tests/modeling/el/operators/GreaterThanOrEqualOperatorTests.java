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

import org.junit.Test;

/**
 * Tests for the greater than or equal operator.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class GreaterThanOrEqualOperatorTests extends AbstractOperatorTests
{
    @Test
    
    public void testGreaterThanOrEqualOperator1()
    {
        test( "${ 3 >= 5 }", false );
    }
    
    @Test
    
    public void testGreaterThanOrEqualOperator2()
    {
        test( "${ 5 >= 3 }", true );
    }
    
    @Test
    
    public void testGreaterThanOrEqualOperator3()
    {
        test( "${ 3 >= 3 }", true );
    }
    
    @Test
    
    public void testGreaterThanOrEqualOperator4()
    {
        test( "${ 3.2 >= 5 }", false );
    }
    
    @Test
    
    public void testGreaterThanOrEqualOperator5()
    {
        test( "${ 5.3 >= 3 }", true );
    }
    
    @Test
    
    public void testGreaterThanOrEqualOperator6()
    {
        test( "${ 3.2 >= 3.2 }", true );
    }
    
    @Test

    public void testGreaterThanOrEqualOperator7()
    {
        test( "${ 3 ge 5 }", false );
    }
    
    @Test
    
    public void testGreaterThanOrEqualOperator8()
    {
        test( "${ 5 ge 3 }", true );
    }
    
    @Test
    
    public void testGreaterThanOrEqualOperator9()
    {
        test( "${ 3 ge 3 }", true );
    }
    
    @Test
    
    public void testGreaterThanOrEqualOperator10()
    {
        test( "${ 3.2 ge 5 }", false );
    }
    
    @Test
    
    public void testGreaterThanOrEqualOperator11()
    {
        test( "${ 5.3 ge 3 }", true );
    }
    
    @Test
    
    public void testGreaterThanOrEqualOperator12()
    {
        test( "${ 3.2 ge 3.2 }", true );
    }
    
    @Test

    public void testGreaterThanOrEqualOperator13()
    {
        test( "${ Integer3 >= 7 }", false );
    }
    
    @Test

    public void testGreaterThanOrEqualOperator14()
    {
        test( "${ 7 >= Integer5 }", true );
    }
    
    @Test
    
    public void testGreaterThanOrEqualOperator15()
    {
        test( "${ Integer3 >= Integer5 }", false );
    }

}

