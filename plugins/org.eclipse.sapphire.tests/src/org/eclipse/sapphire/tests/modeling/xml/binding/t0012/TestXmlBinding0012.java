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
import org.eclipse.sapphire.modeling.ModelElementList;
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
        suite.addTest( new TestXmlBinding0012( "testMoveUp" ) );
        suite.addTest( new TestXmlBinding0012( "testMoveDown" ) );
        
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
            final ModelElementList<TestListEntry> list = element.getList();
            
            final TestListEntry x = list.insert();
            x.setValue( "x" );
            
            final TestListEntry y = list.insert();
            y.setValue( "y" );
        }
        finally
        {
            element.dispose();
        }
    }

    public void testMoveUp() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( resourceStore );
        final TestElement element = TestElement.TYPE.instantiate(  new RootXmlResource( xmlResourceStore ) );
        
        try
        {
            final ModelElementList<TestListEntry> list = element.getList();
            
            final TestListEntry x = list.insert();
            x.setValue( "x" );
            
            final TestListEntry y = list.insert();
            y.setValue( "y" );

            final TestListEntry z = list.insert();
            z.setValue( "z" );
            
            list.moveUp( z );
            
            assertEquals( 3, list.size() );
            assertSame( x, list.get( 0 ) );
            assertEquals( x.getValue().getText(), "x" );
            assertSame( z, list.get( 1 ) );
            assertEquals( z.getValue().getText(), "z" );
            assertSame( y, list.get( 2 ) );
            assertEquals( y.getValue().getText(), "y" );
            
            list.moveUp( z );
            
            assertEquals( 3, list.size() );
            assertSame( z, list.get( 0 ) );
            assertEquals( z.getValue().getText(), "z" );
            assertSame( x, list.get( 1 ) );
            assertEquals( x.getValue().getText(), "x" );
            assertSame( y, list.get( 2 ) );
            assertEquals( y.getValue().getText(), "y" );
        }
        finally
        {
            element.dispose();
        }
    }
    
    public void testMoveDown() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( resourceStore );
        final TestElement element = TestElement.TYPE.instantiate(  new RootXmlResource( xmlResourceStore ) );
        
        try
        {
            final ModelElementList<TestListEntry> list = element.getList();
            
            final TestListEntry x = list.insert();
            x.setValue( "x" );
            
            final TestListEntry y = list.insert();
            y.setValue( "y" );

            final TestListEntry z = list.insert();
            z.setValue( "z" );
            
            list.moveDown( x );
            
            assertEquals( 3, list.size() );
            assertSame( y, list.get( 0 ) );
            assertEquals( y.getValue().getText(), "y" );
            assertSame( x, list.get( 1 ) );
            assertEquals( x.getValue().getText(), "x" );
            assertSame( z, list.get( 2 ) );
            assertEquals( z.getValue().getText(), "z" );
            
            list.moveDown( x );
            
            assertEquals( 3, list.size() );
            assertSame( y, list.get( 0 ) );
            assertEquals( y.getValue().getText(), "y" );
            assertSame( z, list.get( 1 ) );
            assertEquals( z.getValue().getText(), "z" );
            assertSame( x, list.get( 2 ) );
            assertEquals( x.getValue().getText(), "x" );
        }
        finally
        {
            element.dispose();
        }
    }
    
}
