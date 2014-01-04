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

package org.eclipse.sapphire.tests.modeling.el;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class MiscTests extends SapphireTestCase
{
    private void test( final String expr,
                       final Object expected )
    {
        test( expr, expected, new FunctionContext() );
    }
    
    private void test( final String expr,
                       final Object expected,
                       final FunctionContext context )
    {
        final Object actual = ExpressionLanguageParser.parse( expr ).evaluate( context ).value();
        assertEquals( expected, actual );
    }

    @Test
    
    public void literalNull()
    {
        test( "${null}", null );
    }
    
    private void literalBoolean( final String literal )
    {
        test( "${" + literal + "}", Boolean.valueOf( literal ) );
    }
    
    @Test
    
    public void literalBoolean1()
    {
        literalBoolean( "true" );
    }
    
    @Test
    
    public void literalBoolean2()
    {
        literalBoolean( "false" );
    }
    
    private void literalInteger( final String literal )
    {
        test( "${" + literal + "}", new BigInteger( literal ) );
    }
    
    @Test
    
    public void literalInteger1()
    {
        literalInteger( "1234567" );
    }
    
    @Test
    
    public void literalInteger2()
    {
        literalInteger( "1234567890123456789012345678901234567890" );
    }

    private void literalDecimal( final String literal )
    {
        test( "${" + literal + "}", new BigDecimal( literal ) );
    }
    
    @Test

    public void literalDecimal1()
    {
        literalDecimal( "12.34567" );
    }
    
    @Test
    
    public void literalDecimal2()
    {
        literalDecimal( ".34567" );
    }
    
    @Test
    
    public void literalDecimal3()
    {
        literalDecimal( "12e23" );
    }
    
    @Test
    
    public void literalDecimal4()
    {
        literalDecimal( "12e+23" );
    }
    
    @Test

    public void literalDecimal5()
    {
        literalDecimal( "12e-23" );
    }
    
    @Test
    
    public void literalDecimal6()
    {
        literalDecimal( "12.345e23" );
    }
    
    @Test
    
    public void literalDecimal7()
    {
        literalDecimal( "12.345e+23" );
    }
    
    @Test

    public void literalDecimal8()
    {
        literalDecimal( "12.345e-23" );
    }
    
    @Test
    
    public void literalDecimal9()
    {
        literalDecimal( "12E23" );
    }
    
    @Test
    
    public void literalDecimal10()
    {
        literalDecimal( "12E+23" );
    }
    
    @Test

    public void literalDecimal11()
    {
        literalDecimal( "12E-23" );
    }
    
    @Test
    
    public void literalDecimal12()
    {
        literalDecimal( "12.345E23" );
    }
    
    @Test
    
    public void literalDecimal13()
    {
        literalDecimal( "12.345E+23" );
    }
    
    @Test

    public void literalDecimal14()
    {
        literalDecimal( "12.345E-23" );
    }
    
    @Test

    public void literalString1()
    {
        test( "${\"abcdefg 12345 xyz%%\"}", "abcdefg 12345 xyz%%" );
    }
    
    @Test

    public void literalString2()
    {
        test( "${'abcdefg 12345 xyz%%'}", "abcdefg 12345 xyz%%" );
    }
    
    @Test

    public void literalString3()
    {
        test( "${\"abcdefg \\\"12345\\\" xyz%%\"}", "abcdefg \"12345\" xyz%%" );
    }
    
    @Test

    public void literalString4()
    {
        test( "${\"abcdefg '12345' xyz%%\"}", "abcdefg '12345' xyz%%" );
    }
    
    @Test

    public void literalString5()
    {
        test( "${'abcdefg \"12345\" xyz%%'}", "abcdefg \"12345\" xyz%%" );
    }
    
    @Test

    public void literalString6()
    {
        test( "${'abcdefg \\'12345\\' xyz%%'}", "abcdefg '12345' xyz%%" );
    }
    
    @Test
    
    public void precedence1()
    {
        test( "${3+4*5}", new BigInteger( "23" ) );
    }
    
    @Test

    public void precedence2()
    {
        test( "${(3+4)*5}", new BigInteger( "35" ) );
    }
    
    @Test
    
    public void composite1()
    {
        test( "abc", "abc" );
    }
    
    @Test
    
    public void composite2()
    {
        test( "abc${'def'}", "abcdef" );
    }
    
    @Test
    
    public void composite3()
    {
        test( "abc${'def'}ghi", "abcdefghi" );
    }
    
    @Test
    
    public void composite4()
    {
        test( "abc${'def'}ghi${'jkl'}", "abcdefghijkl" );
    }
    
    @Test
    
    public void composite5()
    {
        test( "abc${5}ghi${6}", "abc5ghi6" );
    }
    
    @Test

    public void composite6()
    {
        test( "abc${3*5}ghi${3>5?6.2:7.3}", "abc15ghi7.3" );
    }
    
    @Test

    public void functions1()
    {
        test( "${ test:factorial( 7 ) }", new BigInteger( "5040" ) );
    }
    
    @Test
    
    public void functions2()
    {
        test( "${ test:factorial( 15 + 5 ) }", new BigInteger( "2432902008176640000" ) );
    }
    
    @Test

    public void concat1()
    {
        test( "${concat('a','b')}", "ab" );
    }
    
    @Test

    public void concat2()
    {
        test( "${concat(2010,'-',12,'-',2)}", "2010-12-2" );
    }
    
    @Test

    public void firstSegment1()
    {
        test( "${FirstSegment('abc.def.ghi','.')}", "abc" );
    }
    
    @Test

    public void firstSegment2()
    {
        test( "${FirstSegment('abc/def\\\\ghi','\\\\/')}", "abc" );
    }
    
    @Test

    public void firstSegment3()
    {
        test( "${FirstSegment('abc','.')}", "abc" );
    }
    
    @Test
    
    public void firstSegment4()
    {
        test( "${FirstSegment(null,'.')}", "" );
    }
    
    @Test
    
    public void lastSegment1()
    {
        test( "${LastSegment('abc.def.ghi','.')}", "ghi" );
    }
    
    @Test

    public void lastSegment2()
    {
        test( "${LastSegment('abc/def\\\\ghi','\\\\/')}", "ghi" );
    }
    
    @Test

    public void lastSegment3()
    {
        test( "${LastSegment('abc','.')}", "abc" );
    }
    
    @Test
    
    public void lastSegment4()
    {
        test( "${LastSegment(null,'.')}", "" );
    }

    private void properties( final String expr,
                             final Object expected )
    {
        final TestElement root = TestElement.TYPE.instantiate();
        final TestElement child = root.getFooBar().content( true );
        final TestElement grandchild = child.getFooBar().content( true );
        
        root.setIntegerProp( 1 );
        root.setStringProp( "ABC" );
        child.setIntegerProp( 2 );
        child.setStringProp( "DEF" );
        grandchild.setIntegerProp( 3 );
        grandchild.setStringProp( "GHI" );
        
        final FunctionContext context = new ModelElementFunctionContext( root );
        
        test( expr, expected, context );
    }
    
    @Test
    
    public void properties1()
    {
        properties( "${ IntegerProp + FooBar.IntegerProp + FooBar.FooBar[ 'IntegerProp' ] }", 6L );
    }
    
    @Test
    
    public void properties2()
    {
        properties( "${ StringProp }${ FooBar.StringProp }${ FooBar.FooBar[ 'StringProp' ] }", "ABCDEFGHI" );
    }

}

