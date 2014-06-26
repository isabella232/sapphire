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

import java.math.BigInteger;

import org.junit.Test;

/**
 * Tests for the modulo operator.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ModuloOperatorTests extends AbstractOperatorTests
{
    @Test
    
    public void testModuloOperator1()
    {
        test( "${ 32 % 5 }", new BigInteger( "2" ) );
    }
    
    @Test
    
    public void testModuloOperator2()
    {
        test( "${ Integer3 % 2 }", new BigInteger( "1" ) );
    }

    @Test
    
    public void testModuloOperator3()
    {
        test( "${ 2 % Integer5 }", new BigInteger( "2" ) );
    }
    
    @Test
    
    public void testModuloOperator4()
    {
        test( "${ Integer3 % Integer5 }", Long.valueOf( "3" ) );
    }

}

