/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [337232] Certain schema causes elements to be out of order in corresponding xml files
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.xsd;

import org.eclipse.sapphire.tests.modeling.xml.xsd.all.XmlSchemaAllGroupTests;
import org.eclipse.sapphire.tests.modeling.xml.xsd.t0001.TestXmlXsd0001;
import org.eclipse.sapphire.tests.modeling.xml.xsd.t0002.TestXmlXsd0002;
import org.eclipse.sapphire.tests.modeling.xml.xsd.t0003.TestXmlXsd0003;
import org.eclipse.sapphire.tests.modeling.xml.xsd.t0004.TestXmlXsd0004;
import org.eclipse.sapphire.tests.modeling.xml.xsd.t0005.TestXmlXsd0005;
import org.eclipse.sapphire.tests.modeling.xml.xsd.t0006.TestXmlXsd0006;
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
        TestXmlXsd0001.class,
        TestXmlXsd0002.class,
        TestXmlXsd0003.class,
        TestXmlXsd0004.class,
        TestXmlXsd0005.class,
        TestXmlXsd0006.class,
        XmlSchemaAllGroupTests.class
    }
)

public final class XmlXsdTestSuite
{
}
