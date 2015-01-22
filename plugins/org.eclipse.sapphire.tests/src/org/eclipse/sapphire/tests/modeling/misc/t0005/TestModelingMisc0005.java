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

package org.eclipse.sapphire.tests.modeling.misc.t0005;

import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests overriding of delegated methods.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestModelingMisc0005 extends SapphireTestCase
{
    @Test
    
    public void test() throws Exception
    {
        final TestModelBase base = TestModelBase.TYPE.instantiate();
        
        assertEquals( 1, base.test() );
        
        final TestModelExtender extender = TestModelExtender.TYPE.instantiate();
        
        assertEquals( 2, extender.test() );
    }

}
