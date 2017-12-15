/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.sapphire.tests.modeling.el.ExpressionLanguageTests;
import org.eclipse.sapphire.tests.modeling.serialization.SerializationTests;
import org.eclipse.sapphire.tests.modeling.xml.XmlBindingTests;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireModelingFrameworkTests

    extends TestCase
    
{
    private SapphireModelingFrameworkTests( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "SapphireModelingFrameworkTests" );
        
        suite.addTest( FindInsertionPositionTests.suite() );
        suite.addTest( XmlBindingTests.suite() );
        suite.addTest( SerializationTests.suite() );
        suite.addTest( TopologicalSorterTests.suite() );
        suite.addTest( ExpressionLanguageTests.suite() );
        
        return suite;
    }
    
}
