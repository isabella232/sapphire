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

package org.eclipse.sapphire.tests;

import org.eclipse.sapphire.tests.binding.list.LayeredListPropertyBindingTests;
import org.eclipse.sapphire.tests.concurrency.ConcurrencyTests;
import org.eclipse.sapphire.tests.conversion.ConversionTestSuite;
import org.eclipse.sapphire.tests.index.IndexTests;
import org.eclipse.sapphire.tests.java.JavaTestSuite;
import org.eclipse.sapphire.tests.misc.TestMisc;
import org.eclipse.sapphire.tests.modeling.SapphireModelingFrameworkTests;
import org.eclipse.sapphire.tests.path.relative.RelativePathTests;
import org.eclipse.sapphire.tests.possible.PossibleValuesTest;
import org.eclipse.sapphire.tests.services.ServicesTestSuite;
import org.eclipse.sapphire.tests.ui.UiTestSuite;
import org.eclipse.sapphire.tests.unique.UniqueValueTests;
import org.eclipse.sapphire.tests.workspace.WorkspaceTestSuite;
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
        LayeredListPropertyBindingTests.class,
        ConversionTestSuite.class,
        TestMisc.class,
        SapphireModelingFrameworkTests.class,
        JavaTestSuite.class,
        UiTestSuite.class,
        WorkspaceTestSuite.class,
        ServicesTestSuite.class,
        UniqueValueTests.class,
        IndexTests.class,
        ConcurrencyTests.class,
        PossibleValuesTest.class,
        RelativePathTests.class
    }
)

public final class SapphireTestSuite
{
}
