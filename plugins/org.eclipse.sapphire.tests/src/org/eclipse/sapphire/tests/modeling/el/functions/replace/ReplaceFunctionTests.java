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

package org.eclipse.sapphire.tests.modeling.el.functions.replace;

import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests Replace function.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ReplaceFunctionTests extends TestExpr
{
    @Test
    
    public void testMatchesFunction()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        final FunctionResult fr = ExpressionLanguageParser.parse( "${ Value.Replace( '[0-9]', 'x' ) }" ).evaluate( context );
        
        try
        {
            assertEquals( "", fr.value() );
            
            element.setValue( "a" );
            assertEquals( "a", fr.value() );

            element.setValue( "a1b" );
            assertEquals( "axb", fr.value() );

            element.setValue( "2" );
            assertEquals( "x", fr.value() );

            element.setValue( "2as09df7s0d9f7sd0f" );
            assertEquals( "xasxxdfxsxdxfxsdxf", fr.value() );
        }
        finally
        {
            fr.dispose();
        }
    }

}
