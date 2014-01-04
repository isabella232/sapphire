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

package org.eclipse.sapphire.tests.modeling.xml.dtd;

import org.eclipse.sapphire.tests.modeling.xml.dtd.t0001.TestXmlDtd0001;
import org.eclipse.sapphire.tests.modeling.xml.dtd.t0002.TestXmlDtd0002;
import org.eclipse.sapphire.tests.modeling.xml.dtd.t0003.TestXmlDtd0003;
import org.eclipse.sapphire.tests.modeling.xml.dtd.t0004.TestEntityRefInAttList;
import org.eclipse.sapphire.tests.modeling.xml.dtd.t0005.TestCatalogResolution;
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
        TestXmlDtd0001.class,
        TestXmlDtd0002.class,
        TestXmlDtd0003.class,
        TestEntityRefInAttList.class,
        TestCatalogResolution.class
    }
)

public final class XmlDtdTestSuite
{
}
