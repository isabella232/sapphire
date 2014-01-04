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

import org.junit.Test;

/**
 * Tests for the less than or equal operator.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class LessThanOrEqualOperatorTests extends AbstractOperatorTests
{
    @Test
    
    public void testLessThanOrEqualOperator1()
    {
        test( "${ 3 <= 5 }", true );
    }
    
    @Test
    
    public void testLessThanOrEqualOperator2()
    {
        test( "${ 5 <= 3 }", false );
    }
    
    @Test
    
    public void testLessThanOrEqualOperator3()
    {
        test( "${ 3 <= 3 }", true );
    }
    
    @Test
    
    public void testLessThanOrEqualOperator4()
    {
        test( "${ 3.2 <= 5 }", true );
    }
    
    @Test
    
    public void testLessThanOrEqualOperator5()
    {
        test( "${ 5.3 <= 3 }", false );
    }
    
    @Test
    
    public void testLessThanOrEqualOperator6()
    {
        test( "${ 3.2 <= 3.2 }", true );
    }

    @Test
    
    public void testLessThanOrEqualOperator7()
    {
        test( "${ 3 le 5 }", true );
    }
    
    @Test
    
    public void testLessThanOrEqualOperator8()
    {
        test( "${ 5 le 3 }", false );
    }
    
    @Test
    
    public void testLessThanOrEqualOperator9()
    {
        test( "${ 3 le 3 }", true );
    }
    
    @Test
    
    public void testLessThanOrEqualOperator10()
    {
        test( "${ 3.2 le 5 }", true );
    }
    
    @Test
    
    public void testLessThanOrEqualOperator11()
    {
        test( "${ 5.3 le 3 }", false );
    }
    
    @Test
    
    public void testLessThanOrEqualOperator12()
    {
        test( "${ 3.2 le 3.2 }", true );
    }

    @Test
    
    public void testLessThanOrEqualOperator13()
    {
        test( "${ Integer3 <= 7 }", true );
    }

    @Test
    
    public void testLessThanOrEqualOperator14()
    {
        test( "${ 7 <= Integer5 }", false );
    }
    
    @Test
    
    public void testLessThanOrEqualOperator15()
    {
        test( "${ Integer3 <= Integer5 }", true );
    }

}

