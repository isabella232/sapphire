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

package org.eclipse.sapphire.tests.modeling.misc;

import org.eclipse.sapphire.tests.modeling.misc.t0002.TestModelingMisc0002;
import org.eclipse.sapphire.tests.modeling.misc.t0003.TestModelingMisc0003;
import org.eclipse.sapphire.tests.modeling.misc.t0004.TestModelingMisc0004;
import org.eclipse.sapphire.tests.modeling.misc.t0005.TestModelingMisc0005;
import org.eclipse.sapphire.tests.modeling.misc.t0007.TestModelingMisc0007;
import org.eclipse.sapphire.tests.modeling.misc.t0008.TestModelingMisc0008;
import org.eclipse.sapphire.tests.modeling.misc.t0010.TestModelingMisc0010;
import org.eclipse.sapphire.tests.modeling.misc.t0012.ModelElementCopyTests;
import org.eclipse.sapphire.tests.modeling.misc.t0014.TestModelingMisc0014;
import org.eclipse.sapphire.tests.modeling.misc.t0015.TestModelingMisc0015;
import org.eclipse.sapphire.tests.modeling.misc.t0016.TestModelingMisc0016;
import org.eclipse.sapphire.tests.modeling.misc.t0017.TestModelingMisc0017;
import org.eclipse.sapphire.tests.modeling.misc.t0018.TestModelingMisc0018;
import org.eclipse.sapphire.tests.modeling.misc.t0019.ModelElementClearTests;
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
        TestModelingMisc0002.class,
        TestModelingMisc0003.class,
        TestModelingMisc0004.class,
        TestModelingMisc0005.class,
        TestModelingMisc0007.class,
        TestModelingMisc0008.class,
        TestModelingMisc0010.class,
        ModelElementCopyTests.class,
        TestModelingMisc0014.class,
        TestModelingMisc0015.class,
        TestModelingMisc0016.class,
        TestModelingMisc0017.class,
        TestModelingMisc0018.class,
        ModelElementClearTests.class
    }
)

public final class ModelingMiscTests
{
}
