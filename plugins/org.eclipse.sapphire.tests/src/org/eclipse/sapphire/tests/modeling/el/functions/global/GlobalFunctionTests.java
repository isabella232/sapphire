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

package org.eclipse.sapphire.tests.modeling.el.functions.global;

import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests Global function.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class GlobalFunctionTests extends TestExpr
{
    @Test
    
    public void GlobalFunction()
    {
        Sapphire.global().remove( "Test" );
        
        final FunctionResult fr = ExpressionLanguageParser.parse( "${ Global.Test }" ).evaluate( new FunctionContext() );
        
        try
        {
            assertNull( fr.value() );
            
            Sapphire.global().put( "Test", "a" );
            
            assertEquals( "a", fr.value() );
            
            Sapphire.global().put( "Test", "b" );
            
            assertEquals( "b", fr.value() );
            
            Sapphire.global().remove( "Test" );
            
            assertNull( fr.value() );
        }
        finally
        {
            fr.dispose();
        }
    }

}
