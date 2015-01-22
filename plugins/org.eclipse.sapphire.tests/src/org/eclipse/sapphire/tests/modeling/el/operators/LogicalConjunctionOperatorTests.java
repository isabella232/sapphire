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
 * Tests for the logical conjunction operator.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class LogicalConjunctionOperatorTests extends AbstractOperatorTests
{
    @Test
    
    public void testLogicalConjunctionOperator1()
    {
        test( "${ true && true }", true );
    }
    
    @Test
    
    public void testLogicalConjunctionOperator2()
    {
        test( "${ true && false }", false );
    }
    
    @Test
    
    public void testLogicalConjunctionOperator3()
    {
        test( "${ false && true }", false );
    }
    
    @Test
    
    public void testLogicalConjunctionOperator4()
    {
        test( "${ false && false }", false );
    }
    
    @Test
    
    public void testLogicalConjunctionOperator5()
    {
        test( "${ true and true }", true );
    }
    
    @Test
    
    public void testLogicalConjunctionOperator6()
    {
        test( "${ true and false }", false );
    }
    
    @Test
    
    public void testLogicalConjunctionOperator7()
    {
        test( "${ false and true }", false );
    }
    
    @Test
    
    public void testLogicalConjunctionOperator8()
    {
        test( "${ false and false }", false );
    }

    @Test
    
    public void testLogicalConjunctionOperator9()
    {
        test( "${ BooleanTrue && true }", true );
    }

    @Test
    
    public void testLogicalConjunctionOperator10()
    {
        test( "${ true && BooleanFalse }", false );
    }
    
    @Test
    
    public void testLogicalConjunctionOperator11()
    {
        test( "${ BooleanTrue && BooleanFalse }", false );
    }

}
