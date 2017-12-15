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

package org.eclipse.sapphire.tests.modeling.xml.binding.t0012;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests for XmlDelimitedListBindingImpl and DelimitedListBindingImpl. 
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestXmlBinding0012 extends SapphireTestCase
{
    private TestXmlBinding0012( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestXmlBinding0012" );

        suite.addTest( new TestXmlBinding0012( "testInsertOneAtEnd" ) );
        suite.addTest( new TestXmlBinding0012( "testInsertTwoAtEnd" ) );
        
        return suite;
    }
    
    public void testInsertOneAtEnd() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( resourceStore );
        final TestElement element = TestElement.TYPE.instantiate(  new RootXmlResource( xmlResourceStore ) );
        
        
        try
        {
            final TestListEntry x = element.getList().insert();
            x.setValue( "x" );
        }
        finally
        {
            element.dispose();
        }
    }

    public void testInsertTwoAtEnd() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( resourceStore );
        final TestElement element = TestElement.TYPE.instantiate(  new RootXmlResource( xmlResourceStore ) );
        
        try
        {
            final TestListEntry x = element.getList().insert();
            x.setValue( "x" );
            
            final TestListEntry y = element.getList().insert();
            y.setValue( "y" );
        }
        finally
        {
            element.dispose();
        }
    }

}
