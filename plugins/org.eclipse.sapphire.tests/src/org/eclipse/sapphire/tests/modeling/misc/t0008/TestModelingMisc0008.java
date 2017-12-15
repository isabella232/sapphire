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

package org.eclipse.sapphire.tests.modeling.misc.t0008;

import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests behavior of ModelElementList when the list property is read-only.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestModelingMisc0008 extends SapphireTestCase
{
    private TestModelingMisc0008( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestModelingMisc0008" );

        suite.addTest( new TestModelingMisc0008( "testAddNewElement1" ) );
        suite.addTest( new TestModelingMisc0008( "testAddNewElement2" ) );
        suite.addTest( new TestModelingMisc0008( "testAddNewElement3" ) );
        suite.addTest( new TestModelingMisc0008( "testMoveUp" ) );
        suite.addTest( new TestModelingMisc0008( "testMoveDown" ) );
        suite.addTest( new TestModelingMisc0008( "testSwap" ) );
        suite.addTest( new TestModelingMisc0008( "testRemove1" ) );
        suite.addTest( new TestModelingMisc0008( "testRemove2" ) );
        suite.addTest( new TestModelingMisc0008( "testRemoveAll" ) );
        suite.addTest( new TestModelingMisc0008( "testRetainAll" ) );
        suite.addTest( new TestModelingMisc0008( "testClear" ) );
        
        return suite;
    }
    
    public void testAddNewElement1() throws Exception
    {
        final RootXmlResource rootXmlResource = new RootXmlResource( new XmlResourceStore( loadResource( "TestData.xml" ) ) );
        final ITestRootElement root = ITestRootElement.TYPE.instantiate( rootXmlResource );
        
        root.getChildren().addNewElement();
        
        try
        {
            root.getChildrenReadOnly().addNewElement();
            fail( "Failed to generated UnsupportedOperationException." );
        }
        catch( UnsupportedOperationException e ) {}
    }

    public void testAddNewElement2() throws Exception
    {
        final RootXmlResource rootXmlResource = new RootXmlResource( new XmlResourceStore( loadResource( "TestData.xml" ) ) );
        final ITestRootElement root = ITestRootElement.TYPE.instantiate( rootXmlResource );
        
        root.getChildren().addNewElement( ITestChildElement.TYPE );
        
        try
        {
            root.getChildrenReadOnly().addNewElement( ITestChildElement.TYPE );
            fail( "Failed to generated UnsupportedOperationException." );
        }
        catch( UnsupportedOperationException e ) {}
    }

    public void testAddNewElement3() throws Exception
    {
        final RootXmlResource rootXmlResource = new RootXmlResource( new XmlResourceStore( loadResource( "TestData.xml" ) ) );
        final ITestRootElement root = ITestRootElement.TYPE.instantiate( rootXmlResource );
        
        root.getChildren().addNewElement( ITestChildElement.class );
        
        try
        {
            root.getChildrenReadOnly().addNewElement( ITestChildElement.class );
            fail( "Failed to generated UnsupportedOperationException." );
        }
        catch( UnsupportedOperationException e ) {}
    }

    public void testMoveUp() throws Exception
    {
        final RootXmlResource rootXmlResource = new RootXmlResource( new XmlResourceStore( loadResource( "TestData.xml" ) ) );
        final ITestRootElement root = ITestRootElement.TYPE.instantiate( rootXmlResource );
        
        root.getChildren().moveUp( root.getChildren().get( 1 ) );
        
        try
        {
            root.getChildrenReadOnly().moveUp( root.getChildrenReadOnly().get( 1 ) );
            fail( "Failed to generated UnsupportedOperationException." );
        }
        catch( UnsupportedOperationException e ) {}
    }

    public void testMoveDown() throws Exception
    {
        final RootXmlResource rootXmlResource = new RootXmlResource( new XmlResourceStore( loadResource( "TestData.xml" ) ) );
        final ITestRootElement root = ITestRootElement.TYPE.instantiate( rootXmlResource );
        
        root.getChildren().moveDown( root.getChildren().get( 0 ) );
        
        try
        {
            root.getChildrenReadOnly().moveDown( root.getChildrenReadOnly().get( 0 ) );
            fail( "Failed to generated UnsupportedOperationException." );
        }
        catch( UnsupportedOperationException e ) {}
    }

    public void testSwap() throws Exception
    {
        final RootXmlResource rootXmlResource = new RootXmlResource( new XmlResourceStore( loadResource( "TestData.xml" ) ) );
        final ITestRootElement root = ITestRootElement.TYPE.instantiate( rootXmlResource );
        
        root.getChildren().swap( root.getChildren().get( 0 ), root.getChildren().get( 1 ) );
        
        try
        {
            root.getChildrenReadOnly().swap( root.getChildrenReadOnly().get( 0 ), root.getChildrenReadOnly().get( 1 ) );
            fail( "Failed to generated UnsupportedOperationException." );
        }
        catch( UnsupportedOperationException e ) {}
    }

    public void testRemove1() throws Exception
    {
        final RootXmlResource rootXmlResource = new RootXmlResource( new XmlResourceStore( loadResource( "TestData.xml" ) ) );
        final ITestRootElement root = ITestRootElement.TYPE.instantiate( rootXmlResource );
        
        root.getChildren().remove( root.getChildren().get( 0 ) );
        
        try
        {
            root.getChildrenReadOnly().remove( root.getChildrenReadOnly().get( 0 ) );
            fail( "Failed to generated UnsupportedOperationException." );
        }
        catch( UnsupportedOperationException e ) {}
    }

    public void testRemove2() throws Exception
    {
        final RootXmlResource rootXmlResource = new RootXmlResource( new XmlResourceStore( loadResource( "TestData.xml" ) ) );
        final ITestRootElement root = ITestRootElement.TYPE.instantiate( rootXmlResource );
        
        root.getChildren().remove( 0 );
        
        try
        {
            root.getChildrenReadOnly().remove( 0 );
            fail( "Failed to generated UnsupportedOperationException." );
        }
        catch( UnsupportedOperationException e ) {}
    }

    public void testRemoveAll() throws Exception
    {
        final RootXmlResource rootXmlResource = new RootXmlResource( new XmlResourceStore( loadResource( "TestData.xml" ) ) );
        final ITestRootElement root = ITestRootElement.TYPE.instantiate( rootXmlResource );
        
        root.getChildren().removeAll( Collections.singleton( root.getChildren().get( 0 ) ) );
        
        try
        {
            root.getChildrenReadOnly().removeAll( Collections.singleton( root.getChildrenReadOnly().get( 0 ) ) );
            fail( "Failed to generated UnsupportedOperationException." );
        }
        catch( UnsupportedOperationException e ) {}
    }

    public void testRetainAll() throws Exception
    {
        final RootXmlResource rootXmlResource = new RootXmlResource( new XmlResourceStore( loadResource( "TestData.xml" ) ) );
        final ITestRootElement root = ITestRootElement.TYPE.instantiate( rootXmlResource );
        
        root.getChildren().retainAll( Collections.singleton( root.getChildren().get( 0 ) ) );
        
        try
        {
            root.getChildrenReadOnly().retainAll( Collections.singleton( root.getChildrenReadOnly().get( 0 ) ) );
            fail( "Failed to generated UnsupportedOperationException." );
        }
        catch( UnsupportedOperationException e ) {}
    }

    public void testClear() throws Exception
    {
        final RootXmlResource rootXmlResource = new RootXmlResource( new XmlResourceStore( loadResource( "TestData.xml" ) ) );
        final ITestRootElement root = ITestRootElement.TYPE.instantiate( rootXmlResource );
        
        root.getChildren().clear();
        
        try
        {
            root.getChildrenReadOnly().clear();
            fail( "Failed to generated UnsupportedOperationException." );
        }
        catch( UnsupportedOperationException e ) {}
    }

}
