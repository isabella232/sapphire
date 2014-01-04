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

package org.eclipse.sapphire.tests.modeling.el;

import org.eclipse.sapphire.tests.modeling.el.functions.FunctionTests;
import org.eclipse.sapphire.tests.modeling.el.operators.OperatorTests;
import org.eclipse.sapphire.tests.modeling.el.properties.PropertyTests;
import org.eclipse.sapphire.tests.modeling.el.t0001.TestExpr0001;
import org.eclipse.sapphire.tests.modeling.el.t0002.TestExpr0002;
import org.eclipse.sapphire.tests.modeling.el.t0003.TestExpr0003;
import org.eclipse.sapphire.tests.modeling.el.t0005.TestExpr0005;
import org.eclipse.sapphire.tests.modeling.el.t0007.TestExpr0007;
import org.eclipse.sapphire.tests.modeling.el.t0008.TestExpr0008;
import org.eclipse.sapphire.tests.modeling.el.t0009.TestExpr0009;
import org.eclipse.sapphire.tests.modeling.el.t0010.TestExpr0010;
import org.eclipse.sapphire.tests.modeling.el.t0011.TestExpr0011;
import org.eclipse.sapphire.tests.modeling.el.t0012.TestExpr0012;
import org.eclipse.sapphire.tests.modeling.el.t0013.TestExpr0013;
import org.eclipse.sapphire.tests.modeling.el.t0014.TestExpr0014;
import org.eclipse.sapphire.tests.modeling.el.t0016.TestExpr0016;
import org.eclipse.sapphire.tests.modeling.el.t0017.TestExpr0017;
import org.eclipse.sapphire.tests.modeling.el.t0018.TestExpr0018;
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
        MiscTests.class,
        OperatorTests.class,
        FunctionTests.class,
        PropertyTests.class,
        TestExpr0001.class,
        TestExpr0002.class,
        TestExpr0003.class,
        TestExpr0005.class,
        TestExpr0007.class,
        TestExpr0008.class,
        TestExpr0009.class,
        TestExpr0010.class,
        TestExpr0011.class,
        TestExpr0012.class,
        TestExpr0013.class,
        TestExpr0014.class,
        TestExpr0016.class,
        TestExpr0017.class,
        TestExpr0018.class
    }
)

public final class ExpressionLanguageTests
{
}

