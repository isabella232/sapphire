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

package org.eclipse.sapphire.tests.modeling;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.sapphire.modeling.xml.schema.XmlChoiceGroup;
import org.eclipse.sapphire.modeling.xml.schema.XmlContentModel;
import org.eclipse.sapphire.modeling.xml.schema.XmlElementDefinition;
import org.eclipse.sapphire.modeling.xml.schema.XmlSequenceGroup;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class FindInsertionPositionTests extends SapphireTestCase
{
    @Test
    
    public void test_1A() throws Exception
    {
        final XmlContentModel XmlContentModel = 
            
            sequence
            (
                1, 1,
                element( "A", 0, -1 ),
                element( "B", 0, -1 ),
                element( "C", 0, -1 )
            ).create( null );
        
        final String start = "<root></root>";
        final String expected = "<root><A/></root>";
        
        test( XmlContentModel, start, "A", expected );
    }
    
    @Test

    public void test_1B() throws Exception
    {
        final XmlContentModel XmlContentModel = 
            
            sequence
            (
                1, 1,
                element( "A", 0, -1 ),
                element( "B", 0, -1 ),
                element( "C", 0, -1 )
            ).create( null );
        
        final String start = "<root></root>";
        final String expected = "<root><B/></root>";
        
        test( XmlContentModel, start, "B", expected );
    }

    @Test
    
    public void test_1C() throws Exception
    {
        final XmlContentModel XmlContentModel = 
            
            sequence
            (
                1, 1,
                element( "A", 0, -1 ),
                element( "B", 0, -1 ),
                element( "C", 0, -1 )
            ).create( null );
        
        final String start = "<root><A/><A/><B/><B/><C/><C/></root>";
        final String expected = "<root><A/><A/><A/><B/><B/><C/><C/></root>";
        
        test( XmlContentModel, start, "A", expected );
    }
    
    @Test

    public void test_1D() throws Exception
    {
        final XmlContentModel XmlContentModel = 
            
            sequence
            (
                1, 1,
                element( "A", 0, -1 ),
                element( "B", 0, -1 ),
                element( "C", 0, -1 )
            ).create( null );
        
        final String start = "<root><A/><A/><B/><B/><C/><C/></root>";
        final String expected = "<root><A/><A/><B/><B/><B/><C/><C/></root>";
        
        test( XmlContentModel, start, "B", expected );
    }
    
    @Test

    public void test_1E() throws Exception
    {
        final XmlContentModel XmlContentModel = 
            
            sequence
            (
                1, 1,
                element( "A", 0, -1 ),
                element( "B", 0, -1 ),
                element( "C", 0, -1 )
            ).create( null );
        
        final String start = "<root><A/><A/><B/><B/><C/><C/></root>";
        final String expected = "<root><A/><A/><B/><B/><C/><C/><C/></root>";
        
        test( XmlContentModel, start, "C", expected );
    }
    
    @Test

    public void test_2A() throws Exception
    {
        final XmlContentModel XmlContentModel = 
            
            choice
            (
                0, -1,
                element( "A", 1, 1 ),
                element( "B", 1, 1 ),
                element( "C", 1, 1 )
            ).create( null );
        
        final String start = "<root><A/><A/><B/><A/><C/><B/></root>";
        final String expected = "<root><A/><A/><B/><A/><C/><B/><A/></root>";
        
        test( XmlContentModel, start, "A", expected );
    }
    
    @Test

    public void test_2B() throws Exception
    {
        final XmlContentModel XmlContentModel = 
            
            choice
            (
                0, -1,
                element( "A", 1, 1 ),
                element( "B", 1, 1 ),
                element( "C", 1, 1 )
            ).create( null );
        
        final String start = "<root><A/><A/><B/><A/><C/><B/></root>";
        final String expected = "<root><A/><A/><B/><A/><C/><B/><B/></root>";
        
        test( XmlContentModel, start, "B", expected );
    }
    
    @Test

    public void test_2C() throws Exception
    {
        final XmlContentModel XmlContentModel = 
            
            choice
            (
                0, -1,
                element( "A", 1, 1 ),
                element( "B", 1, 1 ),
                element( "C", 1, 1 )
            ).create( null );
        
        final String start = "<root><A/><A/><B/><A/><C/><B/></root>";
        final String expected = "<root><A/><A/><B/><A/><C/><B/><C/></root>";
        
        test( XmlContentModel, start, "C", expected );
    }
    
    @Test

    public void test_3A() throws Exception
    {
        final XmlContentModel XmlContentModel = 
            
            sequence
            (
                1, 1,
                element( "A", 1, 1 ),
                element( "B", 0, -1 ),
                choice
                (
                    0, -1,
                    element( "C", 1, 1 ),
                    element( "D", 0, -1 )
                ),
                element( "E", 1, 1 )
            ).create( null );
        
        final String start =
            
            "<root>\n" +
            "  <A/>\n" +
            "  <B/>\n" +
            "  <B/>\n" +
            "  <C/>\n" +
            "  <D/>\n" +
            "  <D/>\n" +
            "  <D/>\n" +
            "  <C/>\n" +
            "  <E/>\n" +
            "</root>";

        final String res1 =
            
            "<root>\n" +
            "  <A/>\n" +
            "  <B/>\n" +
            "  <B/>\n" +
            "  <B/>\n" +
            "  <C/>\n" +
            "  <D/>\n" +
            "  <D/>\n" +
            "  <D/>\n" +
            "  <C/>\n" +
            "  <E/>\n" +
            "</root>";
        
        test( XmlContentModel, start, "B", res1 );
    }
    
    @Test
 
    public void test_3B() throws Exception
    {
        final XmlContentModel XmlContentModel = 
            
            sequence
            (
                1, 1,
                element( "A", 1, 1 ),
                element( "B", 0, -1 ),
                choice
                (
                    0, -1,
                    element( "C", 1, 1 ),
                    element( "D", 0, -1 )
                ),
                element( "E", 1, 1 )
            ).create( null );
        
        final String start =
            
            "<root>\n" +
            "  <A/>\n" +
            "  <B/>\n" +
            "  <B/>\n" +
            "  <C/>\n" +
            "  <D/>\n" +
            "  <D/>\n" +
            "  <D/>\n" +
            "  <C/>\n" +
            "  <E/>\n" +
            "</root>";
    
        final String res2 =
            
            "<root>\n" +
            "  <A/>\n" +
            "  <B/>\n" +
            "  <B/>\n" +
            "  <C/>\n" +
            "  <D/>\n" +
            "  <D/>\n" +
            "  <D/>\n" +
            "  <C/>\n" +
            "  <C/>\n" +
            "  <E/>\n" +
            "</root>";
        
        test( XmlContentModel, start, "C", res2 );
    }
    
    @Test

    public void test_3C() throws Exception
    {
        final XmlContentModel XmlContentModel = 
            
            sequence
            (
                1, 1,
                element( "A", 1, 1 ),
                element( "B", 0, -1 ),
                choice
                (
                    0, -1,
                    element( "C", 1, 1 ),
                    element( "D", 0, -1 )
                ),
                element( "E", 1, 1 )
            ).create( null );
        
        final String start =
            
            "<root>\n" +
            "  <A/>\n" +
            "  <B/>\n" +
            "  <B/>\n" +
            "  <C/>\n" +
            "  <D/>\n" +
            "  <D/>\n" +
            "  <D/>\n" +
            "  <C/>\n" +
            "  <E/>\n" +
            "</root>";
    
        final String res3 =
            
            "<root>\n" +
            "  <A/>\n" +
            "  <B/>\n" +
            "  <B/>\n" +
            "  <C/>\n" +
            "  <D/>\n" +
            "  <D/>\n" +
            "  <D/>\n" +
            "  <D/>\n" +
            "  <C/>\n" +
            "  <E/>\n" +
            "</root>";
        
        // ABBCDDDCE -> ABBCDDDDCE
        
        test( XmlContentModel, start, "D", res3 );
    }
    
    @Test

    public void test_3D() throws Exception
    {
        final XmlContentModel XmlContentModel = 
            
            sequence
            (
                1, 1,
                element( "A", 1, 1 ),
                element( "B", 0, -1 ),
                choice
                (
                    0, -1,
                    element( "C", 1, 1 ),
                    element( "D", 0, -1 )
                ),
                element( "E", 1, 1 )
            ).create( null );
        
        final String start =
            
            "<root>\n" +
            "  <A/>\n" +
            "  <B/>\n" +
            "  <B/>\n" +
            "  <C/>\n" +
            "  <D/>\n" +
            "  <D/>\n" +
            "  <D/>\n" +
            "  <C/>\n" +
            "  <E/>\n" +
            "</root>";
    
        final String res4 =
            
            "<root>\n" +
            "  <A/>\n" +
            "  <B/>\n" +
            "  <B/>\n" +
            "  <C/>\n" +
            "  <D/>\n" +
            "  <D/>\n" +
            "  <D/>\n" +
            "  <C/>\n" +
            "  <E/>\n" +
            "  <E/>\n" +
            "</root>";
        
        test( XmlContentModel, start, "E", res4 );
    }
    
    @Test

    public void test_4A() throws Exception
    {
        final XmlContentModel XmlContentModel = 
            
            sequence
            (
                1, 1,
                element( "A", 1, 1 ),
                element( "B", 1, 1 ),
                element( "C", 1, 1 ),
                sequence
                (
                    1, -1,
                    element( "D", 1, 1 ),
                    element( "B", 0, -1 ),
                    element( "E", 1, 1 )
                )
            ).create( null );
        
        // AC -> ACB
        
        final String start = "<root><A/><C/></root>";
        final String expected = "<root><A/><C/><B/></root>";
            
        test( XmlContentModel, start, "B", expected );
    }
    
    @Test

    public void test_4B() throws Exception
    {
        final XmlContentModel XmlContentModel = 
            
            sequence
            (
                1, 1,
                element( "A", 1, 1 ),
                element( "B", 1, 1 ),
                element( "C", 1, 1 ),
                sequence
                (
                    1, -1,
                    element( "D", 1, 1 ),
                    element( "B", 0, -1 ),
                    element( "E", 1, 1 )
                )
            ).create( null );
        
        // ACDBBE  -> ABCDBBE
        
        final String start = "<root><A/><C/><D/><B/><B/><E/></root>";
        final String expected = "<root><A/><B/><C/><D/><B/><B/><E/></root>";
            
        test( XmlContentModel, start, "B", expected );
    }
    
    @Test

    public void test_4C() throws Exception
    {
        final XmlContentModel XmlContentModel = 
            
            sequence
            (
                1, 1,
                element( "A", 1, 1 ),
                element( "B", 1, 1 ),
                element( "C", 1, 1 ),
                sequence
                (
                    1, -1,
                    element( "D", 1, 1 ),
                    element( "B", 0, -1 ),
                    element( "E", 1, 1 )
                )
            ).create( null );
        
        // ABCDBBE -> ABCDBBBE
        
        final String start = "<root><A/><B/><C/><D/><B/><B/><E/></root>";
        final String expected = "<root><A/><B/><C/><D/><B/><B/><B/><E/></root>";
            
        test( XmlContentModel, start, "B", expected );
    }
    
    @Test

    public void test_5A() throws Exception
    {
        final XmlContentModel XmlContentModel = 
            
            sequence
            (
                1, 1,
                element( "A", 1, 1 ),
                element( "X", 1, 1 ),
                element( "B", 0, -1 ),
                choice
                (
                    0, -1,
                    sequence
                    (
                        1, 1,
                        element( "D", 1, 1 ),
                        element( "X", 0, 1 ),
                        element( "C", 1, 1 )
                    ),
                    sequence
                    (
                        1, 1,
                        element( "X", 0, 1 ),
                        element( "D", 0, -1 ),
                        element( "E", 1, 1 )
                    )
                )
            ).create( null );
        
        // ABDC -> AXBDC
        
        final String start = "<root><A/><B/><D/><C/></root>";
        final String expected = "<root><A/><X/><B/><D/><C/></root>";
            
        test( XmlContentModel, start, "X", expected );
    }
    
    @Test
    
    public void test_5B() throws Exception
    {
        final XmlContentModel XmlContentModel = 
            
            sequence
            (
                1, 1,
                element( "A", 1, 1 ),
                element( "X", 1, 1 ),
                element( "B", 0, -1 ),
                choice
                (
                    0, -1,
                    sequence
                    (
                        1, 1,
                        element( "D", 1, 1 ),
                        element( "X", 0, 1 ),
                        element( "C", 1, 1 )
                    ),
                    sequence
                    (
                        1, 1,
                        element( "X", 0, 1 ),
                        element( "D", 0, -1 ),
                        element( "E", 1, 1 )
                    )
                )
            ).create( null );
        
        // AXBDC -> AXBDXC
        
        final String start = "<root><A/><X/><B/><D/><C/></root>";
        final String expected = "<root><A/><X/><B/><D/><X/><C/></root>";
            
        test( XmlContentModel, start, "X", expected );
    }
    
    @Test
    
    public void test_5C() throws Exception
    {
        final XmlContentModel XmlContentModel = 
            
            sequence
            (
                1, 1,
                element( "A", 1, 1 ),
                element( "X", 1, 1 ),
                element( "B", 0, -1 ),
                choice
                (
                    0, -1,
                    sequence
                    (
                        1, 1,
                        element( "D", 1, 1 ),
                        element( "X", 0, 1 ),
                        element( "C", 1, 1 )
                    ),
                    sequence
                    (
                        1, 1,
                        element( "X", 0, 1 ),
                        element( "D", 0, -1 ),
                        element( "E", 1, 1 )
                    )
                )
            ).create( null );
        
        // AXBDE -> AXBXDE
        
        final String start = "<root><A/><X/><B/><D/><E/></root>";
        final String expected = "<root><A/><X/><B/><X/><D/><E/></root>";
            
        test( XmlContentModel, start, "X", expected );
    }
    
    private static void test( final XmlContentModel XmlContentModel,
                              final String initialContent,
                              final String elementNameToInsert,
                              final String expectedContent )
    
        throws Exception
        
    {
        Element root = parse( initialContent );
        NodeList nodeList = root.getChildNodes();
        
        final int position = XmlContentModel.findInsertionPosition( nodeList, new QName( elementNameToInsert ) );
        
        final Element elementToInsert = root.getOwnerDocument().createElementNS( null, elementNameToInsert );
        
        if( position == -1 )
        {
            root.appendChild( elementToInsert );
        }
        else
        {
            final Node nodeAtPosition = nodeList.item( position );
            root.insertBefore( elementToInsert, nodeAtPosition );
        }
        
        final Element expectedContentRoot = parse( expectedContent );
        
        if( ! equal( nodeList, expectedContentRoot.getChildNodes() ) )
        {
            final StringBuilder buf = new StringBuilder();
            buf.append( "=== actual ===\n" );
            buf.append( toString( root.getOwnerDocument() ) );
            buf.append( "\n=== expected ===\n" );
            buf.append( expectedContent );
            
            assertTrue( buf.toString(), false );
        }
    }
    
    private static XmlChoiceGroup.Factory choice( final int minOccur,
                                                  final int maxOccur,
                                                  final XmlContentModel.Factory... list )
    {
        final XmlChoiceGroup.Factory factory = new XmlChoiceGroup.Factory();
        
        factory.setMinOccur( minOccur );
        factory.setMaxOccur( maxOccur );
        
        for( XmlContentModel.Factory child : list )
        {
            factory.addNestedContent( child );
        }
        
        return factory;
    }

    private static XmlSequenceGroup.Factory sequence( final int minOccur,
                                                      final int maxOccur,
                                                      final XmlContentModel.Factory... list )
    {
        final XmlSequenceGroup.Factory factory = new XmlSequenceGroup.Factory();
        
        factory.setMinOccur( minOccur );
        factory.setMaxOccur( maxOccur );
        
        for( XmlContentModel.Factory child : list )
        {
            factory.addNestedContent( child );
        }
        
        return factory;
    }
    
    private static XmlElementDefinition.Factory element( final String name,
                                                         final int minOccur,
                                                         final int maxOccur )
    {
        final XmlElementDefinition.Factory factory = new XmlElementDefinition.Factory();
        
        factory.setName( name );
        factory.setMinOccur( minOccur );
        factory.setMaxOccur( maxOccur );
        
        return factory;
    }
    
    private static Element parse( final String content )
    
        throws Exception
        
    {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware( true );
        final DocumentBuilder docbuilder = factory.newDocumentBuilder();
        final Document doc = docbuilder.parse( new InputSource( new StringReader( content ) ) );
        return doc.getDocumentElement();
    }
    
    private static boolean equal( final NodeList xList,
                                  final NodeList yList )
    {
        int x = 0;
        int y = 0;
        int xLen = xList.getLength();
        int yLen = yList.getLength();
        
        while( x < xLen && y < yLen )
        {
            for( int i = x; i < xLen; i++ )
            {
                if( xList.item( i ).getNodeType() != Node.ELEMENT_NODE )
                {
                    x++;
                }
                else
                {
                    break;
                }
            }
            
            for( int i = y; i < yLen; i++ )
            {
                if( yList.item( i ).getNodeType() != Node.ELEMENT_NODE )
                {
                    y++;
                }
                else
                {
                    break;
                }
            }
            
            if( x < xLen && y < yLen )
            {
                final Node xnode = xList.item( x );
                final Node ynode = yList.item( y );
                
                if( xnode.getLocalName().equals( ynode.getLocalName() ) )
                {
                    x++;
                    y++;
                }
                else
                {
                    return false;
                }
            }
        }
        
        for( int i = x; i < xLen; i++ )
        {
            if( xList.item( i ).getNodeType() == Node.ELEMENT_NODE )
            {
                return false;
            }
        }
        
        for( int i = y; i < yLen; i++ )
        {
            if( yList.item( i ).getNodeType() == Node.ELEMENT_NODE )
            {
                return false;
            }
        }
        
        return true;
    }

    private static String toString( final Document doc )
    
        throws Exception
        
    {
        final DOMSource source = new DOMSource( doc );
        final StringWriter sw = new StringWriter();
        final StreamResult result = new StreamResult( sw );
        
        final TransformerFactory factory = TransformerFactory.newInstance();
        final Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
        
        transformer.transform( source, result );
        
        return sw.toString();
    }
    
}
