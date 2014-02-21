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

package org.eclipse.sapphire.tests.modeling.el.functions;

import org.eclipse.sapphire.tests.modeling.el.functions.absolute.AbsolutePathFunctionTests;
import org.eclipse.sapphire.tests.modeling.el.functions.content.ContentFunctionTests;
import org.eclipse.sapphire.tests.modeling.el.functions.enabled.EnabledFunctionTests;
import org.eclipse.sapphire.tests.modeling.el.functions.encodetoxml.EncodeToXmlFunctionTests;
import org.eclipse.sapphire.tests.modeling.el.functions.endswith.EndsWithFunctionTests;
import org.eclipse.sapphire.tests.modeling.el.functions.fragment.FragmentFunctionTests;
import org.eclipse.sapphire.tests.modeling.el.functions.global.GlobalFunctionTests;
import org.eclipse.sapphire.tests.modeling.el.functions.head.HeadFunctionTests;
import org.eclipse.sapphire.tests.modeling.el.functions.index.IndexFunctionTests;
import org.eclipse.sapphire.tests.modeling.el.functions.matches.MatchesFunctionTests;
import org.eclipse.sapphire.tests.modeling.el.functions.message.MessageFunctionTests;
import org.eclipse.sapphire.tests.modeling.el.functions.overloaded.OverloadedFunctionTests;
import org.eclipse.sapphire.tests.modeling.el.functions.parent.part.PartParentFunctionTests;
import org.eclipse.sapphire.tests.modeling.el.functions.part.PartFunctionTests;
import org.eclipse.sapphire.tests.modeling.el.functions.replace.ReplaceFunctionTests;
import org.eclipse.sapphire.tests.modeling.el.functions.severity.SeverityFunctionTests;
import org.eclipse.sapphire.tests.modeling.el.functions.size.SizeFunctionTests;
import org.eclipse.sapphire.tests.modeling.el.functions.startswith.StartsWithFunctionTests;
import org.eclipse.sapphire.tests.modeling.el.functions.tail.TailFunctionTests;
import org.eclipse.sapphire.tests.modeling.el.functions.text.TextFunctionTests;
import org.eclipse.sapphire.tests.modeling.el.functions.validation.part.PartValidationFunctionTests;
import org.eclipse.sapphire.tests.modeling.el.functions.validation.property.PropertyValidationFunctionTests;
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
        AbsolutePathFunctionTests.class,
        ContentFunctionTests.class,
        EnabledFunctionTests.class,
        EncodeToXmlFunctionTests.class,
        EndsWithFunctionTests.class,
        FragmentFunctionTests.class,
        GlobalFunctionTests.class,
        HeadFunctionTests.class,
        IndexFunctionTests.class,
        MatchesFunctionTests.class,
        MessageFunctionTests.class,
        OverloadedFunctionTests.class,
        PartParentFunctionTests.class,
        PartFunctionTests.class,
        PartValidationFunctionTests.class,
        PropertyValidationFunctionTests.class,
        ReplaceFunctionTests.class,
        SeverityFunctionTests.class,
        SizeFunctionTests.class,
        StartsWithFunctionTests.class,
        TailFunctionTests.class,
        TextFunctionTests.class,
    }
)

public final class FunctionTests
{
}

