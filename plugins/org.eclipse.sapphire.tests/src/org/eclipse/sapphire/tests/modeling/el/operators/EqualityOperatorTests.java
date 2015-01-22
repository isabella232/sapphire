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

import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.junit.Test;

/**
 * Tests for the equality operator.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class EqualityOperatorTests extends AbstractOperatorTests
{
    @Test
    
    public void testEqualityOperator1()
    {
        test( "${ 3 == 3 }", true );
    }
    
    @Test

    public void testEqualityOperator2()
    {
        test( "${ 3 == 5 }", false );
    }
    
    @Test

    public void testEqualityOperator3()
    {
        test( "${ 3.2 == 3.2 }", true );
    }
    
    @Test

    public void testEqualityOperator4()
    {
        test( "${ 3.2 == 5 }", false );
    }
    
    @Test
    
    public void testEqualityOperator5()
    {
        test( "${ 'abc' == 'abc' }", true );
    }
    
    @Test

    public void testEqualityOperator6()
    {
        test( "${ 'abc' == 'xyz' }", false );
    }
    
    @Test
    
    public void testEqualityOperator7()
    {
        test( "${ 3 eq 3 }", true );
    }
    
    @Test

    public void testEqualityOperator8()
    {
        test( "${ 3 eq 5 }", false );
    }
    
    @Test

    public void testEqualityOperator9()
    {
        test( "${ 3.2 eq 3.2 }", true );
    }
    
    @Test

    public void testEqualityOperator10()
    {
        test( "${ 3.2 eq 5 }", false );
    }
    
    @Test
    
    public void testEqualityOperator11()
    {
        test( "${ 'abc' eq 'abc' }", true );
    }
    
    @Test

    public void testEqualityOperator12()
    {
        test( "${ 'abc' eq 'xyz' }", false );
    }
    
    @Test

    public void testEqualityOperator13()
    {
        test( "${ Integer3 == 7 }", false );
    }
    
    @Test

    public void testEqualityOperator14()
    {
        test( "${ 7 == Integer5 }", false );
    }
    
    @Test
    
    public void testEqualityOperator15()
    {
        test( "${ Integer3 == Integer5 }", false );
    }
    
    @Test
    
    public void EqualityOperator_ElementProperty_Null()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        final FunctionResult fr = ExpressionLanguageParser.parse( "${ ChildElement == null }" ).evaluate( context );
        
        try
        {
            assertEquals( Boolean.TRUE, fr.value() );
            
            element.getChildElement().content( true );
            assertEquals( Boolean.FALSE, fr.value() );
            
            element.getChildElement().clear();
            assertEquals( Boolean.TRUE, fr.value() );
        }
        finally
        {
            fr.dispose();
        }
    }

}

