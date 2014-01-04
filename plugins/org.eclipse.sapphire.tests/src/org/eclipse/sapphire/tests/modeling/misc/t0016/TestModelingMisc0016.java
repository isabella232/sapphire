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

package org.eclipse.sapphire.tests.modeling.misc.t0016;

import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests behavior of Element.enabled( ModelProperty ) method for overridden properties.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestModelingMisc0016 extends SapphireTestCase
{
    @Test
    
    public void testEnablementOfOverriddenProperty() throws Exception
    {
        final BaseElement base = BaseElement.TYPE.instantiate();
        
        assertTrue( base.property( BaseElement.PROP_VALUE ).enabled() );
        
        final DerivedElement derived = DerivedElement.TYPE.instantiate();
        
        assertFalse( derived.property( BaseElement.PROP_VALUE ).enabled() );
        assertFalse( derived.property( DerivedElement.PROP_VALUE ).enabled() );
    }

}
