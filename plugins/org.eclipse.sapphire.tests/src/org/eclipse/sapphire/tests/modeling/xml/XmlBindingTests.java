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

package org.eclipse.sapphire.tests.modeling.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.CorruptedResourceExceptionInterceptor;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.modeling.xml.binding.XmlBindingTestSuite;
import org.eclipse.sapphire.tests.modeling.xml.dtd.XmlDtdTestSuite;
import org.eclipse.sapphire.tests.modeling.xml.xsd.XmlXsdTestSuite;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class XmlBindingTests

    extends TestCase
    
{
    private XmlBindingTests( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "Xml" );

        suite.addTest( new XmlBindingTests( "testValueProperties1" ) );
        suite.addTest( new XmlBindingTests( "testValueProperties2" ) );
        suite.addTest( new XmlBindingTests( "testValueProperties3" ) );
        suite.addTest( new XmlBindingTests( "testDefaultBindingWrite" ) );
        suite.addTest( new XmlBindingTests( "testDefaultBindingRead" ) );
        suite.addTest( XmlBindingTestSuite.suite() );
        suite.addTest( XmlDtdTestSuite.suite() );
        suite.addTest( XmlXsdTestSuite.suite() );
        
        return suite;
    }
    
    public void testValueProperties1() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( resourceStore );
        final IXmlBindingTestModel model = IXmlBindingTestModel.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
        testValueProperties( resourceStore, model, loadResource( "testValueProperties1.txt" ) );
    }
    
    public void testValueProperties2() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( resourceStore );
        final IXmlBindingTestModelAltB model = IXmlBindingTestModelAltB.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
        testValueProperties( resourceStore, model, loadResource( "testValueProperties2.txt" ) );
    }
    
    public void testValueProperties3() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( resourceStore );
        final IXmlBindingTestModelAltC model = IXmlBindingTestModelAltC.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
        testValueProperties( resourceStore, model, loadResource( "testValueProperties3.txt" ) );
    }

    private void testValueProperties( final ByteArrayResourceStore resourceStore,
                                      final IXmlBindingTestModel model,
                                      final String expected )
    
        throws Exception
        
    {
        model.resource().setCorruptedResourceExceptionInterceptor
        (
             new CorruptedResourceExceptionInterceptor()
             {
                @Override
                public boolean shouldAttemptRepair()
                {
                    return true;
                }
             }
        );
        
        model.setValuePropertyA( "aaaa" );
        assertEquals( "aaaa", model.getValuePropertyA().getText() );
        
        model.setValuePropertyB( "bbbb" );
        assertEquals( "bbbb", model.getValuePropertyB().getText() );
        
        model.setValuePropertyC( "cccc" );
        assertEquals( "cccc", model.getValuePropertyC().getText() );
        
        model.setValuePropertyD( "dddd" );
        assertEquals( "dddd", model.getValuePropertyD().getText() );
        
        model.setValuePropertyE( "eeee" );
        assertEquals( "eeee", model.getValuePropertyE().getText() );
        
        model.setValuePropertyF( "ffff" );
        assertEquals( "ffff", model.getValuePropertyF().getText() );
        
        model.resource().save();
        
        final String result = new String( resourceStore.getContents(), "UTF-8" );
        
        assertEqualsIgnoreNewLineDiffs( expected, result );
    }
    
    public void testDefaultBindingWrite() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( resourceStore );
        final IDefaultXmlBindingTestModel model = IDefaultXmlBindingTestModel.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
        model.resource().setCorruptedResourceExceptionInterceptor
        (
             new CorruptedResourceExceptionInterceptor()
             {
                @Override
                public boolean shouldAttemptRepair()
                {
                    return true;
                }
             }
        );
        
        model.setValuePropertyA( "aaaa" );
        model.setValuePropertyB( "bbbb" );
        
        final IDefaultXmlBindingTestModelChildA listChild1 = (IDefaultXmlBindingTestModelChildA) model.getListPropertyA().addNewElement( IDefaultXmlBindingTestModelChildA.TYPE );
        listChild1.setValuePropertyA( "cccc" );
        
        final IDefaultXmlBindingTestModelChildB listChild2 = (IDefaultXmlBindingTestModelChildB) model.getListPropertyA().addNewElement( IDefaultXmlBindingTestModelChildB.TYPE );
        listChild2.setValuePropertyB( "dddd" );
        
        final IDefaultXmlBindingTestModelChildA listChild3 = (IDefaultXmlBindingTestModelChildA) model.getListPropertyA().addNewElement( IDefaultXmlBindingTestModelChildA.TYPE );
        listChild3.setValuePropertyA( "eeee" );
        
        final IDefaultXmlBindingTestModelChildB elementChild = (IDefaultXmlBindingTestModelChildB) model.getElementPropertyA().element( true, IDefaultXmlBindingTestModelChildB.TYPE );
        elementChild.setValuePropertyB( "ffff" );
        
        model.resource().save();
        
        final String result = new String( resourceStore.getContents(), "UTF-8" );
        
        assertEqualsIgnoreNewLineDiffs( loadResource( "testDefaultBinding.txt" ), result );
    }
    
    public void testDefaultBindingRead() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore( loadResourceAsStream( "testDefaultBinding.txt" ) );
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( resourceStore );
        final IDefaultXmlBindingTestModel model = IDefaultXmlBindingTestModel.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
        assertEquals( "aaaa", model.getValuePropertyA().getText() );
        assertEquals( "bbbb", model.getValuePropertyB().getText() );
        
        assertEquals( 3, model.getListPropertyA().size() );
        assertEquals( "cccc", ( (IDefaultXmlBindingTestModelChildA) model.getListPropertyA().get( 0 ) ).getValuePropertyA().getText() );
        assertEquals( "dddd", ( (IDefaultXmlBindingTestModelChildB) model.getListPropertyA().get( 1 ) ).getValuePropertyB().getText() );
        assertEquals( "eeee", ( (IDefaultXmlBindingTestModelChildA) model.getListPropertyA().get( 2 ) ).getValuePropertyA().getText() );
        
        assertEquals( "ffff", ( (IDefaultXmlBindingTestModelChildB) model.getElementPropertyA().element( false ) ).getValuePropertyB().getText() );
    }
    
    private InputStream loadResourceAsStream( final String name )
    {
        final InputStream in = getClass().getResourceAsStream( "XmlBindingTests." + name );
        
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
