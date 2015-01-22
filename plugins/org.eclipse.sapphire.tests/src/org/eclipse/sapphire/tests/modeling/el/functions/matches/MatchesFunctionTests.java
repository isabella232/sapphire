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

package org.eclipse.sapphire.tests.modeling.el.functions.matches;

import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests Matches function.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class MatchesFunctionTests extends TestExpr
{
    @Test
    
    public void testMatchesFunction()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        final FunctionResult fr = ExpressionLanguageParser.parse( "${ Value.Matches( '[a-z][a-z0-9]*' ) }" ).evaluate( context );
        
        try
        {
            assertEquals( false, fr.value() );
            
            element.setValue( "a" );
            assertEquals( true, fr.value() );

            element.setValue( "a1b" );
            assertEquals( true, fr.value() );

            element.setValue( "2" );
            assertEquals( false, fr.value() );

            element.setValue( "2a" );
            assertEquals( false, fr.value() );
        }
        finally
        {
            fr.dispose();
        }
    }

}
