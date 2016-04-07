/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.el.functions.index;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests Index function.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class IndexFunctionTests extends TestExpr
{
    @Test
    
    public void testIndexFunction()
    {
        try( TestElement element = TestElement.TYPE.instantiate() )
        {
            final Element entry = element.getList().insert();
            final FunctionContext context = new ModelElementFunctionContext( entry );
            
            try( FunctionResult fr = ExpressionLanguageParser.parse( "${ This.Index }" ).evaluate( context ) )
            {
                assertEquals( 0, fr.value() );
                
                element.getList().insert();
                assertEquals( 0, fr.value() );

                element.getList().insert( 0 );
                assertEquals( 1, fr.value() );
                
                element.getList().moveDown( entry );
                assertEquals( 2, fr.value() );
                
                element.getList().remove( 0 );
                assertEquals( 1, fr.value() );
                
                element.getList().remove( 0 );
                assertEquals( 0, fr.value() );
            }
        }
    }
    
    @Test

    public void testIndexFunctionOnRoot()
    {
        try( TestElement element = TestElement.TYPE.instantiate() )
        {
            final FunctionContext context = new ModelElementFunctionContext( element );
            
            try( FunctionResult fr = ExpressionLanguageParser.parse( "${ This.Index }" ).evaluate( context ) )
            {
                final Status st = fr.status();
                
                assertEquals( Status.Severity.ERROR, st.severity() );
                assertEquals( "Cannot determine index if parent is not a list.", st.message() );
            }
        }
    }
    
    @Test

    public void testIndexFunctionOnElementPropertyContent()
    {
        try( TestElement element = TestElement.TYPE.instantiate() )
        {
            final Element child = element.getElement().content( true );
            final FunctionContext context = new ModelElementFunctionContext( child );
            
            try( FunctionResult fr = ExpressionLanguageParser.parse( "${ This.Index }" ).evaluate( context ) )
            {
                final Status st = fr.status();
                
                assertEquals( Status.Severity.ERROR, st.severity() );
                assertEquals( "Cannot determine index if parent is not a list.", st.message() );
            }
        }
    }
    
}
