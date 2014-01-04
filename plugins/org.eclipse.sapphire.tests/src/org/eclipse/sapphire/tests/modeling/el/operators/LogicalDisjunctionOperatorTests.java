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
 * Tests for the logical disjunction operator.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class LogicalDisjunctionOperatorTests extends AbstractOperatorTests
{
    @Test
    
    public void testLogicalDisjunctionOperator1()
    {
        test( "${ true || true }", true );
    }
    
    @Test
    
    public void testLogicalDisjunctionOperator2()
    {
        test( "${ true || false }", true );
    }
    
    @Test
    
    public void testLogicalDisjunctionOperator3()
    {
        test( "${ false || true }", true );
    }
    
    @Test
    
    public void testLogicalDisjunctionOperator4()
    {
        test( "${ false || false }", false );
    }
    
    @Test
    
    public void testLogicalDisjunctionOperator5()
    {
        test( "${ true or true }", true );
    }
    
    @Test
    
    public void testLogicalDisjunctionOperator6()
    {
        test( "${ true or false }", true );
    }
    
    @Test
    
    public void testLogicalDisjunctionOperator7()
    {
        test( "${ false or true }", true );
    }
    
    @Test
    
    public void testLogicalDisjunctionOperator8()
    {
        test( "${ false or false }", false );
    }
    
    @Test
    
    public void testLogicalDisjunctionOperator9()
    {
        test( "${ BooleanTrue || true }", true );
    }

    @Test
    
    public void testLogicalDisjunctionOperator10()
    {
        test( "${ true || BooleanFalse }", true );
    }
    
    @Test
    
    public void testLogicalDisjunctionOperator11()
    {
        test( "${ BooleanTrue || BooleanFalse }", true );
    }

}

