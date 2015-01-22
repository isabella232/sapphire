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
 * Tests for the less than operator.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class LessThanOperatorTests extends AbstractOperatorTests
{
    @Test
    
    public void testLessThanOperator1()
    {
        test( "${ 3 < 5 }", true );
    }
    
    @Test
    
    public void testLessThanOperator2()
    {
        test( "${ 5 < 3 }", false );
    }
    
    @Test
    
    public void testLessThanOperator3()
    {
        test( "${ 3 < 3 }", false );
    }
    
    @Test
    
    public void testLessThanOperator4()
    {
        test( "${ 3.2 < 5 }", true );
    }
    
    @Test
    
    public void testLessThanOperator5()
    {
        test( "${ 5.3 < 3 }", false );
    }
    
    @Test
    
    public void testLessThanOperator6()
    {
        test( "${ 3.2 < 3.2 }", false );
    }

    @Test
    
    public void testLessThanOperator7()
    {
        test( "${ 3 lt 5 }", true );
    }
    
    @Test
    
    public void testLessThanOperator8()
    {
        test( "${ 5 lt 3 }", false );
    }
    
    @Test
    
    public void testLessThanOperator9()
    {
        test( "${ 3 lt 3 }", false );
    }
    
    @Test
    
    public void testLessThanOperator10()
    {
        test( "${ 3.2 lt 5 }", true );
    }
    
    @Test
    
    public void testLessThanOperator11()
    {
        test( "${ 5.3 lt 3 }", false );
    }
    
    @Test
    
    public void testLessThanOperator12()
    {
        test( "${ 3.2 lt 3.2 }", false );
    }

    @Test
    
    public void testLessThanOperator13()
    {
        test( "${ Integer3 < 7 }", true );
    }

    @Test
    
    public void testLessThanOperator14()
    {
        test( "${ 7 < Integer5 }", false );
    }
    
    @Test
    
    public void testLessThanOperator15()
    {
        test( "${ Integer3 < Integer5 }", true );
    }

}

