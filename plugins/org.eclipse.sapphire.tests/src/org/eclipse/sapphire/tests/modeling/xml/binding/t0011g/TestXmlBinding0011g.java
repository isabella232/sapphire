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

package org.eclipse.sapphire.tests.modeling.xml.binding.t0011g;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests association of XML element names with model element types.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestXmlBinding0011g extends SapphireTestCase
{
    private TestXmlBinding0011g( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestXmlBinding0011g" );

        suite.addTest( new TestXmlBinding0011g( "test" ) );
        
        return suite;
    }
    
    public void test() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( resourceStore );
        final TestModelRoot root = TestModelRoot.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
        ( (TestModelElementA1) root.getList().insert( TestModelElementA1.TYPE ) ).setValue( "1111" );
        ( (TestModelElementA2) root.getList().insert( TestModelElementA2.TYPE ) ).setValue( "2222" );
        
        ( (TestModelElementB2) root.getElement().element( true, TestModelElementB2.TYPE ) ).setValue( "3333" );
        
        root.resource().save();
        
        final String result = new String( resourceStore.getContents(), "UTF-8" );
        
        assertEqualsIgnoreNewLineDiffs( loadResource( "TestData.txt" ), result );
    }

}
