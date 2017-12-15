/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.xsd.t0001;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.modeling.xml.schema.XmlContentModel;
import org.eclipse.sapphire.modeling.xml.schema.XmlDocumentSchema;
import org.eclipse.sapphire.modeling.xml.schema.XmlDocumentSchemasCache;
import org.eclipse.sapphire.modeling.xml.schema.XmlElementDefinition;
import org.eclipse.sapphire.modeling.xml.schema.XmlSequenceGroup;

/**
 * Tests handling of XML Schema redefine directive.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestXmlXsd0001

    extends TestCase
    
{
    private TestXmlXsd0001( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestXmlXsd001" );

        suite.addTest( new TestXmlXsd0001( "testSchemaParsing" ) );
        suite.addTest( new TestXmlXsd0001( "testInsertOrder" ) );
        
        return suite;
    }
    
    public void testSchemaParsing() throws Exception
    {
        final XmlDocumentSchema schema = XmlDocumentSchemasCache.getSchema( "http://www.eclipse.org/sapphire/tests/xml/xsd/0001", null );
        
        final XmlElementDefinition rootElementDef = schema.getElement( "root" );
        final XmlSequenceGroup rootContentModel = (XmlSequenceGroup) rootElementDef.getContentModel();
        final List<XmlContentModel> nestedContent = rootContentModel.getNestedContent();
        
        assertEquals( 4, nestedContent.size() );
        assertEquals( "aaa", ( (XmlElementDefinition) nestedContent.get( 0 ) ).getName().getLocalPart() );
        assertEquals( "bbb", ( (XmlElementDefinition) nestedContent.get( 1 ) ).getName().getLocalPart() );
        assertEquals( "ccc", ( (XmlElementDefinition) nestedContent.get( 2 ) ).getName().getLocalPart() );
        assertEquals( "ddd", ( (XmlElementDefinition) nestedContent.get( 3 ) ).getName().getLocalPart() );
    }
    
    public void testInsertOrder() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final ITestXmlXsd0001ModelRoot model = ITestXmlXsd0001ModelRoot.TYPE.instantiate( new RootXmlResource( new XmlResourceStore( resourceStore ) ) );
        
        model.setDdd( "ddd" );
        model.setCcc( "ccc" );
        model.setBbb( "bbb" );
        model.setAaa( "aaa" );
        
        model.resource().save();
        
        final String result = new String( resourceStore.getContents(), "UTF-8" );
        
        assertEqualsIgnoreNewLineDiffs( loadResource( "0001.txt" ), result );
    }
    
    private InputStream loadResourceAsStream( final String name )
    {
        final InputStream in = getClass().getResourceAsStream( name );
        
        if( in == null )
        {
            throw new IllegalArgumentException( name );
        }
        
        return in;
    }
    
    private String loadResource( final String name )
    
        throws Exception
        
    {
        final InputStream in = loadResourceAsStream( name );
        
        try
        {
            final BufferedReader r = new BufferedReader( new InputStreamReader( in ) );
            final char[] chars = new char[ 1024 ];
            final StringBuilder buf = new StringBuilder();
            
            for( int i = r.read( chars ); i != -1; i = r.read( chars ) )
            {
                buf.append( chars, 0, i );
            }
            
            return buf.toString();
        }
        finally
        {
            try
            {
                in.close();
            }
            catch( IOException e ) {}
        }
    }
    
    private static void assertEqualsIgnoreNewLineDiffs( final String expected, 
                                                        final String actual ) 
    {
        assertEquals( expected.replace( "\r", "" ), actual.replace( "\r", "" ) );
    }
    
}
