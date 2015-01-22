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
 * Tests for the logical negation operator.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class LogicalNegationOperatorTests extends AbstractOperatorTests
{
    @Test
    
    public void testLogicalNegationOperator1()
    {
        test( "${ ! true }", false );
    }
    
    @Test
    
    public void testLogicalNegationOperator2()
    {
        test( "${ not true }", false );
    }
    
    @Test
    
    public void testLogicalNegationOperator3()
    {
        test( "${ ! false }", true );
    }
    
    @Test
    
    public void testLogicalNegationOperator4()
    {
        test( "${ ! BooleanTrue }", false );
    }

}

