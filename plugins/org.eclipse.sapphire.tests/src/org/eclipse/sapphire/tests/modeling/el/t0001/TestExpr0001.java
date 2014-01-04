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

package org.eclipse.sapphire.tests.modeling.el.t0001;

import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests Parent and Root functions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestExpr0001 extends TestExpr
{
    @Test
    
    public void testParentFunction()
    {
        final TestExpr0001ModelElement r = TestExpr0001ModelElement.TYPE.instantiate();
        final TestExpr0001ModelElement r_e = r.getElement().content( true );
        final TestExpr0001ModelElement r_l = r.getList().insert();
        final TestExpr0001ModelElement r_e_l = r_e.getList().insert();
        final TestExpr0001ModelElement r_l_e = r_l.getElement().content( true );

        testForExpectedValue( r_e, "${ Parent() }", r );
        testForExpectedValue( r_l, "${ Parent() }", r );
        
        testForExpectedValue( r_e_l, "${ Parent() }", r_e );
        testForExpectedValue( r_l_e, "${ Parent() }", r_l );

        testForExpectedValue( r_e_l, "${ Parent().Parent() }", r );
        testForExpectedValue( r_l_e, "${ Parent().Parent() }", r );
    }
    
    @Test

    public void testRootFunction()
    {
        final TestExpr0001ModelElement r = TestExpr0001ModelElement.TYPE.instantiate( new RootXmlResource() );
        final TestExpr0001ModelElement r_e = r.getElement().content( true );
        final TestExpr0001ModelElement r_l = r.getList().insert();
        final TestExpr0001ModelElement r_e_l = r_e.getList().insert();
        final TestExpr0001ModelElement r_l_e = r_l.getElement().content( true );

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

