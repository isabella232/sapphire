/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.services.t0012;

import java.util.List;

import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests instantiation of services to ensure that only one instance of a particular service implementation is
 * created in a particular context.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestServices0012 extends SapphireTestCase
{
    @Test
    
    public void test() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final Property property = element.property( TestElement.PROP_VALUE );
        
        final List<PossibleValuesService> xl = property.services( PossibleValuesService.class );
        final List<TestPossibleValuesService> yl = property.services( TestPossibleValuesService.class );
        
        assertEquals( 1, xl.size() );
        assertEquals( 1, yl.size() );
        assertSame( xl.get( 0 ), yl.get( 0 ) );
        
        final PossibleValuesService x = property.service( PossibleValuesService.class );
        final TestPossibleValuesService y = property.service( TestPossibleValuesService.class );
        
        assertNotNull( x );
        assertSame( x, y );
    }

}
