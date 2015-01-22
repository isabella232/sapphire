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

package org.eclipse.sapphire.tests.java.jdt;

import org.eclipse.sapphire.tests.java.jdt.t0001.TestJavaJdt0001;
import org.eclipse.sapphire.tests.java.jdt.t0002.TestJavaJdt0002;
import org.eclipse.sapphire.tests.java.jdt.t0003.TestJavaJdt0003;
import org.eclipse.sapphire.tests.java.jdt.t0004.TestJavaJdt0004;
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
        TestJavaJdt0001.class,
        TestJavaJdt0002.class,
        TestJavaJdt0003.class,
        TestJavaJdt0004.class,
    }
)

public final class JavaJdtTestSuite
{
}
