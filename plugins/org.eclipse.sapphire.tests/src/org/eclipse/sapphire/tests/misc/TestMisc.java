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

package org.eclipse.sapphire.tests.misc;

import org.eclipse.sapphire.tests.misc.t0001.TestMisc0001;
import org.eclipse.sapphire.tests.misc.t0002.TestMisc0002;
import org.eclipse.sapphire.tests.misc.t0003.TestMisc0003;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@RunWith( Suite.class )

@SuiteClasses
(
    {
        TestMisc0001.class,
        TestMisc0002.class,
        TestMisc0003.class
    }
)

public final class TestMisc
{
}
