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

package org.eclipse.sapphire.tests.java.t0005;

import java.util.SortedSet;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.services.FactsAggregationService;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests operation of FactsService implementations.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestJava0005 extends SapphireTestCase
{
    private TestJava0005( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "Java0005" );

        suite.addTest( new TestJava0005( "testKindOne" ) );
        suite.addTest( new TestJava0005( "testKindTwo" ) );
        suite.addTest( new TestJava0005( "testKindThree" ) );
        suite.addTest( new TestJava0005( "testKindFour" ) );
        suite.addTest( new TestJava0005( "testTypeOne" ) );
        suite.addTest( new TestJava0005( "testTypeOneOf" ) );
        suite.addTest( new TestJava0005( "testTypeAll" ) );
        suite.addTest( new TestJava0005( "testCombo1" ) );
        suite.addTest( new TestJava0005( "testCombo2" ) );
        suite.addTest( new TestJava0005( "testCombo3" ) );
        
        return suite;
    }
    
    public void testKindOne() throws Exception
    {
        test( TestRootElement.PROP_KIND_ONE, "Must be a concrete class." );
    }

    public void testKindTwo() throws Exception
    {
        test( TestRootElement.PROP_KIND_TWO, "Must be a concrete class or an abstract class." );
    }
    
    public void testKindThree() throws Exception
    {
        test( TestRootElement.PROP_KIND_THREE, "Must be a concrete class, an abstract class or an interface." );
    }
    
    public void testKindFour() throws Exception
    {
        test( TestRootElement.PROP_KIND_FOUR, "Must be a concrete class, an abstract class, an interface or an annotation." );
    }

    public void testTypeOne() throws Exception
    {
        test( TestRootElement.PROP_TYPE_ONE, "Must implement or extend java.util.List." );
    }
    
    public void testTypeOneOf() throws Exception
    {
        test( TestRootElement.PROP_TYPE_ONE_OF, "Must implement or extend one of: java.util.List, java.util.Map, java.util.Set." );
    }
    
    public void testTypeAll() throws Exception
    {
        test( TestRootElement.PROP_TYPE_ALL, "Must implement or extend all: java.lang.Cloneable, java.lang.Comparable, java.util.List." );
    }

    public void testCombo1() throws Exception
    {
        test( TestRootElement.PROP_COMBO_1, "Must be a concrete class.", "Must implement java.util.List." );
    }
    
    public void testCombo2() throws Exception
    {
        test( TestRootElement.PROP_COMBO_2, "Must be a concrete class.", "Must extend java.util.AbstractList." );
    }
    
    public void testCombo3() throws Exception
    {
        test( TestRootElement.PROP_COMBO_3, "Must be a concrete class, an abstract class or an interface.", "Must implement or extend java.util.List." );
    }
    
    private static void test( final ModelProperty property,
                              final String... factsExpected )
    {
        test( TestRootElement.TYPE.instantiate(), property, factsExpected );
    }
    
    private static void test( final IModelElement element,
                              final ModelProperty property,
                              final String... factsExpected )
    {
        final SortedSet<String> factsActual = element.service( property, FactsAggregationService.class ).facts();
        
        assertEquals( set( factsExpected ), factsActual );
    }

}
