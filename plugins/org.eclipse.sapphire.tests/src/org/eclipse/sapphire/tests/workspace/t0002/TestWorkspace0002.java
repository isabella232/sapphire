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

package org.eclipse.sapphire.tests.workspace.t0002;

import java.util.SortedSet;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.services.FactsAggregationService;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests operation of FactsService implementations.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestWorkspace0002 extends SapphireTestCase
{
    @Test
    
    public void testProjectRelativePath() throws Exception
    {
        test( TestRootElement.PROP_PROJECT_RELATIVE_PATH, "Must be a project relative path" );
    }

    @Test
    
    public void testWorkspaceRelativePath() throws Exception
    {
        test( TestRootElement.PROP_WORKSPACE_RELATIVE_PATH, "Must be a workspace relative path" );
    }

    private static void test( final PropertyDef property,
                              final String... factsExpected )
    {
        test( TestRootElement.TYPE.instantiate(), property, factsExpected );
    }
    
    private static void test( final Element element,
                              final PropertyDef property,
                              final String... factsExpected )
    {
        final SortedSet<String> factsActual = element.property( property ).service( FactsAggregationService.class ).facts();
        
        assertEquals( set( factsExpected ), factsActual );
    }

}
