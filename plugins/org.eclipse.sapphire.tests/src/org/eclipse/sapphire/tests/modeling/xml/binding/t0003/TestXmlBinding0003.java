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

package org.eclipse.sapphire.tests.modeling.xml.binding.t0003;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests namespace support in XML binding.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestXmlBinding0003 extends SapphireTestCase
{
    private TestXmlBinding0003( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestXmlBinding0003" );

        suite.addTest( new TestXmlBinding0003( "test" ) );
        
        return suite;
    }
    
    public void test() throws Exception
    {
        final ByteArrayResourceStore byteArrayResourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( byteArrayResourceStore );
        
        final TestXmlBinding0003A a = TestXmlBinding0003A.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        a.setAa( "aa" );
        
        final TestXmlBinding0003AB ab = a.getAb().element( true );
        ab.setAba( "aba" );
        
        final TestXmlBinding0003AC ac = a.getAc().insert();
        ac.setAca( "aca" );
        ac.setAcb( "acb" );
        
        final TestXmlBinding0003ACC acc = ac.getAcc().element( true );
        acc.setAcca( "acca" );
        
        final TestXmlBinding0003ACD acd = ac.getAcd().insert();
        acd.setAcda( "acda" );
        
        a.resource().save();
        
        final String actual = new String( byteArrayResourceStore.getContents(), "UTF-8" );
        
        assertEqualsIgnoreNewLineDiffs( loadResource( "expected.txt" ), actual );
    }

}
