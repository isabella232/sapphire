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

package org.eclipse.sapphire.tests.modeling.xml;

import org.eclipse.sapphire.tests.modeling.xml.binding.XmlBindingTestSuite;
import org.eclipse.sapphire.tests.modeling.xml.dtd.XmlDtdTestSuite;
import org.eclipse.sapphire.tests.modeling.xml.xsd.XmlXsdTestSuite;
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
        XmlBindingTests.class,
        XmlBindingTestSuite.class,
        XmlDtdTestSuite.class,
        XmlXsdTestSuite.class
    }
)

public final class XmlTests
{
}
