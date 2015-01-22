/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.java.t0005;

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

public final class TestJava0005 extends SapphireTestCase
{
    @Test
    
    public void testKindOne() throws Exception
    {
        test( TestRootElement.PROP_KIND_ONE, "Must be a concrete class" );
    }
    
    @Test

    public void testKindTwo() throws Exception
    {
        test( TestRootElement.PROP_KIND_TWO, "Must be a concrete class or an abstract class" );
    }
    
    @Test
    
    public void testKindThree() throws Exception
    {
        test( TestRootElement.PROP_KIND_THREE, "Must be a concrete class, an abstract class or an interface" );
    }
    
    @Test
    
    public void testKindFour() throws Exception
    {
        test( TestRootElement.PROP_KIND_FOUR, "Must be a concrete class, an abstract class, an interface or an annotation" );
    }
    
    @Test

    public void testTypeOne() throws Exception
    {
        test( TestRootElement.PROP_TYPE_ONE, "Must implement or extend java.util.List" );
    }
    
    @Test
    
    public void testTypeOneOf() throws Exception
    {
        test( TestRootElement.PROP_TYPE_ONE_OF, "Must implement or extend one of: java.util.List, java.util.Map, java.util.Set" );
    }
    
    @Test
    
    public void testTypeAll() throws Exception
    {
        test( TestRootElement.PROP_TYPE_ALL, "Must implement or extend all: java.lang.Cloneable, java.lang.Comparable, java.util.List" );
    }
    
    @Test

    public void testCombo1() throws Exception
    {
        test( TestRootElement.PROP_COMBO_1, "Must be a concrete class", "Must implement java.util.List" );
    }
    
    @Test
    
    public void testCombo2() throws Exception
    {
        test( TestRootElement.PROP_COMBO_2, "Must be a concrete class", "Must extend java.util.AbstractList" );
    }
    
    @Test
    
    public void testCombo3() throws Exception
    {
        test( TestRootElement.PROP_COMBO_3, "Must be a concrete class, an abstract class or an interface", "Must implement or extend java.util.List" );
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
