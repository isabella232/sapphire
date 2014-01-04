/******************************************************************************
 * Copyright (c) 2014 Oracle
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
import java.util.SortedSet;

import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.ValidationService;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests visibility of base property services in a derived property. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestServices0008 extends SapphireTestCase
{
    @Test
    
    public void test() throws Exception
    {
        final DerivedElement element = DerivedElement.TYPE.instantiate();
        
        final List<ValidationService> services = element.property( DerivedElement.PROP_TEST_PROPERTY ).services( ValidationService.class );
        
        assertContainsInstanceOf( services, BaseValidationService.class );
        assertContainsInstanceOf( services, DerivedValidationService.class );
        
        final SortedSet<Status> validation = element.getTestProperty().validation().children();
        
        assertEquals( 2, validation.size() );
        assertValidationError( item( validation, 0 ), "base" );
        assertValidationError( item( validation, 1 ), "derived" );
    }

}
