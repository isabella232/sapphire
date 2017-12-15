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

package org.eclipse.sapphire.tests.services.t0012;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.services.PossibleValuesService;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests instantiation of services to ensure that only one instance of a particular service implementation is
 * created in a particular context.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestServices0012 extends SapphireTestCase
{
    private TestServices0012( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestServices0012" );

        suite.addTest( new TestServices0012( "test" ) );
        
        return suite;
    }
    
    public void test() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        final List<PossibleValuesService> xl = element.services( TestElement.PROP_VALUE, PossibleValuesService.class );
        final List<TestPossibleValuesService> yl = element.services( TestElement.PROP_VALUE, TestPossibleValuesService.class );
        
        assertEquals( 1, xl.size() );
        assertEquals( 1, yl.size() );
        assertSame( xl.get( 0 ), yl.get( 0 ) );
        
        final PossibleValuesService x = element.service( TestElement.PROP_VALUE, PossibleValuesService.class );
        final TestPossibleValuesService y = element.service( TestElement.PROP_VALUE, TestPossibleValuesService.class );
        
        assertNotNull( x );
        assertSame( x, y );
    }

}
