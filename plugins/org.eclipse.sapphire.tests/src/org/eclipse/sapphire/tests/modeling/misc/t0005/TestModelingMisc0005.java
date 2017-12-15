/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.misc.t0005;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests overriding of delegated methods.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestModelingMisc0005

    extends SapphireTestCase
    
{
    private TestModelingMisc0005( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestModelingMisc0005" );

        suite.addTest( new TestModelingMisc0005( "test" ) );
        
        return suite;
    }
    
    public void test() throws Exception
    {
        final TestModelBase base = TestModelBase.TYPE.instantiate();
        
        assertEquals( 1, base.test() );
        
        final TestModelExtender extender = TestModelExtender.TYPE.instantiate();
        
        assertEquals( 2, extender.test() );
    }

}
