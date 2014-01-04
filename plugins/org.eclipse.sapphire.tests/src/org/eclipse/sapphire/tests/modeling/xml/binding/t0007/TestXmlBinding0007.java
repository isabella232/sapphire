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

package org.eclipse.sapphire.tests.modeling.xml.binding.t0007;

import static org.eclipse.sapphire.util.StringUtil.UTF8;

import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests adding of namespace declaration when default prefix for the namespace is already in use.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestXmlBinding0007 extends SapphireTestCase
{
    @Test
    
    public void test() throws Exception
    {
        final ByteArrayResourceStore byteArrayResourceStore = new ByteArrayResourceStore( loadResourceAsStream( "initial.txt" ) );
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( byteArrayResourceStore );

        final TestElement root = TestElement.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        root.setTestProperty( "abc" );
        
        root.resource().save();
        final String result = new String( byteArrayResourceStore.getContents(), UTF8 );
        
        assertEqualsIgnoreNewLineDiffs( loadResource( "expected.txt" ), result );
    }

}
