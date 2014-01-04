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

package org.eclipse.sapphire.tests.modeling.xml.binding.t0003;

import static org.eclipse.sapphire.util.StringUtil.UTF8;

import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests namespace support in XML binding.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestXmlBinding0003 extends SapphireTestCase
{
    @Test
    
    public void test() throws Exception
    {
        final ByteArrayResourceStore byteArrayResourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( byteArrayResourceStore );
        
        final TestXmlBinding0003A a = TestXmlBinding0003A.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        a.setAa( "aa" );
        
        final TestXmlBinding0003AB ab = a.getAb().content( true );
        ab.setAba( "aba" );
        
        final TestXmlBinding0003AC ac = a.getAc().insert();
        ac.setAca( "aca" );
        ac.setAcb( "acb" );
        
        final TestXmlBinding0003ACC acc = ac.getAcc().content( true );
        acc.setAcca( "acca" );
        
        final TestXmlBinding0003ACD acd = ac.getAcd().insert();
        acd.setAcda( "acda" );
        
        a.resource().save();
        
        final String actual = new String( byteArrayResourceStore.getContents(), UTF8 );
        
        assertEqualsIgnoreNewLineDiffs( loadResource( "expected.txt" ), actual );
    }

}
