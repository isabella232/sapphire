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
 * Tests for the membership operator.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class MembershipOperatorTests extends AbstractOperatorTests
{
    @Test
    
    public void testMembershipOperator1()
    {
        test( "${ 'x' IN List( 'x', 'y', 'z' ) }", true );
    }
    
    @Test

    public void testMembershipOperator2()
    {
        test( "${ 'y' IN List( 'x', 'y', 'z' ) }", true );
    }
    
    @Test

    public void testMembershipOperator3()
    {
        test( "${ 'z' IN List( 'x', 'y', 'z' ) }", true );
    }
    
    @Test

    public void testMembershipOperator4()
    {
        test( "${ 'a' IN List( 'x', 'y', 'z' ) }", false );
    }
    
    @Test

    public void testMembershipOperator5()
    {
        test( "${ 'y' IN 'x,y,z' }", true );
    }
    
    @Test

    public void testMembershipOperator6()
    {
        test( "${ 'a' IN 'x,y,z' }", false );
    }
    
    @Test

    public void testMembershipOperator7()
    {
        test( "${ null IN null }", false );
    }
    
    @Test

    public void testMembershipOperator8()
    {
        test( "${ 'x' IN null }", false );
    }
    
    @Test

    public void testMembershipOperator9()
    {
        test( "${ null IN List( 'x' ) }", false );
    }
    
}

