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

package org.eclipse.sapphire.tests.services;

import org.eclipse.sapphire.tests.services.t0001.TestServices0001;
import org.eclipse.sapphire.tests.services.t0002.TestServices0002;
import org.eclipse.sapphire.tests.services.t0003.TestServices0003;
import org.eclipse.sapphire.tests.services.t0004.TestServices0004;
import org.eclipse.sapphire.tests.services.t0005.TestServices0005;
import org.eclipse.sapphire.tests.services.t0008.TestServices0008;
import org.eclipse.sapphire.tests.services.t0009.TestServices0009;
import org.eclipse.sapphire.tests.services.t0010.TestServices0010;
import org.eclipse.sapphire.tests.services.t0011.PreferDefaultValueTests;
import org.eclipse.sapphire.tests.services.t0012.TestServices0012;
import org.eclipse.sapphire.tests.services.t0013.RequiredConstraintTests;
import org.eclipse.sapphire.tests.services.t0014.DeclarativeValidationServiceTests;
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
        TestServices0001.class,
        TestServices0002.class,
        TestServices0003.class,
        TestServices0004.class,
        TestServices0005.class,
        TestServices0008.class,
        TestServices0009.class,
        TestServices0010.class,
        PreferDefaultValueTests.class,
        TestServices0012.class,
        RequiredConstraintTests.class,
        DeclarativeValidationServiceTests.class
    }
)

public final class ServicesTestSuite
{
}
