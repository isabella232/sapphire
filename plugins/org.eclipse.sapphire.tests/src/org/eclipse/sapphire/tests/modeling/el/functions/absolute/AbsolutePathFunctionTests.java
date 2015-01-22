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

package org.eclipse.sapphire.tests.modeling.el.functions.absolute;

import java.io.File;

import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests Absolute function for paths.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class AbsolutePathFunctionTests extends TestExpr
{
    @Test
    
    public void testAbsolutePathFunction() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        final FunctionResult fr = ExpressionLanguageParser.parse( "${ Path.Absolute }" ).evaluate( context );
        
        try
        {
            assertNull( fr.value() );
            
            element.setPath( "abc/file.txt" );
            assertEquals( ( new Path( new File( "" ).getCanonicalPath() ) ).append( "abc/file.txt" ), fr.value() );
        }
        finally
        {
            fr.dispose();
        }
    }

}
