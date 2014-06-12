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

package org.eclipse.sapphire.tests.misc.t0001;

import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.VersionConstraint;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests Sapphire.version() method.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestMisc0001 extends SapphireTestCase
{
    private static final String EXPECTED_VERSION_CONSTRAINT = "[0.7.4-0.7.5)";

    @Test
    
    public void testSapphireVersion() throws Exception
    {
        final Version version = Sapphire.version();
        
        assertNotNull( version );
        
        final VersionConstraint constraint = new VersionConstraint( EXPECTED_VERSION_CONSTRAINT );
        
        assertTrue( constraint.check( version ) );
    }

}
