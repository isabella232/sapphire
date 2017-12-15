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

package org.eclipse.sapphire.tests.modeling.xml.binding.t0005;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests reporting of unresolvable namespace usage in element property binding.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestXmlBinding0005

    extends SapphireTestCase
    
{
    private TestXmlBinding0005( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "XmlBinding0005" );

        suite.addTest( new TestXmlBinding0005( "testInMapping" ) );
        suite.addTest( new TestXmlBinding0005( "testInPath" ) );
        
        return suite;
    }
    
    public void testInMapping() throws Exception
    {
        final XmlResourceStore xmlResourceStore = new XmlResourceStore();
        final ITestXmlBinding0005A element = ITestXmlBinding0005A.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
        try
        {
            element.getTestProperty().element( true );
            fail( "Did not catch the expected exception." );
        }
        catch( Exception e )
        {
            assertEquals( e.getMessage(), "ITestXmlBinding0005A.TestProperty : Could not resolve namespace for foo:abc node name." );
        }
    }

    public void testInPath() throws Exception
    {
        final XmlResourceStore xmlResourceStore = new XmlResourceStore();
        final ITestXmlBinding0005B element = ITestXmlBinding0005B.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
        try
        {
            element.getTestProperty().element( true );
            fail( "Did not catch the expected exception." );
        }
        catch( Exception e )
        {
            assertEquals( e.getMessage(), "ITestXmlBinding0005B.TestProperty : Could not resolve namespace for foo:abc node name." );
        }
    }

}
