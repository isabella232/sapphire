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

package org.eclipse.sapphire.tests.modeling.xml.xsd.t0006;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.modeling.xml.XmlUtil;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.w3c.dom.Document;

/**
 * Tests XmlUtil.convertToNamespaceForm() and convertFromNamespaceForm() methods.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestXmlXsd0006 extends SapphireTestCase
{
    private TestXmlXsd0006( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestXmlXsd0006" );

        suite.addTest( new TestXmlXsd0006( "testConvertToNamespaceForm1" ) );
        suite.addTest( new TestXmlXsd0006( "testConvertToNamespaceForm2" ) );
        suite.addTest( new TestXmlXsd0006( "testConvertToNamespaceForm3" ) );
        suite.addTest( new TestXmlXsd0006( "testConvertToNamespaceForm4" ) );
        
        suite.addTest( new TestXmlXsd0006( "testConvertFromNamespaceForm1" ) );
        suite.addTest( new TestXmlXsd0006( "testConvertFromNamespaceForm2" ) );
        
        return suite;
    }
    
    public void testConvertToNamespaceForm1() throws Exception
    {
        testConvertToNamespaceForm
        (
            "ConvertToNamespaceForm-1-Initial.txt",
            "ConvertToNamespaceForm-1-Expected.txt",
            "http://www.eclipse.org/sapphire/tests/xml/xsd/0006"
        );
    }

    public void testConvertToNamespaceForm2() throws Exception
    {
        testConvertToNamespaceForm
        (
            "ConvertToNamespaceForm-2-Initial.txt",
            "ConvertToNamespaceForm-2-Expected.txt",
            "http://www.eclipse.org/sapphire/tests/xml/xsd/0006",
            null
        );
    }

    public void testConvertToNamespaceForm3() throws Exception
    {
        testConvertToNamespaceForm
        (
            "ConvertToNamespaceForm-3-Initial.txt",
            "ConvertToNamespaceForm-3-Expected.txt",
            "http://www.eclipse.org/sapphire/tests/xml/xsd/0006",
            "http://www.eclipse.org/sapphire/tests/xml/xsd/0006"
        );
    }

    public void testConvertToNamespaceForm4() throws Exception
    {
        testConvertToNamespaceForm
        (
            "ConvertToNamespaceForm-4-Initial.txt",
            "ConvertToNamespaceForm-4-Expected.txt",
            "http://www.eclipse.org/sapphire/tests/xml/xsd/0006",
            "http://www.eclipse.org/sapphire/tests/xml/xsd/0006/v1"
        );
    }

    private void testConvertToNamespaceForm( final String initialResourceName,
                                             final String expectedResourceName,
                                             final String namespace )
                                                       
        throws Exception
        
    {
        test
        (
            initialResourceName,
            expectedResourceName,
            new ConvertOp()
            {
                @Override
                public void run( final Document document )
                {
                    XmlUtil.convertToNamespaceForm( document, namespace );
                }
            }
        );
    }

    private void testConvertToNamespaceForm( final String initialResourceName,
                                             final String expectedResourceName,
                                             final String namespace,
                                             final String schemaLocation )
                                                       
        throws Exception
        
    {
        test
        (
            initialResourceName,
            expectedResourceName,
            new ConvertOp()
            {
                @Override
                public void run( final Document document )
                {
                    XmlUtil.convertToNamespaceForm( document, namespace, schemaLocation );
                }
            }
        );
    }

    public void testConvertFromNamespaceForm1() throws Exception
    {
        testConvertFromNamespaceForm
        (
            "ConvertFromNamespaceForm-1-Initial.txt",
            "ConvertFromNamespaceForm-1-Expected.txt"
        );
    }

    public void testConvertFromNamespaceForm2() throws Exception
    {
        testConvertFromNamespaceForm
        (
            "ConvertFromNamespaceForm-2-Initial.txt",
            "ConvertFromNamespaceForm-2-Expected.txt"
        );
    }

    private void testConvertFromNamespaceForm( final String initialResourceName,
                                               final String expectedResourceName )
                                                       
        throws Exception
        
    {
        test
        (
            initialResourceName,
            expectedResourceName,
            new ConvertOp()
            {
                @Override
                public void run( final Document document )
                {
                    XmlUtil.convertFromNamespaceForm( document );
                }
            }
        );
    }
    
    private void test( final String initialResourceName,
                       final String expectedResourceName,
                       final ConvertOp op )
    
        throws Exception
        
    {
        final String initial = loadResource( initialResourceName );
        final String expected = loadResource( expectedResourceName );
        
        final ByteArrayResourceStore byteArrayResourceStore = new ByteArrayResourceStore( initial );
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( byteArrayResourceStore );
        
        op.run( xmlResourceStore.getDomDocument() );
        
        xmlResourceStore.save();
        
        final String actual = new String( byteArrayResourceStore.getContents(), "UTF-8" );
        
        assertEqualsIgnoreNewLineDiffs( expected, actual );
    }
    
    private static abstract class ConvertOp
    {
        public abstract void run( Document document );
    }
    
}
