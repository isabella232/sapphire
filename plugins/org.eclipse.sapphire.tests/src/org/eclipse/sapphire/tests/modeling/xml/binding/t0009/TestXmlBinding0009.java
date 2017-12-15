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

package org.eclipse.sapphire.tests.modeling.xml.binding.t0009;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.CorruptedResourceExceptionInterceptor;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests default XML binding. The test is repeated with and without the convention of using 'I' prefix
 * on model element interfaces.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestXmlBinding0009

    extends SapphireTestCase
    
{
    private TestXmlBinding0009( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestXmlBinding0009" );

        suite.addTest( new TestXmlBinding0009( "testDefaultBindingWrite1" ) );
        suite.addTest( new TestXmlBinding0009( "testDefaultBindingWrite2" ) );
        suite.addTest( new TestXmlBinding0009( "testDefaultBindingRead1" ) );
        suite.addTest( new TestXmlBinding0009( "testDefaultBindingRead2" ) );
        
        return suite;
    }
    
    public void testDefaultBindingWrite1() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( resourceStore );
        final TestRootElement model = TestRootElement.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
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
        
        final TestChildElementA listChild1 = (TestChildElementA) model.getListPropertyA().insert( TestChildElementA.TYPE );
        listChild1.setValuePropertyA( "cccc" );
        
        final TestChildElementB listChild2 = (TestChildElementB) model.getListPropertyA().insert( TestChildElementB.TYPE );
        listChild2.setValuePropertyB( "dddd" );
        
        final TestChildElementA listChild3 = (TestChildElementA) model.getListPropertyA().insert( TestChildElementA.TYPE );
        listChild3.setValuePropertyA( "eeee" );
        
        final TestChildElementB elementChild = (TestChildElementB) model.getElementPropertyA().element( true, TestChildElementB.TYPE );
        elementChild.setValuePropertyB( "ffff" );
        
        model.resource().save();
        
        final String result = new String( resourceStore.getContents(), "UTF-8" );
        
        assertEqualsIgnoreNewLineDiffs( loadResource( "TestData.txt" ), result );
    }
    
    public void testDefaultBindingWrite2() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( resourceStore );
        final ITestRootElement model = ITestRootElement.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
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
        
        final ITestChildElementA listChild1 = (ITestChildElementA) model.getListPropertyA().insert( ITestChildElementA.TYPE );
        listChild1.setValuePropertyA( "cccc" );
        
        final ITestChildElementB listChild2 = (ITestChildElementB) model.getListPropertyA().insert( ITestChildElementB.TYPE );
        listChild2.setValuePropertyB( "dddd" );
        
        final ITestChildElementA listChild3 = (ITestChildElementA) model.getListPropertyA().insert( ITestChildElementA.TYPE );
        listChild3.setValuePropertyA( "eeee" );
        
        final ITestChildElementB elementChild = (ITestChildElementB) model.getElementPropertyA().element( true, ITestChildElementB.TYPE );
        elementChild.setValuePropertyB( "ffff" );
        
        model.resource().save();
        
        final String result = new String( resourceStore.getContents(), "UTF-8" );
        
        assertEqualsIgnoreNewLineDiffs( loadResource( "TestData.txt" ), result );
    }
    
    public void testDefaultBindingRead1() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore( loadResourceAsStream( "TestData.txt" ) );
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( resourceStore );
        final TestRootElement model = TestRootElement.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
        assertEquals( "aaaa", model.getValuePropertyA().getText() );
        assertEquals( "bbbb", model.getValuePropertyB().getText() );
        
        assertEquals( 3, model.getListPropertyA().size() );
        assertEquals( "cccc", ( (TestChildElementA) model.getListPropertyA().get( 0 ) ).getValuePropertyA().getText() );
        assertEquals( "dddd", ( (TestChildElementB) model.getListPropertyA().get( 1 ) ).getValuePropertyB().getText() );
        assertEquals( "eeee", ( (TestChildElementA) model.getListPropertyA().get( 2 ) ).getValuePropertyA().getText() );
        
        assertEquals( "ffff", ( (TestChildElementB) model.getElementPropertyA().element( false ) ).getValuePropertyB().getText() );
    }

    public void testDefaultBindingRead2() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore( loadResourceAsStream( "TestData.txt" ) );
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( resourceStore );
        final ITestRootElement model = ITestRootElement.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
        assertEquals( "aaaa", model.getValuePropertyA().getText() );
        assertEquals( "bbbb", model.getValuePropertyB().getText() );
        
        assertEquals( 3, model.getListPropertyA().size() );
        assertEquals( "cccc", ( (ITestChildElementA) model.getListPropertyA().get( 0 ) ).getValuePropertyA().getText() );
        assertEquals( "dddd", ( (ITestChildElementB) model.getListPropertyA().get( 1 ) ).getValuePropertyB().getText() );
        assertEquals( "eeee", ( (ITestChildElementA) model.getListPropertyA().get( 2 ) ).getValuePropertyA().getText() );
        
        assertEquals( "ffff", ( (ITestChildElementB) model.getElementPropertyA().element( false ) ).getValuePropertyB().getText() );
    }

}
