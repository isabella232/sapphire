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

package org.eclipse.sapphire.tests.services.t0006;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests ValidationService in model element context.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestServices0006 extends SapphireTestCase
{
    private TestServices0006( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestServices0006" );

        suite.addTest( new TestServices0006( "test" ) );
        
        return suite;
    }
    
    public void test() throws Exception
    {
        final Contact person = Contact.TYPE.instantiate();
        
        person.setFirstName( "John" );
        
        assertValidationError( person, "Last name must be specified." );
        
        person.setLastName( "Doe" );
        
        assertValidationWarning( person, "John Doe is likely a fake name." );
        
        person.setLastName( "Smith" );
        
        assertValidationOk( person );
    }
    
}
