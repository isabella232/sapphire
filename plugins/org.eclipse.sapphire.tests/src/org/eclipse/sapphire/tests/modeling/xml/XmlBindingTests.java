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

import org.eclipse.sapphire.modeling.ByteArrayModelStore;
import org.eclipse.sapphire.modeling.CorruptedModelStoreExceptionInterceptor;
import org.eclipse.sapphire.modeling.xml.ModelStoreForXml;
import org.eclipse.sapphire.tests.modeling.xml.internal.XmlBindingTestModel;
import org.eclipse.sapphire.tests.modeling.xml.internal.XmlBindingTestModelAltB;
import org.eclipse.sapphire.tests.modeling.xml.internal.XmlBindingTestModelAltC;

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
        
        suite.setName( "XmlBindingTests" );

        suite.addTest( new XmlBindingTests( "testValueProperties1" ) );
        suite.addTest( new XmlBindingTests( "testValueProperties2" ) );
        suite.addTest( new XmlBindingTests( "testValueProperties3" ) );
        
        return suite;
    }
    
    public void testValueProperties1() throws Exception
    {
        final ByteArrayModelStore modelStore = new ByteArrayModelStore();
        final ModelStoreForXml xmlModelStore = new ModelStoreForXml( modelStore );
        final IXmlBindingTestModel model = new XmlBindingTestModel( xmlModelStore );
        
        testValueProperties( modelStore, model, loadResource( "testValueProperties1.txt" ) );
    }
    
    public void testValueProperties2() throws Exception
    {
        final ByteArrayModelStore modelStore = new ByteArrayModelStore();
        final ModelStoreForXml xmlModelStore = new ModelStoreForXml( modelStore );
        final IXmlBindingTestModel model = new XmlBindingTestModelAltB( xmlModelStore );
        
        testValueProperties( modelStore, model, loadResource( "testValueProperties2.txt" ) );
    }
    
    public void testValueProperties3() throws Exception
    {
        final ByteArrayModelStore modelStore = new ByteArrayModelStore();
        final ModelStoreForXml xmlModelStore = new ModelStoreForXml( modelStore );
        final IXmlBindingTestModel model = new XmlBindingTestModelAltC( xmlModelStore );
        
        testValueProperties( modelStore, model, loadResource( "testValueProperties3.txt" ) );
    }

    private void testValueProperties( final ByteArrayModelStore modelStore,
                                      final IXmlBindingTestModel model,
                                      final String expected )
    
        throws Exception
        
    {
        model.setCorruptedModelStoreExceptionInterceptor
        (
             new CorruptedModelStoreExceptionInterceptor()
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
        model.setValuePropertyC( "cccc" );
        model.setValuePropertyD( "dddd" );
        model.setValuePropertyE( "eeee" );
        model.setValuePropertyF( "ffff" );
        
        model.save();
        
        final String result = new String( modelStore.getContents(), "UTF-8" );
        
        assertEqualsIgnoreNewLineDiffs( expected, result );
    }
    
    private String loadResource( final String name )
    
        throws Exception
        
    {
        final InputStream in = getClass().getResourceAsStream( "XmlBindingTests." + name );
        
        if( in == null )
        {
            throw new IllegalArgumentException( name );
        }
        
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
