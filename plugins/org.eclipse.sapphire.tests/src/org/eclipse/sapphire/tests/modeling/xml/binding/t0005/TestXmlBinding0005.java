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

package org.eclipse.sapphire.tests.modeling.xml.binding.t0005;

import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests reporting of unresolvable namespace usage in element property binding.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestXmlBinding0005 extends SapphireTestCase
{
    @Test
    
    public void testInMapping() throws Exception
    {
        final XmlResourceStore xmlResourceStore = new XmlResourceStore();
        final TestXmlBinding0005A element = TestXmlBinding0005A.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
        try
        {
            element.getTestProperty().content( true );
            fail( "Did not catch the expected exception." );
        }
        catch( Exception e )
        {
            assertEquals( e.getMessage(), "TestXmlBinding0005A.TestProperty : Could not resolve namespace for foo:abc node name." );
        }
    }

    @Test
    
    public void testInPath() throws Exception
    {
        final XmlResourceStore xmlResourceStore = new XmlResourceStore();
        final TestXmlBinding0005B element = TestXmlBinding0005B.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
        try
        {
            element.getTestProperty().content( true );
            fail( "Did not catch the expected exception." );
        }
        catch( Exception e )
        {
            assertEquals( e.getMessage(), "TestXmlBinding0005B.TestProperty : Could not resolve namespace for foo:abc node name." );
        }
    }

}
