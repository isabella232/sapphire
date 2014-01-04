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

package org.eclipse.sapphire.tests.modeling.xml.binding.t0008;

import static org.eclipse.sapphire.util.StringUtil.UTF8;

import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests removal of non-significant empty XML elements from an implied element property when all content
 * is cleared.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestXmlBinding0008 extends SapphireTestCase
{
    @Test
    
    public void test() throws Exception
    {
        final ByteArrayResourceStore byteArrayResourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( byteArrayResourceStore );

        final TestModelRoot root = TestModelRoot.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        final TestModelChild child = root.getChild();
        
        child.setAaa( "111" );
        child.setBbb( "222" );
        child.setCcc( "333" );
        
        root.resource().save();
        assertEqualsIgnoreNewLineDiffs( loadResource( "checkpoint-1.txt" ), new String( byteArrayResourceStore.getContents(), UTF8 ) );
        
        child.setAaa( null );

        root.resource().save();
        assertEqualsIgnoreNewLineDiffs( loadResource( "checkpoint-2.txt" ), new String( byteArrayResourceStore.getContents(), UTF8 ) );
        
        child.setBbb( null );

        root.resource().save();
        assertEqualsIgnoreNewLineDiffs( loadResource( "checkpoint-3.txt" ), new String( byteArrayResourceStore.getContents(), UTF8 ) );
        
        child.setCcc( null );

        root.resource().save();
        assertEqualsIgnoreNewLineDiffs( loadResource( "checkpoint-4.txt" ), new String( byteArrayResourceStore.getContents(), UTF8 ) );
    }

}
