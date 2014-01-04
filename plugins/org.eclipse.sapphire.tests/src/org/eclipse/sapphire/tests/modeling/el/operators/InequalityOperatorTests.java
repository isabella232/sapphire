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

import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.junit.Test;

/**
 * Tests for the inequality operator.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class InequalityOperatorTests extends AbstractOperatorTests
{
    @Test
    
    public void testInequalityOperator1()
    {
        test( "${ 3 != 3 }", false );
    }
    
    @Test
    
    public void testInequalityOperator2()
    {
        test( "${ 3 != 5 }", true );
    }

    @Test
    
    public void testInequalityOperator3()
    {
        test( "${ 3.2 != 3.2 }", false );
    }

    @Test
    
    public void testInequalityOperator4()
    {
        test( "${ 3.2 != 5 }", true );
    }
    
    @Test
    
    public void testInequalityOperator5()
    {
        test( "${ 'abc' != 'abc' }", false );
    }

    @Test
    
    public void testInequalityOperator6()
    {
        test( "${ 'abc' != 'xyz' }", true );
    }
    
    @Test
    
    public void testInequalityOperator7()
    {
        test( "${ 3 ne 3 }", false );
    }

    @Test
    
    public void testInequalityOperator8()
    {
        test( "${ 3 ne 5 }", true );
    }

    @Test
    
    public void testInequalityOperator9()
    {
        test( "${ 3.2 ne 3.2 }", false );
    }

    @Test
    
    public void testInequalityOperator10()
    {
        test( "${ 3.2 ne 5 }", true );
    }
    
    @Test
    
    public void testInequalityOperator11()
    {
        test( "${ 'abc' ne 'abc' }", false );
    }

    @Test
    
    public void testInequalityOperator12()
    {
        test( "${ 'abc' ne 'xyz' }", true );
    }

    @Test
    
    public void testInequalityOperator13()
    {
        test( "${ Integer3 != 7 }", true );
    }

    @Test
    
    public void testInequalityOperator14()
    {
        test( "${ 7 != Integer5 }", true );
    }
    
    @Test
    
    public void testInequalityOperator15()
    {
        test( "${ Integer3 != Integer5 }", true );
    }

    @Test
    
    public void InequalityOperator_ElementProperty_Null()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        final FunctionResult fr = ExpressionLanguageParser.parse( "${ ChildElement != null }" ).evaluate( context );
        
        try
        {
            assertEquals( Boolean.FALSE, fr.value() );
            
            element.getChildElement().content( true );
            assertEquals( Boolean.TRUE, fr.value() );
            
            element.getChildElement().clear();
            assertEquals( Boolean.FALSE, fr.value() );
        }
        finally
        {
            fr.dispose();
        }
    }

}

