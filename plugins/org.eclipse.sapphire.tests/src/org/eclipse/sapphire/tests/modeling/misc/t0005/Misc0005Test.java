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

package org.eclipse.sapphire.tests.modeling.misc.t0005;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests overriding of delegated methods.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class Misc0005Test

    extends SapphireTestCase
    
{
    private Misc0005Test( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "Misc0005" );

        suite.addTest( new Misc0005Test( "test" ) );
        
        return suite;
    }
    
    public void test() throws Exception
    {
        final ITestModelBase base = ITestModelBase.TYPE.instantiate();
        
        assertEquals( 1, base.test() );
        
        final ITestModelExtender extender = ITestModelExtender.TYPE.instantiate();
        
        assertEquals( 2, extender.test() );
    }

}
