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

package org.eclipse.sapphire.tests.modeling.xml.dtd.t0003;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests DTD use in XML binding.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestXmlDtd0003 extends SapphireTestCase
{
    private TestXmlDtd0003( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestXmlDtd0003" );

        suite.addTest( new TestXmlDtd0003( "testPublic" ) );
        suite.addTest( new TestXmlDtd0003( "testSystem" ) );
        suite.addTest( new TestXmlDtd0003( "testError1" ) );
        suite.addTest( new TestXmlDtd0003( "testError2" ) );
        
        return suite;
    }
    
    public void testPublic() throws Exception
    {
        test( TestElementPublic.TYPE, "ExpectedPublic.txt" );
    }
    
    public void testSystem() throws Exception
    {
        test( TestElementSystem.TYPE, "ExpectedSystem.txt" );
    }
    
    public void testError1() throws Exception
    {
        test( TestElementError1.TYPE, "ExpectedError1.txt" );
    }

    public void testError2() throws Exception
    {
        test( TestElementError1.TYPE, "ExpectedError2.txt" );
    }

    private void test( final ModelElementType type,
                       final String expected )
                               
        throws Exception
        
    {
        final ByteArrayResourceStore byteArrayResourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( byteArrayResourceStore );

        final TestElement root = type.instantiate( new RootXmlResource( xmlResourceStore ) );
        root.getCcc().insert().setText( "111" );
        root.getBbb().insert().setText( "222" );
        root.getAaa().insert().setText( "333" );
        root.getCcc().insert().setText( "444" );
        root.getBbb().insert().setText( "555" );
        root.getAaa().insert().setText( "666" );
        
        root.resource().save();
        final String result = new String( byteArrayResourceStore.getContents(), "UTF-8" );
        
        assertEqualsIgnoreNewLineDiffs( loadResource( expected ), result );
    }

}
