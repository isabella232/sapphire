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

package org.eclipse.sapphire.tests.services.t0003;

import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.services.DependenciesAggregationService;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests DependenciesService and DependenciesAggregationService along with the related @DependsOn annotation.
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

        suite.addTest( new TestServices0003( "testCustom1" ) );
        suite.addTest( new TestServices0003( "testCustom2" ) );
        
        return suite;
    }
    
    public void testCustom1() throws Exception
    {
        final TestModel model = TestModel.TYPE.instantiate();
        final TestModelItem item = model.getItems().insert();
        final Set<ModelPath> dependencies = item.service( TestModelItem.PROP_CUSTOM_1, DependenciesAggregationService.class ).dependencies();
        
        assertEquals( set( new ModelPath( "Name" ), new ModelPath( "Id" ) ), dependencies );
    }
    
    public void testCustom2() throws Exception
    {
        final TestModel model = TestModel.TYPE.instantiate();
        final TestModelItem item = model.getItems().insert();
        final Set<ModelPath> dependencies = item.service( TestModelItem.PROP_CUSTOM_2, DependenciesAggregationService.class ).dependencies();
        
        assertEquals( set( new ModelPath( "Name" ), new ModelPath( "Id" ), new ModelPath( "Custom1" ) ), dependencies );
    }
    
}
