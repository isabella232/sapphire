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

package org.eclipse.sapphire.tests.modeling.xml.xsd.t0006;

import static org.eclipse.sapphire.util.StringUtil.UTF8;

import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.modeling.xml.XmlUtil;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Tests XmlUtil.convertToNamespaceForm() and convertFromNamespaceForm() methods.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestXmlXsd0006 extends SapphireTestCase
{
    @Test
    
    public void testConvertToNamespaceForm1() throws Exception
    {
        testConvertToNamespaceForm
        (
            "ConvertToNamespaceForm-1-Initial.txt",
            "ConvertToNamespaceForm-1-Expected.txt",
            "http://www.eclipse.org/sapphire/tests/xml/xsd/0006"
        );
    }
    
    @Test

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
    
    @Test

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
    
    @Test

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
    
    @Test

    public void testConvertFromNamespaceForm1() throws Exception
    {
        testConvertFromNamespaceForm
        (
            "ConvertFromNamespaceForm-1-Initial.txt",
            "ConvertFromNamespaceForm-1-Expected.txt"
        );
    }
    
    @Test

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
        
        final String actual = new String( byteArrayResourceStore.getContents(), UTF8 );
        
        assertEqualsIgnoreNewLineDiffs( expected, actual );
    }
    
    private static abstract class ConvertOp
    {
        public abstract void run( Document document );
    }
    
}
