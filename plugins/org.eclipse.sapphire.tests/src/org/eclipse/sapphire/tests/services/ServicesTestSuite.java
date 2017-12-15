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

package org.eclipse.sapphire.tests.services;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.sapphire.tests.services.t0001.TestServices0001;
import org.eclipse.sapphire.tests.services.t0002.TestServices0002;
import org.eclipse.sapphire.tests.services.t0003.TestServices0003;
import org.eclipse.sapphire.tests.services.t0004.TestServices0004;
import org.eclipse.sapphire.tests.services.t0005.TestServices0005;
import org.eclipse.sapphire.tests.services.t0006.TestServices0006;
import org.eclipse.sapphire.tests.services.t0007.TestServices0007;
import org.eclipse.sapphire.tests.services.t0008.TestServices0008;
import org.eclipse.sapphire.tests.services.t0009.TestServices0009;
import org.eclipse.sapphire.tests.services.t0010.TestServices0010;
import org.eclipse.sapphire.tests.services.t0011.PreferDefaultValueTests;
import org.eclipse.sapphire.tests.services.t0012.TestServices0012;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ServicesTestSuite extends TestCase
{
    private ServicesTestSuite( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "Services" );

        suite.addTest( TestServices0001.suite() );
        suite.addTest( TestServices0002.suite() );
        suite.addTest( TestServices0003.suite() );
        suite.addTest( TestServices0004.suite() );
        suite.addTest( TestServices0005.suite() );
        suite.addTest( TestServices0006.suite() );
        suite.addTest( TestServices0007.suite() );
        suite.addTest( TestServices0008.suite() );
        suite.addTest( TestServices0009.suite() );
        suite.addTest( TestServices0010.suite() );
        suite.addTest( PreferDefaultValueTests.suite() );
        suite.addTest( TestServices0012.suite() );
        
        return suite;
    }
    
}
