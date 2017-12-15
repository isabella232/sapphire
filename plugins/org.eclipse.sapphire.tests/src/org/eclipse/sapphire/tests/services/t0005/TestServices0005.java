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

package org.eclipse.sapphire.tests.services.t0005;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests EqualityService.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestServices0005 extends SapphireTestCase
{
    private TestServices0005( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestServices0005" );

        suite.addTest( new TestServices0005( "test" ) );
        
        return suite;
    }
    
    public void test() throws Exception
    {
        final Contact x = Contact.TYPE.instantiate();
        x.setFirstName( "John" );
        x.setLastName( "Doe" );
        
        final Contact y = Contact.TYPE.instantiate();
        y.setFirstName( "John" );
        y.setLastName( "Doe" );

        final Contact z = Contact.TYPE.instantiate();
        z.setFirstName( "Jane" );
        z.setLastName( "Doe" );
        
        assertTrue( x.equals( y ) );
        assertFalse( x.equals( z ) );
        
        assertTrue( x.hashCode() == y.hashCode() );
        assertFalse( x.hashCode() == z.hashCode() );
    }
    
}
