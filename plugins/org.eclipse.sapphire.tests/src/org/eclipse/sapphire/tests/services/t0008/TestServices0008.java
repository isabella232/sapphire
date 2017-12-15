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

package org.eclipse.sapphire.tests.services.t0008;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.ValidationService;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests visibility of base property services in a derived property. 
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestServices0008 extends SapphireTestCase
{
    private TestServices0008( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestServices0008" );

        suite.addTest( new TestServices0008( "test" ) );
        
        return suite;
    }
    
    public void test() throws Exception
    {
        final DerivedElement element = DerivedElement.TYPE.instantiate();
        
        final List<ValidationService> services = element.services( DerivedElement.PROP_TEST_PROPERTY, ValidationService.class );
        
        assertEquals( 2, services.size() );
        
        assertContainsInstanceOf( services, BaseValidationService.class );
        assertContainsInstanceOf( services, DerivedValidationService.class );
        
        final List<Status> validation = element.getTestProperty().validation().children();
        
        assertEquals( 2, validation.size() );
        assertValidationError( validation.get( 0 ), "derived" );
        assertValidationError( validation.get( 1 ), "base" );
    }

}
