/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.services.t0003;

import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.services.DependenciesAggregationService;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests DependenciesService and DependenciesAggregationService along with related @DependsOn and @NoDuplicates annotations.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestServices0003

    extends SapphireTestCase
    
{
    private TestServices0003( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestServices0003" );

        suite.addTest( new TestServices0003( "testNoDuplicatesStandalone" ) );
        suite.addTest( new TestServices0003( "testNoDuplicatesInElementProperty" ) );
        suite.addTest( new TestServices0003( "testNoDuplicatesInImpliedElementProperty" ) );
        suite.addTest( new TestServices0003( "testNoDuplicatesInListProperty" ) );
        suite.addTest( new TestServices0003( "testNoDuplicatesAndDependsOn" ) );
        suite.addTest( new TestServices0003( "testCustom1" ) );
        suite.addTest( new TestServices0003( "testCustom2" ) );
        
        return suite;
    }
    
    public void testNoDuplicatesStandalone() throws Exception
    {
        final TestModelItem item = TestModelItem.TYPE.instantiate();
        final Set<ModelPath> dependencies = item.service( TestModelItem.PROP_NAME, DependenciesAggregationService.class ).dependencies();
        
        assertEquals( set(), dependencies );
    }
    
    public void testNoDuplicatesInElementProperty() throws Exception
    {
        final TestModel model = TestModel.TYPE.instantiate();
        final TestModelItem item = model.getItem().element( true );
        final Set<ModelPath> dependencies = item.service( TestModelItem.PROP_NAME, DependenciesAggregationService.class ).dependencies();
        
        assertEquals( set(), dependencies );
    }
    
    public void testNoDuplicatesInImpliedElementProperty() throws Exception
    {
        final TestModel model = TestModel.TYPE.instantiate();
        final TestModelItem item = model.getItemImplied();
        final Set<ModelPath> dependencies = item.service( TestModelItem.PROP_NAME, DependenciesAggregationService.class ).dependencies();
        
        assertEquals( set(), dependencies );
    }
    
    public void testNoDuplicatesInListProperty() throws Exception
    {
        final TestModel model = TestModel.TYPE.instantiate();
        final TestModelItem item = model.getItems().addNewElement();
        final Set<ModelPath> dependencies = item.service( TestModelItem.PROP_NAME, DependenciesAggregationService.class ).dependencies();
        
        assertEquals( set( new ModelPath( "#/Name" ) ), dependencies );
    }
    
    public void testNoDuplicatesAndDependsOn() throws Exception
    {
        final TestModel model = TestModel.TYPE.instantiate();
        final TestModelItem item = model.getItems().addNewElement();
        final Set<ModelPath> dependencies = item.service( TestModelItem.PROP_ID, DependenciesAggregationService.class ).dependencies();
        
        assertEquals( set( new ModelPath( "Name" ), new ModelPath( "#/Id" ) ), dependencies );
    }
    
    public void testCustom1() throws Exception
    {
        final TestModel model = TestModel.TYPE.instantiate();
        final TestModelItem item = model.getItems().addNewElement();
        final Set<ModelPath> dependencies = item.service( TestModelItem.PROP_CUSTOM_1, DependenciesAggregationService.class ).dependencies();
        
        assertEquals( set( new ModelPath( "Name" ), new ModelPath( "Id" ) ), dependencies );
    }
    
    public void testCustom2() throws Exception
    {
        final TestModel model = TestModel.TYPE.instantiate();
        final TestModelItem item = model.getItems().addNewElement();
        final Set<ModelPath> dependencies = item.service( TestModelItem.PROP_CUSTOM_2, DependenciesAggregationService.class ).dependencies();
        
        assertEquals( set( new ModelPath( "Name" ), new ModelPath( "Id" ), new ModelPath( "Custom1" ) ), dependencies );
    }
    
}
