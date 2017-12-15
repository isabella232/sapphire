/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.el.t0004;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;

/**
 * Tests conditional operator in circumstances when evaluating unused branch would result in errors.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestExpr0004

    extends TestExpr
    
{
    private TestExpr0004( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestExpr0004" );

        suite.addTest( new TestExpr0004( "test" ) );
        
        return suite;
    }
    
    public void test()
    {
        final String[] emptyArray = new String[] {};
        
        final FunctionContext context = new FunctionContext()
        {
            @Override
            public FunctionResult property( final Object element,
                                            final String name )
            {
                if( element == this && name.equalsIgnoreCase( "EmptyArray" ) )
                {
                    return Literal.create( emptyArray ).evaluate( this );
                }
                
                return super.property( element, name );
            }
        };
        
        testForExpectedValue( context, "${ EmptyArray.Size == 1 ? EmptyArray[ 0 ] : 'x' }", "x" );
    }
    
}

