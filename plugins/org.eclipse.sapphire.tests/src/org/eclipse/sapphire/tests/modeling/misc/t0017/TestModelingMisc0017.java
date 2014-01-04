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

package org.eclipse.sapphire.tests.modeling.misc.t0017;

import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests ability to safely access property enablement inside a validation service.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestModelingMisc0017 extends SapphireTestCase
{
    @Test
    
    public void testAccessToEnablementDuringValidation1() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
        element.getValue();
    }
    
    @Test

    public void testAccessToEnablementDuringValidation2() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
        element.property( TestElement.PROP_VALUE ).enabled();
    }

}
