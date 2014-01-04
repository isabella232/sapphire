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

package org.eclipse.sapphire.tests.modeling.xml.binding.t0011a;

import static org.eclipse.sapphire.util.StringUtil.UTF8;

import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests association of XML element names with model element types.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestXmlBinding0011a extends SapphireTestCase
{
    @Test
    
    public void test() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( resourceStore );
        final TestModelRoot root = TestModelRoot.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
        ( (TestModelElementA1) root.getList().insert( TestModelElementA1.TYPE ) ).setValue( "1111" );
        ( (TestModelElementA2) root.getList().insert( TestModelElementA2.TYPE ) ).setValue( "2222" );
        
        ( (TestModelElementB2) root.getElement().content( true, TestModelElementB2.TYPE ) ).setValue( "3333" );
        
        root.resource().save();
        
        final String result = new String( resourceStore.getContents(), UTF8 );
        
        assertEqualsIgnoreNewLineDiffs( loadResource( "TestData.txt" ), result );
    }

}
