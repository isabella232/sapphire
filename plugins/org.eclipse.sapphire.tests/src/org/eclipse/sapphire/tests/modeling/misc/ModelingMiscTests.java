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

package org.eclipse.sapphire.tests.modeling.misc;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.sapphire.tests.modeling.misc.t0001.TestModelingMisc0001;
import org.eclipse.sapphire.tests.modeling.misc.t0002.TestModelingMisc0002;
import org.eclipse.sapphire.tests.modeling.misc.t0003.TestModelingMisc0003;
import org.eclipse.sapphire.tests.modeling.misc.t0004.TestModelingMisc0004;
import org.eclipse.sapphire.tests.modeling.misc.t0005.TestModelingMisc0005;
import org.eclipse.sapphire.tests.modeling.misc.t0006.TestModelingMisc0006;
import org.eclipse.sapphire.tests.modeling.misc.t0007.TestModelingMisc0007;
import org.eclipse.sapphire.tests.modeling.misc.t0008.TestModelingMisc0008;
import org.eclipse.sapphire.tests.modeling.misc.t0009.TestModelingMisc0009;
import org.eclipse.sapphire.tests.modeling.misc.t0010.TestModelingMisc0010;
import org.eclipse.sapphire.tests.modeling.misc.t0011.TestModelingMisc0011;
import org.eclipse.sapphire.tests.modeling.misc.t0012.TestModelingMisc0012;
import org.eclipse.sapphire.tests.modeling.misc.t0013.TestModelingMisc0013;
import org.eclipse.sapphire.tests.modeling.misc.t0014.TestModelingMisc0014;
import org.eclipse.sapphire.tests.modeling.misc.t0015.TestModelingMisc0015;
import org.eclipse.sapphire.tests.modeling.misc.t0016.TestModelingMisc0016;
import org.eclipse.sapphire.tests.modeling.misc.t0017.TestModelingMisc0017;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ModelingMiscTests extends TestCase
{
    private ModelingMiscTests( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "ModelingMiscTests" );

        suite.addTest( TestModelingMisc0001.suite() );
        suite.addTest( TestModelingMisc0002.suite() );
        suite.addTest( TestModelingMisc0003.suite() );
        suite.addTest( TestModelingMisc0004.suite() );
        suite.addTest( TestModelingMisc0005.suite() );
        suite.addTest( TestModelingMisc0006.suite() );
        suite.addTest( TestModelingMisc0007.suite() );
        suite.addTest( TestModelingMisc0008.suite() );
        suite.addTest( TestModelingMisc0009.suite() );
        suite.addTest( TestModelingMisc0010.suite() );
        suite.addTest( TestModelingMisc0011.suite() );
        suite.addTest( TestModelingMisc0012.suite() );
        suite.addTest( TestModelingMisc0013.suite() );
        suite.addTest( TestModelingMisc0014.suite() );
        suite.addTest( TestModelingMisc0015.suite() );
        suite.addTest( TestModelingMisc0016.suite() );
        suite.addTest( TestModelingMisc0017.suite() );
        
        return suite;
    }
    
}
