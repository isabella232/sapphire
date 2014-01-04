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

package org.eclipse.sapphire.tests.modeling.el.t0018;

import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests FailSafeFunction function's handling of malformed values. Of particular interest is ability to retrieve malformed text
 * when casting to a string and proper application of model-level and fail-safe defaults.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestExpr0018 extends TestExpr
{
    @Test
    
    public void testFailSafeWithBoolean()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        element.setBooleanValue( "abc" );
        element.setBooleanValueWithDefault( "abc" );
        
        assertEquals( "abc", element.getBooleanValue().text() );
        assertEquals( null, element.getBooleanValue().content() );

        assertFailSafeValueEquals( context, "${ BooleanValue }", String.class, Boolean.TRUE, "abc" );
        assertFailSafeValueEquals( context, "${ BooleanValue }", Boolean.class, Boolean.TRUE, Boolean.TRUE );
        assertFailSafeValueEquals( context, "${ BooleanValue }", Boolean.class, null, Boolean.FALSE );

        assertEquals( "abc", element.getBooleanValueWithDefault().text() );
        assertEquals( Boolean.TRUE, element.getBooleanValueWithDefault().content() );
        
        assertFailSafeValueEquals( context, "${ BooleanValueWithDefault }", String.class, Boolean.TRUE, "abc" );
        assertFailSafeValueEquals( context, "${ BooleanValueWithDefault }", Boolean.class, Boolean.TRUE, Boolean.TRUE );
        assertFailSafeValueEquals( context, "${ BooleanValueWithDefault }", Boolean.class, null, Boolean.TRUE );
    }
    
    @Test
    
    public void testFailSafeWithInteger()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        element.setIntegerValue( "abc" );
        element.setIntegerValueWithDefault( "abc" );
        
        assertEquals( "abc", element.getIntegerValue().text() );
        assertEquals( null, element.getIntegerValue().content() );

        assertFailSafeValueEquals( context, "${ IntegerValue }", String.class, 2, "abc" );
        assertFailSafeValueEquals( context, "${ IntegerValue }", Integer.class, 2, 2 );
        assertFailSafeValueEquals( context, "${ IntegerValue }", Integer.class, null, 0 );

        assertEquals( "abc", element.getIntegerValueWithDefault().text() );
        assertEquals( Integer.valueOf( 1 ), element.getIntegerValueWithDefault().content() );
        
        assertFailSafeValueEquals( context, "${ IntegerValueWithDefault }", String.class, 2, "abc" );
        assertFailSafeValueEquals( context, "${ IntegerValueWithDefault }", Integer.class, 2, 1 );
        assertFailSafeValueEquals( context, "${ IntegerValueWithDefault }", Integer.class, null, 1 );
    }
    
    @Test
    
    public void testFailSafeWithEnum()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        element.setEnumValue( "abc" );
        element.setEnumValueWithDefault( "abc" );
        
        assertEquals( "abc", element.getEnumValue().text() );
        assertEquals( null, element.getEnumValue().content() );

        assertFailSafeValueEquals( context, "${ EnumValue }", String.class, TestElement.EnumType.B, "abc" );
        assertFailSafeValueEquals( context, "${ EnumValue }", TestElement.EnumType.class, TestElement.EnumType.B, TestElement.EnumType.B );
        assertFailSafeValueEquals( context, "${ EnumValue }", TestElement.EnumType.class, null, null );

        assertEquals( "abc", element.getEnumValueWithDefault().text() );
        assertEquals( TestElement.EnumType.A, element.getEnumValueWithDefault().content() );
        
        assertFailSafeValueEquals( context, "${ EnumValueWithDefault }", String.class, TestElement.EnumType.B, "abc" );
        assertFailSafeValueEquals( context, "${ EnumValueWithDefault }", TestElement.EnumType.class, TestElement.EnumType.B, TestElement.EnumType.A );
        assertFailSafeValueEquals( context, "${ EnumValueWithDefault }", TestElement.EnumType.class, null, TestElement.EnumType.A );
    }
    
    protected static void assertFailSafeValueEquals( final FunctionContext context,
                                                     final String expr,
                                                     final Class<?> type,
                                                     final Object def,
                                                     final Object expected )
    {
        testForExpectedValue( context, FailSafeFunction.create( ExpressionLanguageParser.parse( expr ), Literal.create( type ), Literal.create( def ) ), expected );
    }

}

