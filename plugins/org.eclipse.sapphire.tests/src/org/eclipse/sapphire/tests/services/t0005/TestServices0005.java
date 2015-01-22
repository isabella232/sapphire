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

package org.eclipse.sapphire.tests.services.t0005;

import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests EqualityService.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestServices0005 extends SapphireTestCase
{
    @Test
    
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
