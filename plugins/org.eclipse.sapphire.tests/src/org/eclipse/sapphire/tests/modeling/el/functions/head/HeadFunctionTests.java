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

package org.eclipse.sapphire.tests.modeling.el.functions.head;

import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests Head function.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class HeadFunctionTests extends TestExpr
{
    @Test
    
    public void testHeadFunction()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        FunctionResult fr = ExpressionLanguageParser.parse( "${ Value.Head( 3 ) }" ).evaluate( context );
        
        try
        {
            assertEquals( "", fr.value() );
            
            element.setValue( "ab" );
            assertEquals( "ab", fr.value() );

            element.setValue( "abcdefg" );
            assertEquals( "abc", fr.value() );
        }
        finally
        {
            fr.dispose();
        }
        
        fr = ExpressionLanguageParser.parse( "${ Value.Head( 0 ) }" ).evaluate( context );
        
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
        
        fr = ExpressionLanguageParser.parse( "${ Value.Head( -3 ) }" ).evaluate( context );
        
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
