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
        suite.addTest( XmlBindingTestSuite.suite() );
        suite.addTest( XmlDtdTestSuite.suite() );
        suite.addTest( XmlXsdTestSuite.suite() );
        
        return suite;
    }
    
    public void testValueProperties1() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( resourceStore );
        final XmlBindingTestModel model = XmlBindingTestModel.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
        testValueProperties( resourceStore, model, loadResource( "testValueProperties1.txt" ) );
    }
    
    public void testValueProperties2() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( resourceStore );
        final XmlBindingTestModelAltB model = XmlBindingTestModelAltB.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
        testValueProperties( resourceStore, model, loadResource( "testValueProperties2.txt" ) );
    }
    
    public void testValueProperties3() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( resourceStore );
        final XmlBindingTestModelAltC model = XmlBindingTestModelAltC.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
        testValueProperties( resourceStore, model, loadResource( "testValueProperties3.txt" ) );
    }

    private void testValueProperties( final ByteArrayResourceStore resourceStore,
                                      final XmlBindingTestModel model,
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
