/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.misc.t0017;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests ability to safely access property enablement inside a validation service.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestModelingMisc0017 extends SapphireTestCase
{
    private TestModelingMisc0017( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestModelingMisc0017" );

        suite.addTest( new TestModelingMisc0017( "testAccessToEnablementDuringValidation1" ) );
        suite.addTest( new TestModelingMisc0017( "testAccessToEnablementDuringValidation2" ) );
        
        return suite;
    }
    
    public void testAccessToEnablementDuringValidation1() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
        element.getValue();
    }

    public void testAccessToEnablementDuringValidation2() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
        element.enabled( TestElement.PROP_VALUE );
    }

}
