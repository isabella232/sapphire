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

package org.eclipse.sapphire.tests.modeling.el.t0010;

import java.math.BigDecimal;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests Sum function.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestExpr0010 extends TestExpr
{
    @Test
    
    public void testWithArray()
    {
        FunctionContext context;
        
        context = new TestFunctionContext( new Object[] {} );
        testForExpectedValue( context, "${ Sum( Collection ) }", new BigDecimal( "0" ) );

        context = new TestFunctionContext( new Object[] { "1" } );
        testForExpectedValue( context, "${ Sum( Collection ) }", new BigDecimal( "1" ) );

        context = new TestFunctionContext( new Object[] { "1", "2", "3.5" } );
        testForExpectedValue( context, "${ Sum( Collection ) }", new BigDecimal( "6.5" ) );
        
        context = new TestFunctionContext( new Object[] { "1", 2, new BigDecimal( "3.5" ) } );
        testForExpectedValue( context, "${ Sum( Collection ) }", new BigDecimal( "6.5" ) );
    }
    
    @Test
    
    public void testWithList()
    {
        FunctionContext context;
        
        context = new TestFunctionContext( list() );
        testForExpectedValue( context, "${ Sum( Collection ) }", new BigDecimal( "0" ) );

        context = new TestFunctionContext( list( "1" ) );
        testForExpectedValue( context, "${ Sum( Collection ) }", new BigDecimal( "1" ) );

        context = new TestFunctionContext( list( "1", "2", "3.5" ) );
        testForExpectedValue( context, "${ Sum( Collection ) }", new BigDecimal( "6.5" ) );
        
        context = new TestFunctionContext( list( (Object) "1", 2, new BigDecimal( "3.5" ) ) );
        testForExpectedValue( context, "${ Sum( Collection ) }", new BigDecimal( "6.5" ) );
    }
    
    @Test
    
    public void testWithSet()
    {
        FunctionContext context;
        
        context = new TestFunctionContext( set() );
        testForExpectedValue( context, "${ Sum( Collection ) }", new BigDecimal( "0" ) );

        context = new TestFunctionContext( set( "1" ) );
        testForExpectedValue( context, "${ Sum( Collection ) }", new BigDecimal( "1" ) );

        context = new TestFunctionContext( set( "1", "2", "3.5" ) );
        testForExpectedValue( context, "${ Sum( Collection ) }", new BigDecimal( "6.5" ) );
        
        context = new TestFunctionContext( set( (Object) "1", 2, new BigDecimal( "3.5" ) ) );
        testForExpectedValue( context, "${ Sum( Collection ) }", new BigDecimal( "6.5" ) );
    }
    
    @Test

    public void testWithModelElementList()
    {
        final TestModelRoot root = TestModelRoot.TYPE.instantiate();
        final ElementList<TestModelElementA> list = root.getList1();
        final FunctionContext context = new ModelElementFunctionContext( root );
        
        TestModelElementA a;
        
        testForExpectedValue( context, "${ Sum( List1 ) }", new BigDecimal( "0" ) );
        testForExpectedValue( context, "${ Sum( List1, 'Value1' ) }", new BigDecimal( "0" ) );
        testForExpectedValue( context, "${ Sum( List1, 'Value2' ) }", new BigDecimal( "0" ) );
        
        a = list.insert();
        a.setValue1( "1" );
        a.setValue2( 2 );

        testForExpectedValue( context, "${ Sum( List1 ) }", new BigDecimal( "1" ) );
        testForExpectedValue( context, "${ Sum( List1, 'Value1' ) }", new BigDecimal( "1" ) );
        testForExpectedValue( context, "${ Sum( List1, 'Value2' ) }", new BigDecimal( "2" ) );

        a = list.insert();
        a.setValue1( "2" );
        a.setValue2( 3 );

        a = list.insert();
        a.setValue1( "3.5" );
        a.setValue2( 4 );
        
        testForExpectedValue( context, "${ Sum( List1 ) }", new BigDecimal( "6.5" ) );
        testForExpectedValue( context, "${ Sum( List1, 'Value1' ) }", new BigDecimal( "6.5" ) );
        testForExpectedValue( context, "${ Sum( List1, 'Value2' ) }", new BigDecimal( "9" ) );
        
        testForExpectedError( context, "${ Sum( List1, 'abc' ) }", "Property TestModelElementA.abc could not be found." );
        testForExpectedError( context, "${ Sum( List1, 'Element1' ) }", "Property TestModelElementA.Element1 is not a value property." );
        testForExpectedError( context, "${ Sum( List2 ) }", "Element type TestModelElementB does not contain a value property." );
        
        final FunctionResult result = ExpressionLanguageParser.parse( "${ Sum( List1, 'Value1' ) }" ).evaluate( context );

        assertEquals( new BigDecimal( "6.5" ), result.value() );
        
        list.get( 0 ).setValue1( "2" );
        list.get( 1 ).setValue1( "3.5" );
        
        assertEquals( new BigDecimal( "9.0" ), result.value() );
        
        a = list.insert();
        a.setValue1( "5.2" );
        
        assertEquals( new BigDecimal( "14.2" ), result.value() );
    }
    
    private static final class TestFunctionContext extends FunctionContext
    {
        private final Object collection;
        
        public TestFunctionContext( final Object collection )
        {
            this.collection = collection;
        }
        
        @Override
        public FunctionResult property( final Object element,
                                        final String name )
        {
            if( element == this && name.equalsIgnoreCase( "Collection" ) )
            {
                return Literal.create( this.collection ).evaluate( this );
            }
            
            return super.property( element, name );
        }
    };
    
}

