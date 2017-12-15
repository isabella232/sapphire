/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.el.t0001;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;

/**
 * Tests Parent and Root functions.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestExpr0001

    extends TestExpr
    
{
    private TestExpr0001( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestExpr0001" );

        suite.addTest( new TestExpr0001( "testParentFunction" ) );
        suite.addTest( new TestExpr0001( "testRootFunction" ) );
        
        return suite;
    }
    
    public void testParentFunction()
    {
        final ITestExpr0001ModelElement r = ITestExpr0001ModelElement.TYPE.instantiate();
        final ITestExpr0001ModelElement r_e = r.getElement().element( true );
        final ITestExpr0001ModelElement r_l = r.getList().addNewElement();
        final ITestExpr0001ModelElement r_e_l = r_e.getList().addNewElement();
        final ITestExpr0001ModelElement r_l_e = r_l.getElement().element( true );

        testForExpectedValue( r_e, "${ Parent() }", r );
        testForExpectedValue( r_l, "${ Parent() }", r );
        
        testForExpectedValue( r_e_l, "${ Parent() }", r_e );
        testForExpectedValue( r_l_e, "${ Parent() }", r_l );

        testForExpectedValue( r_e_l, "${ Parent().Parent() }", r );
        testForExpectedValue( r_l_e, "${ Parent().Parent() }", r );
    }

    public void testRootFunction()
    {
        final ITestExpr0001ModelElement r = ITestExpr0001ModelElement.TYPE.instantiate( new RootXmlResource() );
        final ITestExpr0001ModelElement r_e = r.getElement().element( true );
        final ITestExpr0001ModelElement r_l = r.getList().addNewElement();
        final ITestExpr0001ModelElement r_e_l = r_e.getList().addNewElement();
        final ITestExpr0001ModelElement r_l_e = r_l.getElement().element( true );

        testForExpectedValue( r_e, "${ Root() }", r );
        testForExpectedValue( r_l, "${ Root() }", r );
        
        testForExpectedValue( r_e_l, "${ Root() }", r );
        testForExpectedValue( r_l_e, "${ Root() }", r );

        testForExpectedValue( r_e_l, "${ Parent().Root() }", r );
        testForExpectedValue( r_l_e, "${ Parent().Root() }", r );

        testForExpectedValue( r_e_l, "${ Root().Root() }", r );
        testForExpectedValue( r_l_e, "${ Root().Root() }", r );
    }
    
}

