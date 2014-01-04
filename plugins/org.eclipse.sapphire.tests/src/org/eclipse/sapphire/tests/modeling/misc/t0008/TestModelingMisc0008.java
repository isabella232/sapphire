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

package org.eclipse.sapphire.tests.modeling.misc.t0008;

import java.util.Collections;

import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests behavior of ElementList when the list property is read-only.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestModelingMisc0008 extends SapphireTestCase
{
    @Test
    
    public void testAddNewElement1() throws Exception
    {
        final RootXmlResource rootXmlResource = new RootXmlResource( new XmlResourceStore( loadResource( "TestData.xml" ) ) );
        final TestRootElement root = TestRootElement.TYPE.instantiate( rootXmlResource );
        
        root.getChildren().insert();
        
        try
        {
            root.getChildrenReadOnly().insert();
            fail( "Failed to generated UnsupportedOperationException." );
        }
        catch( UnsupportedOperationException e ) {}
    }

    @Test
    
    public void testAddNewElement2() throws Exception
    {
        final RootXmlResource rootXmlResource = new RootXmlResource( new XmlResourceStore( loadResource( "TestData.xml" ) ) );
        final TestRootElement root = TestRootElement.TYPE.instantiate( rootXmlResource );
        
        root.getChildren().insert( TestChildElement.TYPE );
        
        try
        {
            root.getChildrenReadOnly().insert( TestChildElement.TYPE );
            fail( "Failed to generated UnsupportedOperationException." );
        }
        catch( UnsupportedOperationException e ) {}
    }

    @Test
    
    public void testAddNewElement3() throws Exception
    {
        final RootXmlResource rootXmlResource = new RootXmlResource( new XmlResourceStore( loadResource( "TestData.xml" ) ) );
        final TestRootElement root = TestRootElement.TYPE.instantiate( rootXmlResource );
        
        root.getChildren().insert( TestChildElement.class );
        
        try
        {
            root.getChildrenReadOnly().insert( TestChildElement.class );
            fail( "Failed to generated UnsupportedOperationException." );
        }
        catch( UnsupportedOperationException e ) {}
    }

    @Test
    
    public void testMoveUp() throws Exception
    {
        final RootXmlResource rootXmlResource = new RootXmlResource( new XmlResourceStore( loadResource( "TestData.xml" ) ) );
        final TestRootElement root = TestRootElement.TYPE.instantiate( rootXmlResource );
        
        root.getChildren().moveUp( root.getChildren().get( 1 ) );
        
        try
        {
            root.getChildrenReadOnly().moveUp( root.getChildrenReadOnly().get( 1 ) );
            fail( "Failed to generated UnsupportedOperationException." );
        }
        catch( UnsupportedOperationException e ) {}
    }

    @Test
    
    public void testMoveDown() throws Exception
    {
        final RootXmlResource rootXmlResource = new RootXmlResource( new XmlResourceStore( loadResource( "TestData.xml" ) ) );
        final TestRootElement root = TestRootElement.TYPE.instantiate( rootXmlResource );
        
        root.getChildren().moveDown( root.getChildren().get( 0 ) );
        
        try
        {
            root.getChildrenReadOnly().moveDown( root.getChildrenReadOnly().get( 0 ) );
            fail( "Failed to generated UnsupportedOperationException." );
        }
        catch( UnsupportedOperationException e ) {}
    }

    @Test
    
    public void testSwap() throws Exception
    {
        final RootXmlResource rootXmlResource = new RootXmlResource( new XmlResourceStore( loadResource( "TestData.xml" ) ) );
        final TestRootElement root = TestRootElement.TYPE.instantiate( rootXmlResource );
        
        root.getChildren().swap( root.getChildren().get( 0 ), root.getChildren().get( 1 ) );
        
        try
        {
            root.getChildrenReadOnly().swap( root.getChildrenReadOnly().get( 0 ), root.getChildrenReadOnly().get( 1 ) );
            fail( "Failed to generated UnsupportedOperationException." );
        }
        catch( UnsupportedOperationException e ) {}
    }

    @Test
    
    public void testRemove1() throws Exception
    {
        final RootXmlResource rootXmlResource = new RootXmlResource( new XmlResourceStore( loadResource( "TestData.xml" ) ) );
        final TestRootElement root = TestRootElement.TYPE.instantiate( rootXmlResource );
        
        root.getChildren().remove( root.getChildren().get( 0 ) );
        
        try
        {
            root.getChildrenReadOnly().remove( root.getChildrenReadOnly().get( 0 ) );
            fail( "Failed to generated UnsupportedOperationException." );
        }
        catch( UnsupportedOperationException e ) {}
    }

    @Test
    
    public void testRemove2() throws Exception
    {
        final RootXmlResource rootXmlResource = new RootXmlResource( new XmlResourceStore( loadResource( "TestData.xml" ) ) );
        final TestRootElement root = TestRootElement.TYPE.instantiate( rootXmlResource );
        
        root.getChildren().remove( 0 );
        
        try
        {
            root.getChildrenReadOnly().remove( 0 );
            fail( "Failed to generated UnsupportedOperationException." );
        }
        catch( UnsupportedOperationException e ) {}
    }
    
    @Test

    public void testRemoveAll() throws Exception
    {
        final RootXmlResource rootXmlResource = new RootXmlResource( new XmlResourceStore( loadResource( "TestData.xml" ) ) );
        final TestRootElement root = TestRootElement.TYPE.instantiate( rootXmlResource );
        
        root.getChildren().removeAll( Collections.singleton( root.getChildren().get( 0 ) ) );
        
        try
        {
            root.getChildrenReadOnly().removeAll( Collections.singleton( root.getChildrenReadOnly().get( 0 ) ) );
            fail( "Failed to generated UnsupportedOperationException." );
        }
        catch( UnsupportedOperationException e ) {}
    }
    
    @Test

    public void testRetainAll() throws Exception
    {
        final RootXmlResource rootXmlResource = new RootXmlResource( new XmlResourceStore( loadResource( "TestData.xml" ) ) );
        final TestRootElement root = TestRootElement.TYPE.instantiate( rootXmlResource );
        
        root.getChildren().retainAll( Collections.singleton( root.getChildren().get( 0 ) ) );
        
        try
        {
            root.getChildrenReadOnly().retainAll( Collections.singleton( root.getChildrenReadOnly().get( 0 ) ) );
            fail( "Failed to generated UnsupportedOperationException." );
        }
        catch( UnsupportedOperationException e ) {}
    }
    
    @Test

    public void testClear() throws Exception
    {
        final RootXmlResource rootXmlResource = new RootXmlResource( new XmlResourceStore( loadResource( "TestData.xml" ) ) );
        final TestRootElement root = TestRootElement.TYPE.instantiate( rootXmlResource );
        
        root.getChildren().clear();
        
        try
        {
            root.getChildrenReadOnly().clear();
            fail( "Failed to generated UnsupportedOperationException." );
        }
        catch( UnsupportedOperationException e ) {}
    }

}
