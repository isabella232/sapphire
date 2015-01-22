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

package org.eclipse.sapphire.tests.modeling;

import org.eclipse.sapphire.tests.modeling.el.ExpressionLanguageTests;
import org.eclipse.sapphire.tests.modeling.events.TestPropertyEvents;
import org.eclipse.sapphire.tests.modeling.misc.ModelingMiscTests;
import org.eclipse.sapphire.tests.modeling.properties.element.ElementPropertyTests;
import org.eclipse.sapphire.tests.modeling.xml.XmlTests;
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
        FindInsertionPositionTests.class,
        XmlTests.class,
        TopologicalSorterTests.class,
        ExpressionLanguageTests.class,
        TestPropertyEvents.class,
        ModelingMiscTests.class,
        ElementPropertyTests.class
    }
)

public final class SapphireModelingFrameworkTests
{
}
