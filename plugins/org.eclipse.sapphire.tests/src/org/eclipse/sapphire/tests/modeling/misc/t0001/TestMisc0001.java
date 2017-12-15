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

package org.eclipse.sapphire.tests.modeling.misc.t0001;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests reporting of failure to instantiate model element.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestMisc0001

    extends SapphireTestCase
    
{
    private TestMisc0001( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "Misc0001" );

        suite.addTest( new TestMisc0001( "test" ) );
        
        return suite;
    }
    
    public void test() throws Exception
    {
        try
        {
            ITestMisc0001.TYPE.instantiate();
            fail( "Did not catch the expected exception." );
        }
        catch( Exception e )
        {
            assertEquals( e.getMessage(), "ITestMisc0001 : Could not instantiate implementation class." );
        }
    }

}
