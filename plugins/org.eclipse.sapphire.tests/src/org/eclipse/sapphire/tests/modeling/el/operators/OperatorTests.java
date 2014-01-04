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

package org.eclipse.sapphire.tests.modeling.el.operators;

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
        AdditionOperatorTests.class,
        SubtractionOperatorTests.class,
        MultiplicationOperatorTests.class,
        DivisionOperatorTests.class,
        ModuloOperatorTests.class,
        ArithmeticNegationOperatorTests.class,
        EqualityOperatorTests.class,
        InequalityOperatorTests.class,
        LessThanOperatorTests.class,
        LessThanOrEqualOperatorTests.class,
        GreaterThanOperatorTests.class,
        GreaterThanOrEqualOperatorTests.class,
        LogicalConjunctionOperatorTests.class,
        LogicalDisjunctionOperatorTests.class,
        LogicalNegationOperatorTests.class,
        EmptyOperatorTests.class,
        ConditionalOperatorTests.class,
        MembershipOperatorTests.class
    }
)

public final class OperatorTests
{
}

