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

package org.eclipse.sapphire.tests.modeling.el.functions.endswith;

import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests EndsWith function.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class EndsWithFunctionTests extends TestExpr
{
    @Test
    
    public void testEndsWithFunction()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        final FunctionResult fr = ExpressionLanguageParser.parse( "${ Value.EndsWith( 'defg' ) }" ).evaluate( context );
        
        try
        {
            assertEquals( false, fr.value() );
            
            element.setValue( "a" );
            assertEquals( false, fr.value() );

            element.setValue( "abc" );
            assertEquals( false, fr.value() );

            element.setValue( "abcdefg" );
            assertEquals( true, fr.value() );

            element.setValue( "defg" );
            assertEquals( true, fr.value() );
        }
        finally
        {
            fr.dispose();
        }
    }

}
