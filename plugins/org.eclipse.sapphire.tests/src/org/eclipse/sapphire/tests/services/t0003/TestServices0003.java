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

package org.eclipse.sapphire.tests.services.t0003;

import java.util.Set;

import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.services.DependenciesService;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.eclipse.sapphire.util.SetFactory;
import org.junit.Test;

/**
 * Tests DependenciesService along with the related @DependsOn annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestServices0003 extends SapphireTestCase
{
    @Test
    
    public void testCustom1() throws Exception
    {
        final TestModel model = TestModel.TYPE.instantiate();
        final TestModelItem item = model.getItems().insert();
        
        assertEquals( set( new ModelPath( "Name" ), new ModelPath( "Id" ) ), dependencies( item.getCustom1() ) );
    }
    
    @Test
    
    public void testCustom2() throws Exception
    {
        final TestModel model = TestModel.TYPE.instantiate();
        final TestModelItem item = model.getItems().insert();
        
        assertEquals( set( new ModelPath( "Name" ), new ModelPath( "Id" ), new ModelPath( "Custom1" ) ), dependencies( item.getCustom2() ) );
    }
    
    private static Set<ModelPath> dependencies( final Property property )
    {
        final SetFactory<ModelPath> dependencies = SetFactory.start();
        
        for( DependenciesService ds : property.services( DependenciesService.class ) )
        {
            dependencies.add( ds.dependencies() );
        }
        
        return dependencies.result();
    }
    
}
