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

package org.eclipse.sapphire.tests.services.t0009;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.VersionConstraint;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests VersionSerializationService and VersionConstraintSerializationService. 
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestServices0009 extends SapphireTestCase
{
    private TestServices0009( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestServices0009" );

        suite.addTest( new TestServices0009( "testVersionSerializationService" ) );
        suite.addTest( new TestServices0009( "testVersionConstraintSerializationService" ) );
        
        return suite;
    }
    
    public void testVersionSerializationService() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        element.setVersion( "1.2.3" );
        
        final Version version = element.getVersion().getContent();
        
        assertNotNull( version );
        assertEquals( 3, version.length() );
        assertEquals( 1, version.segment( 0 ) );
        assertEquals( 2, version.segment( 1 ) );
        assertEquals( 3, version.segment( 2 ) );
    }

    public void testVersionConstraintSerializationService() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        element.setVersionConstraint( "[1.2.3-2.0)" );
        
        final VersionConstraint constraint = element.getVersionConstraint().getContent();
        
        assertNotNull( constraint );
        assertTrue( constraint.check( "1.2.3" ) );
        assertTrue( constraint.check( "1.5" ) );
        assertFalse( constraint.check( "2.0" ) );
        assertFalse( constraint.check( "55.0" ) );
    }

}
