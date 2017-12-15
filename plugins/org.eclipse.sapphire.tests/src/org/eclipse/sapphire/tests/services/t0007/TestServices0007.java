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

package org.eclipse.sapphire.tests.services.t0007;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.ValidationService;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests visibility of base element services in a derived element. 
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestServices0007 extends SapphireTestCase
{
    private TestServices0007( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestServices0007" );

        suite.addTest( new TestServices0007( "test" ) );
        
        return suite;
    }
    
    public void test() throws Exception
    {
        final DerivedElement element = DerivedElement.TYPE.instantiate();
        
        final List<ValidationService> services = element.services( ValidationService.class );
        
        assertEquals( 3, services.size() );
        
        assertContainsInstanceOf( services, BaseValidationService.class );
        assertContainsInstanceOf( services, DerivedValidationService.class );
        
        final List<Status> validation = element.validation().children();
        
        assertEquals( 2, validation.size() );
        assertValidationError( validation.get( 0 ), "derived" );
        assertValidationError( validation.get( 1 ), "base" );
    }

}
