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

package org.eclipse.sapphire.tests.modeling.misc.t0014;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests operation of IModelElement.dispose() method when the element contains an element or a list property that
 * has not been accessed.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestModelingMisc0014 extends SapphireTestCase
{
    private TestModelingMisc0014( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestModelingMisc0014" );

        suite.addTest( new TestModelingMisc0014( "testElementProperty" ) );
        suite.addTest( new TestModelingMisc0014( "testImpliedElementProperty" ) );
        suite.addTest( new TestModelingMisc0014( "testListProperty" ) );
        
        return suite;
    }
    
    public void testElementProperty() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        root.getChildImplied();
        root.getChildren();
        root.dispose();
    }

    public void testImpliedElementProperty() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        root.getChild();
        root.getChildren();
        root.dispose();
    }
    
    public void testListProperty() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        root.getChild();
        root.getChildImplied();
        root.dispose();
    }

}
