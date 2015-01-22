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

package org.eclipse.sapphire.tests.modeling.xml.dtd.t0003;

import static org.eclipse.sapphire.util.StringUtil.UTF8;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests DTD use in XML binding.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestXmlDtd0003 extends SapphireTestCase
{
    @Test
    
    public void testPublic() throws Exception
    {
        test( TestElementPublic.TYPE, "ExpectedPublic.txt" );
    }
    
    @Test
    
    public void testSystem() throws Exception
    {
        test( TestElementSystem.TYPE, "ExpectedSystem.txt" );
    }
    
    @Test
    
    public void testError1() throws Exception
    {
        test( TestElementError1.TYPE, "ExpectedError1.txt" );
    }

    @Test
    
    public void testError2() throws Exception
    {
        test( TestElementError1.TYPE, "ExpectedError2.txt" );
    }

    private void test( final ElementType type,
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
        final String result = new String( byteArrayResourceStore.getContents(), UTF8 );
        
        assertEqualsIgnoreNewLineDiffs( loadResource( expected ), result );
    }

}
