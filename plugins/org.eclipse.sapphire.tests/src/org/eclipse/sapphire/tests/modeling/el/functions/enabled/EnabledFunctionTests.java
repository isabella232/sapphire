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

package org.eclipse.sapphire.tests.modeling.el.functions.enabled;

import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests Enabled function.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class EnabledFunctionTests extends TestExpr
{
    @Test
    
    public void testEnabledFunctionOnValueProperty()
    {
        testEnabledFunctionOnProperty( TestElement.PROP_VALUE );
    }
    
    @Test
    
    public void testEnabledFunctionOnListProperty()
    {
        testEnabledFunctionOnProperty( TestElement.PROP_LIST );
    }
    
    @Test
    
    public void testEnabledFunctionOnElementProperty()
    {
        testEnabledFunctionOnProperty( TestElement.PROP_ELEMENT );
    }
    
    @Test
    
    public void testEnabledFunctionOnImpliedElementProperty()
    {
        testEnabledFunctionOnProperty( TestElement.PROP_ELEMENT_IMPLIED );
    }
    
    private void testEnabledFunctionOnProperty( final PropertyDef property )
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        final FunctionResult fr = ExpressionLanguageParser.parse( "${ " + property.name() + ".Enabled }" ).evaluate( context );
        
        try
        {
            assertFalse( (Boolean) fr.value() );
            
            element.setEnable( true );
            assertTrue( (Boolean) fr.value() );
            
            element.setEnable( false );
            assertFalse( (Boolean) fr.value() );
        }
        finally
        {
            fr.dispose();
        }
    }
    
    @Test

    public void testEnabledFunctionNull()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        final FunctionResult fr = ExpressionLanguageParser.parse( "${ Enabled( null ) }" ).evaluate( context );
        
        try
        {
            final Status st = fr.status();
            
            assertEquals( Status.Severity.ERROR, st.severity() );
            assertEquals( "Function Enabled does not accept nulls in position 0.", st.message() );
        }
        finally
        {
            fr.dispose();
        }
    }
    
    @Test

    public void testEnabledFunctionWrongType()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final FunctionContext context = new ModelElementFunctionContext( element );
        
        final FunctionResult fr = ExpressionLanguageParser.parse( "${ Enabled( 'abc' ) }" ).evaluate( context );
        
        try
        {
            final Status st = fr.status();
            
            assertEquals( Status.Severity.ERROR, st.severity() );
            assertEquals( "Function Enabled( java.lang.String ) is undefined.", st.message() );
        }
        finally
        {
            fr.dispose();
        }
    }

}
