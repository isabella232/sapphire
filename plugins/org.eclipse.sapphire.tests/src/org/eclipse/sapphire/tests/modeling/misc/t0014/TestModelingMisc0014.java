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

package org.eclipse.sapphire.tests.modeling.misc.t0014;

import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests operation of Element.dispose() method when the element contains an element or a list property that
 * has not been accessed.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestModelingMisc0014 extends SapphireTestCase
{
    @Test
    
    public void testElementProperty() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        root.getChildImplied();
        root.getChildren();
        root.dispose();
    }

    @Test
    
    public void testImpliedElementProperty() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        root.getChild();
        root.getChildren();
        root.dispose();
    }
    
    @Test
    
    public void testListProperty() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        root.getChild();
        root.getChildImplied();
        root.dispose();
    }

}
