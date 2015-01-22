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

package org.eclipse.sapphire.tests.modeling.el.t0017;

import java.util.List;

import org.eclipse.sapphire.modeling.el.AndFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.el.OrFunction;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.eclipse.sapphire.util.ListFactory;
import org.junit.Test;

/**
 * Tests AND and OR functions, including arbitrary operand cardinality.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestExpr0017 extends TestExpr
{
    @Test
    
    public void testAndFunction()
    {
        Function function;
        
        function = AndFunction.create();
        assertNull( function );
        
        function = AndFunction.create( Literal.TRUE );
        assertSame( Literal.TRUE, function );

        function = AndFunction.create( ListFactory.<Function>singleton( Literal.TRUE ) );
        assertSame( Literal.TRUE, function );
        
        final TestLogic testLogic = new AndTestLogic();
        
        test( 2, testLogic );
        test( 3, testLogic );
        test( 4, testLogic );
    }
    
    @Test

    public void testOrFunction()
    {
        Function function;
        
        function = OrFunction.create();
        assertNull( function );
        
        function = OrFunction.create( Literal.TRUE );
        assertSame( Literal.TRUE, function );

        function = OrFunction.create( ListFactory.<Function>singleton( Literal.TRUE ) );
        assertSame( Literal.TRUE, function );
        
        final TestLogic testLogic = new OrTestLogic();
        
        test( 2, testLogic );
        test( 3, testLogic );
        test( 4, testLogic );
    }
    
    private void test( final int operands,
                       final TestLogic testLogic )
    {
        test( ListFactory.<Function>empty(), operands, testLogic );
    }

    private void test( final List<Function> operands,
                       final int operandsToAdd,
                       final TestLogic testLogic )
    {
        if( operandsToAdd == 0 )
        {
            testLogic.execute( operands );
        }
        else
        {
            final int nextOperandsToAdd = operandsToAdd - 1;
            
            test( ListFactory.<Function>start().add( operands ).add( Literal.TRUE ).result(), nextOperandsToAdd, testLogic );
            test( ListFactory.<Function>start().add( operands ).add( Literal.FALSE ).result(), nextOperandsToAdd, testLogic );
        }
    }
    
    private static abstract class TestLogic
    {
        public abstract void execute( List<Function> operands );
        
        protected final void verify( final List<Function> inputOperands,
                                     final boolean expectedResult,
                                     final Function function )
        {
            assertEquals( inputOperands.size(), function.operands().size() );
            
            for( int i = 0, n = inputOperands.size(); i < n; i++ )
            {
                assertSame( inputOperands.get( i ), function.operand( i ) );
            }
            
            testForExpectedValue( new FunctionContext(), function, expectedResult );
        }
    }
    
    private static final class AndTestLogic extends TestLogic
    {
        public void execute( final List<Function> operands )
        {
            boolean expectedResult = true;
            
            for( Function operand : operands )
            {
                expectedResult = expectedResult && ( operand == Literal.TRUE );
            }
            
            verify( operands, expectedResult, AndFunction.create( operands ) );
            verify( operands, expectedResult, AndFunction.create( operands.toArray( new Function[ operands.size() ] ) ) );
        }
    }

    private static final class OrTestLogic extends TestLogic
    {
        public void execute( final List<Function> operands )
        {
            boolean expectedResult = false;
            
            for( Function operand : operands )
            {
                expectedResult = expectedResult || ( operand == Literal.TRUE );
            }
            
            verify( operands, expectedResult, OrFunction.create( operands ) );
            verify( operands, expectedResult, OrFunction.create( operands.toArray( new Function[ operands.size() ] ) ) );
        }
    }

}

