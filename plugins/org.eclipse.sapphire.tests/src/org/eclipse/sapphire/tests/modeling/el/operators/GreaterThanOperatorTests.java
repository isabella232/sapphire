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
 * Tests for the greater than operator.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class GreaterThanOperatorTests extends AbstractOperatorTests
{
    @Test
    
    public void testGreaterThanOperator1()
    {
        test( "${ 3 > 5 }", false );
    }
    
    @Test
    
    public void testGreaterThanOperator2()
    {
        test( "${ 5 > 3 }", true );
    }
    
    @Test
    
    public void testGreaterThanOperator3()
    {
        test( "${ 3 > 3 }", false );
    }
    
    @Test
    
    public void testGreaterThanOperator4()
    {
        test( "${ 3.2 > 5 }", false );
    }
    
    @Test
    
    public void testGreaterThanOperator5()
    {
        test( "${ 5.3 > 3 }", true );
    }
    
    @Test
    
    public void testGreaterThanOperator6()
    {
        test( "${ 3.2 > 3.2 }", false );
    }
    
    @Test

    public void testGreaterThanOperator7()
    {
        test( "${ 3 gt 5 }", false );
    }
    
    @Test
    
    public void testGreaterThanOperator8()
    {
        test( "${ 5 gt 3 }", true );
    }
    
    @Test
    
    public void testGreaterThanOperator9()
    {
        test( "${ 3 gt 3 }", false );
    }
    
    @Test
    
    public void testGreaterThanOperator10()
    {
        test( "${ 3.2 gt 5 }", false );
    }
    
    @Test
    
    public void testGreaterThanOperator11()
    {
        test( "${ 5.3 gt 3 }", true );
    }
    
    @Test
    
    public void testGreaterThanOperator12()
    {
        test( "${ 3.2 gt 3.2 }", false );
    }
    
    @Test

    public void testGreaterThanOperator13()
    {
        test( "${ Integer3 > 7 }", false );
    }
    
    @Test

    public void testGreaterThanOperator14()
    {
        test( "${ 7 > Integer5 }", true );
    }
    
    @Test
    
    public void testGreaterThanOperator15()
    {
        test( "${ Integer3 > Integer5 }", false );
    }

}

