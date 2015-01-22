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

package org.eclipse.sapphire.tests.modeling.misc.t0010;

import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests model element implementation generator with inner class style of type definition
 * where the inner type derives from the outer type.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestModelingMisc0010 extends SapphireTestCase
{
    @Test
    
    public void test() throws Exception
    {
        Level1.TYPE.instantiate();
        Level1.Level2.TYPE.instantiate();
        Level1.Level2.Level3.TYPE.instantiate();
    }

}
