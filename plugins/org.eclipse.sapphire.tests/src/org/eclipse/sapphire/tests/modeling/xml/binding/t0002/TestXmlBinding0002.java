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

package org.eclipse.sapphire.tests.modeling.xml.binding.t0002;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests detection and reporting of missing element name in @XmlListBinding.Mapping annotation.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestXmlBinding0002

    extends SapphireTestCase
    
{
    private TestXmlBinding0002( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "XmlBinding0002" );

        suite.addTest( new TestXmlBinding0002( "test" ) );
        
        return suite;
    }
    
    public void test() throws Exception
    {
        final XmlResourceStore xmlResourceStore = new XmlResourceStore();
        final ITestXmlBinding0002ModelElement element = ITestXmlBinding0002ModelElement.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
        try
        {
            element.getTestProperty().addNewElement();
            fail( "Did not catch the expected exception." );
        }
        catch( Exception e )
        {
            assertEquals( e.getMessage(), "ITestXmlBinding0002ModelElement.TestProperty : Element name must be specified in @XmlListBinding.Mapping annotation." );
        }
    }

}
