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

package org.eclipse.sapphire.tests.modeling.el.functions.tail;

import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests Tail function.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TailFunctionTests extends TestExpr
{
    @Test
    
    public void testTailFunction()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        FunctionResult fr = ExpressionLanguageParser.parse( "${ Value.Tail( 3 ) }" ).evaluate( context );
        
        try
        {
            assertEquals( "", fr.value() );
            
            element.setValue( "ab" );
            assertEquals( "ab", fr.value() );

            element.setValue( "abcdefg" );
            assertEquals( "efg", fr.value() );
        }
        finally
        {
            fr.dispose();
        }
        
        fr = ExpressionLanguageParser.parse( "${ Value.Tail( 0 ) }" ).evaluate( context );
        
        try
        {
            element.setValue( null );
            assertEquals( "", fr.value() );
            
            element.setValue( "ab" );
            assertEquals( "", fr.value() );

            element.setValue( "abcdefg" );
            assertEquals( "", fr.value() );
        }
        finally
        {
            fr.dispose();
        }
        
        fr = ExpressionLanguageParser.parse( "${ Value.Tail( -3 ) }" ).evaluate( context );
        
        try
        {
            element.setValue( null );
            assertEquals( "", fr.value() );
            
            element.setValue( "ab" );
            assertEquals( "", fr.value() );

            element.setValue( "abcdefg" );
            assertEquals( "", fr.value() );
        }
        finally
        {
            fr.dispose();
        }
    }

}
