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

package org.eclipse.sapphire.tests.modeling.xml.dtd.t0003;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests DTD use in XML binding.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestXmlDtd0003

    extends SapphireTestCase
    
{
    private TestXmlDtd0003( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "XmlDtd0003" );

        suite.addTest( new TestXmlDtd0003( "test" ) );
        
        return suite;
    }
    
    public void test() throws Exception
    {
        final ByteArrayResourceStore byteArrayResourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( byteArrayResourceStore );

        final ITestElement root = ITestElement.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        root.getCcc().addNewElement().setText( "111" );
        root.getBbb().addNewElement().setText( "222" );
        root.getAaa().addNewElement().setText( "333" );
        root.getCcc().addNewElement().setText( "444" );
        root.getBbb().addNewElement().setText( "555" );
        root.getAaa().addNewElement().setText( "666" );
        
        root.resource().save();
        final String result = new String( byteArrayResourceStore.getContents(), "UTF-8" );
        
        assertEqualsIgnoreNewLineDiffs( loadResource( "expected.txt" ), result );
    }

}
