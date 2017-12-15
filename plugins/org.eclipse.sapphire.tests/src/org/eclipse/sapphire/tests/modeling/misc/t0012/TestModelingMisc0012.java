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

package org.eclipse.sapphire.tests.modeling.misc.t0012;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests the IModelElement.copy() method.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestModelingMisc0012 extends SapphireTestCase
{
    private TestModelingMisc0012( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestModelingMisc0012" );

        suite.addTest( new TestModelingMisc0012( "test" ) );
        
        return suite;
    }
    
    public void test() throws Exception
    {
        final TestElementRoot a = TestElementRoot.TYPE.instantiate();
        TestElementChild ac1;
        TestElementChildEx ac2;
        
        a.setValueProperty1( "abc" );
        a.setValueProperty2( 5 );
        a.setValueProperty3( (String) null );
        
        ac1 = a.getImpliedElementProperty1();
        ac1.setValueProperty1( "implied-1" );
        
        ac2 = a.getImpliedElementProperty2();
        ac2.setValueProperty1( "implied-2-a" );
        ac2.setValueProperty2( "implied-2-b" );
        
        ac1 = a.getElementProperty1().element( true );
        ac1.setValueProperty1( "element-1" );
        
        ac2 = (TestElementChildEx) a.getElementProperty2().element( true, TestElementChildEx.TYPE );
        ac2.setValueProperty1( "element-2-a" );
        ac2.setValueProperty2( "element-2-b" );
        
        ac1 = a.getListProperty1().insert();
        ac1.setValueProperty1( "list-1-a" );
        
        ac1 = a.getListProperty1().insert();
        ac1.setValueProperty1( "list-1-b" );
        
        ac1 = a.getListProperty1().insert();
        ac1.setValueProperty1( "list-1-c" );
        
        ac1 = a.getListProperty2().insert( TestElementChild.TYPE );
        ac1.setValueProperty1( "list-2-a" );

        ac2 = (TestElementChildEx) a.getListProperty2().insert( TestElementChildEx.TYPE );
        ac2.setValueProperty1( "list-2-b-a" );
        ac2.setValueProperty2( "list-2-b-b" );
        
        ac2 = (TestElementChildEx) a.getListProperty2().insert( TestElementChildEx.TYPE );
        ac2.setValueProperty1( "list-2-c-a" );
        ac2.setValueProperty2( "list-2-c-b" );
        
        final Object t = new Object();
        a.setTransientProperty( t );
        
        final TestElementRoot b = TestElementRoot.TYPE.instantiate();
        b.copy( a );
        
        assertEquals( b.getValueProperty1().getText( false ), "abc" );
        assertEquals( b.getValueProperty2().getText( false ), "5" );
        assertEquals( b.getValueProperty3().getText( false ), null );
        
        assertEquals( b.getImpliedElementProperty1().getValueProperty1().getText( false ), "implied-1" );
        assertEquals( b.getImpliedElementProperty2().getValueProperty1().getText( false ), "implied-2-a" );
        assertEquals( b.getImpliedElementProperty2().getValueProperty2().getText( false ), "implied-2-b" );
        
        assertEquals( b.getElementProperty1().element().getValueProperty1().getText( false ), "element-1" );
        assertEquals( ( (TestElementChildEx) b.getElementProperty2().element() ).getValueProperty1().getText( false ), "element-2-a" );
        assertEquals( ( (TestElementChildEx) b.getElementProperty2().element() ).getValueProperty2().getText( false ), "element-2-b" );
        
        assertEquals( b.getListProperty1().size(), 3 );
        assertEquals( b.getListProperty1().get( 0 ).getValueProperty1().getText( false ), "list-1-a" );
        assertEquals( b.getListProperty1().get( 1 ).getValueProperty1().getText( false ), "list-1-b" );
        assertEquals( b.getListProperty1().get( 2 ).getValueProperty1().getText( false ), "list-1-c" );
        
        assertEquals( b.getListProperty2().size(), 3 );
        assertEquals( b.getListProperty2().get( 0 ).getValueProperty1().getText( false ), "list-2-a" );
        assertEquals( ( (TestElementChildEx) b.getListProperty2().get( 1 ) ).getValueProperty1().getText( false ), "list-2-b-a" );
        assertEquals( ( (TestElementChildEx) b.getListProperty2().get( 1 ) ).getValueProperty2().getText( false ), "list-2-b-b" );
        assertEquals( ( (TestElementChildEx) b.getListProperty2().get( 2 ) ).getValueProperty1().getText( false ), "list-2-c-a" );
        assertEquals( ( (TestElementChildEx) b.getListProperty2().get( 2 ) ).getValueProperty2().getText( false ), "list-2-c-b" );
        
        assertEquals( b.getTransientProperty().content(), t );
    }

}
