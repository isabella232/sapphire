/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.binding.t0001;

import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests detection and reporting of missing element name in @XmlElementBinding.Mapping annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestXmlBinding0001 extends SapphireTestCase
{
    @Test
    
    public void test() throws Exception
    {
        final XmlResourceStore xmlResourceStore = new XmlResourceStore();
        final TestXmlBinding0001ModelElement element = TestXmlBinding0001ModelElement.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
        try
        {
            element.getTestProperty().content( true );
            fail( "Did not catch the expected exception." );
        }
        catch( Exception e )
        {
            assertEquals( "TestXmlBinding0001ModelElement.TestProperty : Element name must be specified in @XmlElementBinding.Mapping annotation.", e.getMessage() );
        }
    }

}
