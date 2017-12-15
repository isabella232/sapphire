/******************************************************************************
 * Copyright (c) 2012 Oracle
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
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.tests.modeling.el.t0001.TestExpr0001;
import org.eclipse.sapphire.tests.modeling.el.t0002.TestExpr0002;
import org.eclipse.sapphire.tests.modeling.el.t0003.TestExpr0003;
import org.eclipse.sapphire.tests.modeling.el.t0004.TestExpr0004;
import org.eclipse.sapphire.tests.modeling.el.t0005.TestExpr0005;
import org.eclipse.sapphire.tests.modeling.el.t0006.TestExpr0006;
import org.eclipse.sapphire.tests.modeling.el.t0007.TestExpr0007;
import org.eclipse.sapphire.tests.modeling.el.t0008.TestExpr0008;
import org.eclipse.sapphire.tests.modeling.el.t0009.TestExpr0009;
import org.eclipse.sapphire.tests.modeling.el.t0010.TestExpr0010;
import org.eclipse.sapphire.tests.modeling.el.t0011.TestExpr0011;
import org.eclipse.sapphire.tests.modeling.el.t0012.TestExpr0012;
import org.eclipse.sapphire.tests.modeling.el.t0013.TestExpr0013;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ExpressionLanguageTests

    extends TestCase
    
{
    private ExpressionLanguageTests( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "ExpressionLanguageTests" );
        
        suite.addTest( new ExpressionLanguageTests( "literalNull" ) );

        for( int i = 1; i <= 2; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "literalBoolean" + String.valueOf( i ) ) );
        }
        
        for( int i = 1; i <= 2; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "literalInteger" + String.valueOf( i ) ) );
        }
        
        for( int i = 1; i <= 14; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "literalDecimal" + String.valueOf( i ) ) );
        }
        
        for( int i = 1; i <= 6; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "literalString" + String.valueOf( i ) ) );
        }
        
        for( int i = 1; i <= 3; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "opAddition" + String.valueOf( i ) ) );
        }
        
        for( int i = 1; i <= 3; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "opSubtraction" + String.valueOf( i ) ) );
        }
        
        for( int i = 1; i <= 4; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "opMultiplication" + String.valueOf( i ) ) );
        }
        
        for( int i = 1; i <= 6; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "opDivision" + String.valueOf( i ) ) );
        }
        
        for( int i = 1; i <= 2; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "opUnaryMinus" + String.valueOf( i ) ) );
        }
        
        for( int i = 1; i <= 12; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "opLessThan" + String.valueOf( i ) ) );
        }
        
        for( int i = 1; i <= 12; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "opLessThanOrEq" + String.valueOf( i ) ) );
        }
        
        for( int i = 1; i <= 12; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "opGreaterThan" + String.valueOf( i ) ) );
        }
        
        for( int i = 1; i <= 12; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "opGreaterThanOrEq" + String.valueOf( i ) ) );
        }
        
        for( int i = 1; i <= 12; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "opEqual" + String.valueOf( i ) ) );
        }
        
        for( int i = 1; i <= 12; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "opNotEqual" + String.valueOf( i ) ) );
        }
        
        for( int i = 1; i <= 8; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "opAnd" + String.valueOf( i ) ) );
        }

        for( int i = 1; i <= 8; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "opOr" + String.valueOf( i ) ) );
        }

        for( int i = 1; i <= 4; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "opNot" + String.valueOf( i ) ) );
        }

        for( int i = 1; i <= 4; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "opEmpty" + String.valueOf( i ) ) );
        }

        for( int i = 1; i <= 2; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "opConditional" + String.valueOf( i ) ) );
        }

        for( int i = 1; i <= 2; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "precedence" + String.valueOf( i ) ) );
        }
        
        for( int i = 1; i <= 6; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "composite" + String.valueOf( i ) ) );
        }
        
        for( int i = 1; i <= 3; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "functions" + String.valueOf( i ) ) );
        }
        
        for( int i = 1; i <= 2; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "concat" + String.valueOf( i ) ) );
        }
        
        for( int i = 1; i <= 4; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "firstSegment" + String.valueOf( i ) ) );
        }
        
        for( int i = 1; i <= 4; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "lastSegment" + String.valueOf( i ) ) );
        }
        
        for( int i = 1; i <= 2; i++ )
        {
            suite.addTest( new ExpressionLanguageTests( "properties" + String.valueOf( i ) ) );
        }
        
        suite.addTest( TestExpr0001.suite() );
        suite.addTest( TestExpr0002.suite() );
        suite.addTest( TestExpr0003.suite() );
        suite.addTest( TestExpr0004.suite() );
        suite.addTest( TestExpr0005.suite() );
        suite.addTest( TestExpr0006.suite() );
        suite.addTest( TestExpr0007.suite() );
        suite.addTest( TestExpr0008.suite() );
        suite.addTest( TestExpr0009.suite() );
        suite.addTest( TestExpr0010.suite() );
        suite.addTest( TestExpr0011.suite() );
        suite.addTest( TestExpr0012.suite() );
        suite.addTest( TestExpr0013.suite() );
        
        return suite;
    }
    
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

    public void literalNull()
    {
        test( "${null}", null );
    }
    
    private void literalBoolean( final String literal )
    {
        test( "${" + literal + "}", Boolean.valueOf( literal ) );
    }
    
    public void literalBoolean1()
    {
        literalBoolean( "true" );
    }
    
    public void literalBoolean2()
    {
        literalBoolean( "false" );
    }
    
    private void literalInteger( final String literal )
    {
        test( "${" + literal + "}", new BigInteger( literal ) );
    }
    
    public void literalInteger1()
    {
        literalInteger( "1234567" );
    }
    
    public void literalInteger2()
    {
        literalInteger( "1234567890123456789012345678901234567890" );
    }

    private void literalDecimal( final String literal )
    {
        test( "${" + literal + "}", new BigDecimal( literal ) );
    }

    public void literalDecimal1()
    {
        literalDecimal( "12.34567" );
    }
    
    public void literalDecimal2()
    {
        literalDecimal( ".34567" );
    }
    
    public void literalDecimal3()
    {
        literalDecimal( "12e23" );
    }
    
    public void literalDecimal4()
    {
        literalDecimal( "12e+23" );
    }

    public void literalDecimal5()
    {
        literalDecimal( "12e-23" );
    }
    
    public void literalDecimal6()
    {
        literalDecimal( "12.345e23" );
    }
    
    public void literalDecimal7()
    {
        literalDecimal( "12.345e+23" );
    }

    public void literalDecimal8()
    {
        literalDecimal( "12.345e-23" );
    }
    
    public void literalDecimal9()
    {
        literalDecimal( "12E23" );
    }
    
    public void literalDecimal10()
    {
        literalDecimal( "12E+23" );
    }

    public void literalDecimal11()
    {
        literalDecimal( "12E-23" );
    }
    
    public void literalDecimal12()
    {
        literalDecimal( "12.345E23" );
    }
    
    public void literalDecimal13()
    {
        literalDecimal( "12.345E+23" );
    }

    public void literalDecimal14()
    {
        literalDecimal( "12.345E-23" );
    }

    public void literalString1()
    {
        test( "${\"abcdefg 12345 xyz%%\"}", "abcdefg 12345 xyz%%" );
    }

    public void literalString2()
    {
        test( "${'abcdefg 12345 xyz%%'}", "abcdefg 12345 xyz%%" );
    }

    public void literalString3()
    {
        test( "${\"abcdefg \\\"12345\\\" xyz%%\"}", "abcdefg \"12345\" xyz%%" );
    }

    public void literalString4()
    {
        test( "${\"abcdefg '12345' xyz%%\"}", "abcdefg '12345' xyz%%" );
    }

    public void literalString5()
    {
        test( "${'abcdefg \"12345\" xyz%%'}", "abcdefg \"12345\" xyz%%" );
    }

    public void literalString6()
    {
        test( "${'abcdefg \\'12345\\' xyz%%'}", "abcdefg '12345' xyz%%" );
    }
    
    public void opAddition1()
    {
        test( "${3+5}", new BigInteger( "8" ) );
    }
    
    public void opAddition2()
    {
        test( "${3+5+7}", new BigInteger( "15" ) );
    }
    
    public void opAddition3()
    {
        test( "${3.2+5}", new BigDecimal( "8.2" ) );
    }
    
    public void opSubtraction1()
    {
        test( "${3-5}", new BigInteger( "-2" ) );
    }
    
    public void opSubtraction2()
    {
        test( "${3-5-7}", new BigInteger( "-9" ) );
    }
    
    public void opSubtraction3()
    {
        test( "${3.2-5}", new BigDecimal( "-1.8" ) );
    }

    public void opMultiplication1()
    {
        test( "${3*5}", new BigInteger( "15" ) );
    }
    
    public void opMultiplication2()
    {
        test( "${3*5*7}", new BigInteger( "105" ) );
    }
    
    public void opMultiplication3()
    {
        test( "${3.2*5}", new BigDecimal( "16.0" ) );
    }

    public void opMultiplication4()
    {
        test( "${3.2*6}", new BigDecimal( "19.2" ) );
    }

    public void opDivision1()
    {
        test( "${32/5}", new BigDecimal( "6" ) );
    }
    
    public void opDivision2()
    {
        test( "${322/5/7}", new BigDecimal( "9" ) );
    }
    
    public void opDivision3()
    {
        test( "${32.2/5}", new BigDecimal( "6.4" ) );
    }
    
    public void opDivision4()
    {
        test( "${32 div 5}", new BigDecimal( "6" ) );
    }
    
    public void opDivision5()
    {
        test( "${322 div 5 div 7}", new BigDecimal( "9" ) );
    }
    
    public void opDivision6()
    {
        test( "${32.2 div 5}", new BigDecimal( "6.4" ) );
    }
    
    public void opUnaryMinus1()
    {
        test( "${-5}", new BigInteger( "-5" ) );
    }

    public void opUnaryMinus2()
    {
        test( "${-5.23}", new BigDecimal( "-5.23" ) );
    }
    
    public void opLessThan1()
    {
        test( "${3<5}", true );
    }
    
    public void opLessThan2()
    {
        test( "${5<3}", false );
    }
    
    public void opLessThan3()
    {
        test( "${3<3}", false );
    }
    
    public void opLessThan4()
    {
        test( "${3.2<5}", true );
    }
    
    public void opLessThan5()
    {
        test( "${5.3<3}", false );
    }
    
    public void opLessThan6()
    {
        test( "${3.2<3.2}", false );
    }

    public void opLessThan7()
    {
        test( "${3 lt 5}", true );
    }
    
    public void opLessThan8()
    {
        test( "${5 lt 3}", false );
    }
    
    public void opLessThan9()
    {
        test( "${3 lt 3}", false );
    }
    
    public void opLessThan10()
    {
        test( "${3.2 lt 5}", true );
    }
    
    public void opLessThan11()
    {
        test( "${5.3 lt 3}", false );
    }
    
    public void opLessThan12()
    {
        test( "${3.2 lt 3.2}", false );
    }
    
    public void opLessThanOrEq1()
    {
        test( "${3<=5}", true );
    }
    
    public void opLessThanOrEq2()
    {
        test( "${5<=3}", false );
    }
    
    public void opLessThanOrEq3()
    {
        test( "${3<=3}", true );
    }
    
    public void opLessThanOrEq4()
    {
        test( "${3.2<=5}", true );
    }
    
    public void opLessThanOrEq5()
    {
        test( "${5.3<=3}", false );
    }
    
    public void opLessThanOrEq6()
    {
        test( "${3.2<=3.2}", true );
    }

    public void opLessThanOrEq7()
    {
        test( "${3 le 5}", true );
    }
    
    public void opLessThanOrEq8()
    {
        test( "${5 le 3}", false );
    }
    
    public void opLessThanOrEq9()
    {
        test( "${3 le 3}", true );
    }
    
    public void opLessThanOrEq10()
    {
        test( "${3.2 le 5}", true );
    }
    
    public void opLessThanOrEq11()
    {
        test( "${5.3 le 3}", false );
    }
    
    public void opLessThanOrEq12()
    {
        test( "${3.2 le 3.2}", true );
    }

    public void opGreaterThan1()
    {
        test( "${3>5}", false );
    }
    
    public void opGreaterThan2()
    {
        test( "${5>3}", true );
    }
    
    public void opGreaterThan3()
    {
        test( "${3>3}", false );
    }
    
    public void opGreaterThan4()
    {
        test( "${3.2>5}", false );
    }
    
    public void opGreaterThan5()
    {
        test( "${5.3>3}", true );
    }
    
    public void opGreaterThan6()
    {
        test( "${3.2>3.2}", false );
    }

    public void opGreaterThan7()
    {
        test( "${3 gt 5}", false );
    }
    
    public void opGreaterThan8()
    {
        test( "${5 gt 3}", true );
    }
    
    public void opGreaterThan9()
    {
        test( "${3 gt 3}", false );
    }
    
    public void opGreaterThan10()
    {
        test( "${3.2 gt 5}", false );
    }
    
    public void opGreaterThan11()
    {
        test( "${5.3 gt 3}", true );
    }
    
    public void opGreaterThan12()
    {
        test( "${3.2 gt 3.2}", false );
    }
    
    public void opGreaterThanOrEq1()
    {
        test( "${3>=5}", false );
    }
    
    public void opGreaterThanOrEq2()
    {
        test( "${5>=3}", true );
    }
    
    public void opGreaterThanOrEq3()
    {
        test( "${3>=3}", true );
    }
    
    public void opGreaterThanOrEq4()
    {
        test( "${3.2>=5}", false );
    }
    
    public void opGreaterThanOrEq5()
    {
        test( "${5.3>=3}", true );
    }
    
    public void opGreaterThanOrEq6()
    {
        test( "${3.2>=3.2}", true );
    }

    public void opGreaterThanOrEq7()
    {
        test( "${3 ge 5}", false );
    }
    
    public void opGreaterThanOrEq8()
    {
        test( "${5 ge 3}", true );
    }
    
    public void opGreaterThanOrEq9()
    {
        test( "${3 ge 3}", true );
    }
    
    public void opGreaterThanOrEq10()
    {
        test( "${3.2 ge 5}", false );
    }
    
    public void opGreaterThanOrEq11()
    {
        test( "${5.3 ge 3}", true );
    }
    
    public void opGreaterThanOrEq12()
    {
        test( "${3.2 ge 3.2}", true );
    }
    
    public void opEqual1()
    {
        test( "${3==3}", true );
    }

    public void opEqual2()
    {
        test( "${3==5}", false );
    }

    public void opEqual3()
    {
        test( "${3.2==3.2}", true );
    }

    public void opEqual4()
    {
        test( "${3.2==5}", false );
    }
    
    public void opEqual5()
    {
        test( "${'abc'=='abc'}", true );
    }

    public void opEqual6()
    {
        test( "${'abc'=='xyz'}", false );
    }
    
    public void opEqual7()
    {
        test( "${3 eq 3}", true );
    }

    public void opEqual8()
    {
        test( "${3 eq 5}", false );
    }

    public void opEqual9()
    {
        test( "${3.2 eq 3.2}", true );
    }

    public void opEqual10()
    {
        test( "${3.2 eq 5}", false );
    }
    
    public void opEqual11()
    {
        test( "${'abc' eq 'abc'}", true );
    }

    public void opEqual12()
    {
        test( "${'abc' eq 'xyz'}", false );
    }
    
    public void opNotEqual1()
    {
        test( "${3!=3}", false );
    }

    public void opNotEqual2()
    {
        test( "${3!=5}", true );
    }

    public void opNotEqual3()
    {
        test( "${3.2!=3.2}", false );
    }

    public void opNotEqual4()
    {
        test( "${3.2!=5}", true );
    }
    
    public void opNotEqual5()
    {
        test( "${'abc'!='abc'}", false );
    }

    public void opNotEqual6()
    {
        test( "${'abc'!='xyz'}", true );
    }
    
    public void opNotEqual7()
    {
        test( "${3 ne 3}", false );
    }

    public void opNotEqual8()
    {
        test( "${3 ne 5}", true );
    }

    public void opNotEqual9()
    {
        test( "${3.2 ne 3.2}", false );
    }

    public void opNotEqual10()
    {
        test( "${3.2 ne 5}", true );
    }
    
    public void opNotEqual11()
    {
        test( "${'abc' ne 'abc'}", false );
    }

    public void opNotEqual12()
    {
        test( "${'abc' ne 'xyz'}", true );
    }
    
    public void opAnd1()
    {
        test( "${true&&true}", true );
    }
    
    public void opAnd2()
    {
        test( "${true&&false}", false );
    }
    
    public void opAnd3()
    {
        test( "${false&&true}", false );
    }
    
    public void opAnd4()
    {
        test( "${false&&false}", false );
    }
    
    public void opAnd5()
    {
        test( "${true and true}", true );
    }
    
    public void opAnd6()
    {
        test( "${true and false}", false );
    }
    
    public void opAnd7()
    {
        test( "${false and true}", false );
    }
    
    public void opAnd8()
    {
        test( "${false and false}", false );
    }
    
    public void opOr1()
    {
        test( "${true||true}", true );
    }
    
    public void opOr2()
    {
        test( "${true||false}", true );
    }
    
    public void opOr3()
    {
        test( "${false||true}", true );
    }
    
    public void opOr4()
    {
        test( "${false||false}", false );
    }
    
    public void opOr5()
    {
        test( "${true or true}", true );
    }
    
    public void opOr6()
    {
        test( "${true or false}", true );
    }
    
    public void opOr7()
    {
        test( "${false or true}", true );
    }
    
    public void opOr8()
    {
        test( "${false or false}", false );
    }
    
    public void opNot1()
    {
        test( "${!true}", false );
    }
    
    public void opNot2()
    {
        test( "${!false}", true );
    }
    
    public void opNot3()
    {
        test( "${not true}", false );
    }
    
    public void opNot4()
    {
        test( "${not false}", true );
    }
    
    public void opEmpty1()
    {
        test( "${empty null}", true );
    }

    public void opEmpty2()
    {
        test( "${empty 5}", false );
    }
    
    public void opEmpty3()
    {
        test( "${empty 'abc'}", false );
    }
    
    public void opEmpty4()
    {
        test( "${empty ''}", true );
    }
    
    public void opConditional1()
    {
        test( "${true?3:5}", new BigInteger( "3" ) );
    }
    
    public void opConditional2()
    {
        test( "${false?3:5}", new BigInteger( "5" ) );
    }
    
    public void precedence1()
    {
        test( "${3+4*5}", new BigInteger( "23" ) );
    }

    public void precedence2()
    {
        test( "${(3+4)*5}", new BigInteger( "35" ) );
    }
    
    public void composite1()
    {
        test( "abc", "abc" );
    }
    
    public void composite2()
    {
        test( "abc${'def'}", "abcdef" );
    }
    
    public void composite3()
    {
        test( "abc${'def'}ghi", "abcdefghi" );
    }
    
    public void composite4()
    {
        test( "abc${'def'}ghi${'jkl'}", "abcdefghijkl" );
    }
    
    public void composite5()
    {
        test( "abc${5}ghi${6}", "abc5ghi6" );
    }

    public void composite6()
    {
        test( "abc${3*5}ghi${3>5?6.2:7.3}", "abc15ghi7.3" );
    }

    public void functions1()
    {
        final FunctionContext context = new FunctionContext()
        {
            @Override
            public Function function( final String name,
                                      final List<Function> operands )
            {
                Function function = null;
                
                if( name.equals( "eclipse:add" ) )
                {
                    function = new Function()
                    {
                        @Override
                        public String name()
                        {
                            return "eclipse:add";
                        }

                        @Override
                        public FunctionResult evaluate( final FunctionContext context )
                        {
                            return new FunctionResult( this, context )
                            {
                                @Override
                                protected Object evaluate()
                                {
                                    return cast( operand( 0 ).value(), BigInteger.class ).add( cast( operand( 1 ).value(), BigInteger.class ) );
                                }
                            };
                        }
                    };
                }
                else if( name.equals( "subtract" ) )
                {
                    function = new Function()
                    {
                        @Override
                        public String name()
                        {
                            return "subtract";
                        }

                        @Override
                        public FunctionResult evaluate( final FunctionContext context )
                        {
                            return new FunctionResult( this, context )
                            {
                                @Override
                                protected Object evaluate()
                                {
                                    return cast( operand( 0 ).value(), BigInteger.class ).subtract( cast( operand( 1 ).value(), BigInteger.class ) );
                                }
                            };
                        }
                    };
                }
                
                if( function != null )
                {
                    function.init( operands.toArray( new Function[ operands.size() ] ) );
                    return function;
                }
                
                return super.function( name, operands );
            }
        };
        
        test( "${ 3 + eclipse:add( 4, 5 ) + subtract( 6, 7 ) + subtract( eclipse:add( 8 + 9, 10 + 11 ), 12 + 13 ) }", new BigInteger( "24" ), context ); 
    }
    
    public void functions2()
    {
        test( "${ test:factorial( 7 ) }", new BigInteger( "5040" ) );
    }
    
    public void functions3()
    {
        test( "${ test:factorial( 15 + 5 ) }", new BigInteger( "2432902008176640000" ) );
    }

    public void concat1()
    {
        test( "${concat('a','b')}", "ab" );
    }

    public void concat2()
    {
        test( "${concat(2010,'-',12,'-',2)}", "2010-12-2" );
    }

    public void firstSegment1()
    {
        test( "${FirstSegment('abc.def.ghi','.')}", "abc" );
    }

    public void firstSegment2()
    {
        test( "${FirstSegment('abc/def\\\\ghi','\\\\/')}", "abc" );
    }

    public void firstSegment3()
    {
        test( "${FirstSegment('abc','.')}", "abc" );
    }
    
    public void firstSegment4()
    {
        test( "${FirstSegment(null,'.')}", "" );
    }
    
    public void lastSegment1()
    {
        test( "${LastSegment('abc.def.ghi','.')}", "ghi" );
    }

    public void lastSegment2()
    {
        test( "${LastSegment('abc/def\\\\ghi','\\\\/')}", "ghi" );
    }

    public void lastSegment3()
    {
        test( "${LastSegment('abc','.')}", "abc" );
    }
    
    public void lastSegment4()
    {
        test( "${LastSegment(null,'.')}", "" );
    }

    private void properties( final String expr,
                             final Object expected )
    {
        final TestElement root = TestElement.TYPE.instantiate();
        final TestElement child = root.getFooBar().element( true );
        final TestElement grandchild = child.getFooBar().element( true );
        
        root.setIntegerProp( 1 );
        root.setStringProp( "ABC" );
        child.setIntegerProp( 2 );
        child.setStringProp( "DEF" );
        grandchild.setIntegerProp( 3 );
        grandchild.setStringProp( "GHI" );
        
        final FunctionContext context = new ModelElementFunctionContext( root );
        
        test( expr, expected, context );
    }
    
    public void properties1()
    {
        properties( "${ IntegerProp + FooBar.IntegerProp + FooBar.FooBar[ 'IntegerProp' ] }", 6L );
    }
    
    public void properties2()
    {
        properties( "${ StringProp }${ FooBar.StringProp }${ FooBar.FooBar[ 'StringProp' ] }", "ABCDEFGHI" );
    }

}

