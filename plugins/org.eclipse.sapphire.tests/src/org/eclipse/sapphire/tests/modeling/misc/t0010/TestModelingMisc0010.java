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

package org.eclipse.sapphire.tests.modeling.misc.t0010;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests model element implementation generator with inner class style of type definition
 * where the inner type derives from the outer type.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestModelingMisc0010 extends SapphireTestCase
{
    private TestModelingMisc0010( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestModelingMisc0010" );

        suite.addTest( new TestModelingMisc0010( "test" ) );
        
        return suite;
    }
    
    public void test() throws Exception
    {
        Level1.TYPE.instantiate();
        Level1.Level2.TYPE.instantiate();
        Level1.Level2.Level3.TYPE.instantiate();
    }

}
