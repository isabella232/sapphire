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

package org.eclipse.sapphire.tests;

import org.eclipse.sapphire.tests.binding.list.LayeredListPropertyBindingTests;
import org.eclipse.sapphire.tests.collation.CollationTests;
import org.eclipse.sapphire.tests.concurrency.ConcurrencyTests;
import org.eclipse.sapphire.tests.conversion.ConversionTestSuite;
import org.eclipse.sapphire.tests.element.ElementTests;
import org.eclipse.sapphire.tests.index.IndexTests;
import org.eclipse.sapphire.tests.java.JavaTestSuite;
import org.eclipse.sapphire.tests.misc.TestMisc;
import org.eclipse.sapphire.tests.modeling.SapphireModelingFrameworkTests;
import org.eclipse.sapphire.tests.observable.ObservableTests;
import org.eclipse.sapphire.tests.path.relative.RelativePathTests;
import org.eclipse.sapphire.tests.possible.PossibleValuesTest;
import org.eclipse.sapphire.tests.property.PropertyTests;
import org.eclipse.sapphire.tests.reference.element.ElementReferenceTests;
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
        CollationTests.class,
        ConcurrencyTests.class,
        ConversionTestSuite.class,
        ElementReferenceTests.class,
        ElementTests.class,
        IndexTests.class,
        JavaTestSuite.class,
        LayeredListPropertyBindingTests.class,
        ObservableTests.class,
        PossibleValuesTest.class,
        PropertyTests.class,
        RelativePathTests.class,
        SapphireModelingFrameworkTests.class,
        ServicesTestSuite.class,
        TestMisc.class,
        UiTestSuite.class,
        UniqueValueTests.class,
        WorkspaceTestSuite.class
    }
)

public final class SapphireTestSuite
{
}
