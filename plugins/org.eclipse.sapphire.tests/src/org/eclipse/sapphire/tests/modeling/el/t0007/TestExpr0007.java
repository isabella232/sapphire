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

package org.eclipse.sapphire.tests.modeling.el.t0007;

import java.util.List;

import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests casting between collections and strings.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestExpr0007 extends TestExpr
{
    @Test
    
    public void testListToString()
    {
        final FunctionContext context = new FunctionContext();
        
        testForExpectedValue( context, FailSafeFunction.create( Literal.create( list() ), Literal.create( String.class ) ), "" );
        testForExpectedValue( context, FailSafeFunction.create( Literal.create( list( "x" ) ), Literal.create( String.class ) ), "x" );
        testForExpectedValue( context, FailSafeFunction.create( Literal.create( list( "x", "y" ) ), Literal.create( String.class ) ), "x,y" );
        testForExpectedValue( context, FailSafeFunction.create( Literal.create( list( "x", "y", "z" ) ), Literal.create( String.class ) ), "x,y,z" );
    }
    
    @Test

    public void testSetToString()
    {
        final FunctionContext context = new FunctionContext();
        
        testForExpectedValue( context, FailSafeFunction.create( Literal.create( set() ), Literal.create( String.class ) ), "" );
        testForExpectedValue( context, FailSafeFunction.create( Literal.create( set( "x" ) ), Literal.create( String.class ) ), "x" );
        testForExpectedValue( context, FailSafeFunction.create( Literal.create( set( "x", "y" ) ), Literal.create( String.class ) ), "x,y" );
        testForExpectedValue( context, FailSafeFunction.create( Literal.create( set( "x", "y", "z" ) ), Literal.create( String.class ) ), "x,y,z" );
    }
    
    @Test

    public void testArrayToString()
    {
        final FunctionContext context = new FunctionContext();
        
        testForExpectedValue( context, FailSafeFunction.create( Literal.create( new String[] {} ), Literal.create( String.class ) ), "" );
        testForExpectedValue( context, FailSafeFunction.create( Literal.create( new String[] { "x" } ), Literal.create( String.class ) ), "x" );
        testForExpectedValue( context, FailSafeFunction.create( Literal.create( new String[] { "x", "y" } ), Literal.create( String.class ) ), "x,y" );
        testForExpectedValue( context, FailSafeFunction.create( Literal.create( new String[] { "x", "y", "z" } ), Literal.create( String.class ) ), "x,y,z" );
    }
    
    @Test

    public void testStringToList()
    {
        final FunctionContext context = new FunctionContext();
        
        testForExpectedValue( context, FailSafeFunction.create( Literal.create( "" ), Literal.create( List.class ) ), list() );
        testForExpectedValue( context, FailSafeFunction.create( Literal.create( "x" ), Literal.create( List.class ) ), list( "x" ) );
        testForExpectedValue( context, FailSafeFunction.create( Literal.create( "x,y" ), Literal.create( List.class ) ), list( "x", "y" ) );
        testForExpectedValue( context, FailSafeFunction.create( Literal.create( "x,y,z" ), Literal.create( List.class ) ), list( "x", "y", "z" ) );
    }
    
}

