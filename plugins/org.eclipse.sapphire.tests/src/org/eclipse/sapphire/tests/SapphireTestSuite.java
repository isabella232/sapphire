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

package org.eclipse.sapphire.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.sapphire.tests.java.JavaTestSuite;
import org.eclipse.sapphire.tests.modeling.SapphireModelingFrameworkTests;
import org.eclipse.sapphire.tests.services.ServicesTestSuite;
import org.eclipse.sapphire.tests.ui.UiTestSuite;
import org.eclipse.sapphire.tests.workspace.WorkspaceTestSuite;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireTestSuite extends TestCase
{
    private SapphireTestSuite( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "Sapphire" );
        
        suite.addTest( SapphireModelingFrameworkTests.suite() );
        suite.addTest( JavaTestSuite.suite() );
        suite.addTest( UiTestSuite.suite() );
        suite.addTest( WorkspaceTestSuite.suite() );
        suite.addTest( ServicesTestSuite.suite() );
        
        return suite;
    }
    
}
