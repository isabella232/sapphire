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

package org.eclipse.sapphire.tests.misc.t0003;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.VersionConstraint;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests VersionConstraint class.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestMisc0003 extends SapphireTestCase
{
    private TestMisc0003( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestMisc0003" );

        suite.addTest( new TestMisc0003( "testSingleVersionConstraint" ) );
        suite.addTest( new TestMisc0003( "testClosedRangeVersionConstraint1" ) );
        suite.addTest( new TestMisc0003( "testClosedRangeVersionConstraint2" ) );
        suite.addTest( new TestMisc0003( "testClosedRangeVersionConstraint3" ) );
        suite.addTest( new TestMisc0003( "testOpenRangeVersionConstraint1" ) );
        suite.addTest( new TestMisc0003( "testOpenRangeVersionConstraint2" ) );
        suite.addTest( new TestMisc0003( "testOpenRangeVersionConstraint3" ) );
        suite.addTest( new TestMisc0003( "testOpenRangeVersionConstraint4" ) );
        suite.addTest( new TestMisc0003( "testComplexVersionConstraint" ) );
        suite.addTest( new TestMisc0003( "testVersionConstraintSpaceTolerance" ) );
        suite.addTest( new TestMisc0003( "testVersionConstraintErrorHandling01" ) );
        suite.addTest( new TestMisc0003( "testVersionConstraintErrorHandling02" ) );
        suite.addTest( new TestMisc0003( "testVersionConstraintErrorHandling03" ) );
        suite.addTest( new TestMisc0003( "testVersionConstraintErrorHandling04" ) );
        suite.addTest( new TestMisc0003( "testVersionConstraintErrorHandling05" ) );
        suite.addTest( new TestMisc0003( "testVersionConstraintErrorHandling06" ) );
        suite.addTest( new TestMisc0003( "testVersionConstraintErrorHandling07" ) );
        suite.addTest( new TestMisc0003( "testVersionConstraintErrorHandling08" ) );
        suite.addTest( new TestMisc0003( "testVersionConstraintErrorHandling09" ) );
        suite.addTest( new TestMisc0003( "testVersionConstraintErrorHandling10" ) );
        suite.addTest( new TestMisc0003( "testVersionConstraintErrorHandling11" ) );
        suite.addTest( new TestMisc0003( "testVersionConstraintErrorHandling12" ) );
        suite.addTest( new TestMisc0003( "testVersionConstraintErrorHandling13" ) );
        suite.addTest( new TestMisc0003( "testVersionConstraintErrorHandling14" ) );
        suite.addTest( new TestMisc0003( "testVersionConstraintErrorHandling15" ) );
        suite.addTest( new TestMisc0003( "testVersionConstraintErrorHandling16" ) );
        suite.addTest( new TestMisc0003( "testVersionConstraintErrorHandling17" ) );
        suite.addTest( new TestMisc0003( "testVersionConstraintErrorHandling18" ) );
        suite.addTest( new TestMisc0003( "testVersionConstraintErrorHandling19" ) );
        suite.addTest( new TestMisc0003( "testVersionConstraintErrorHandling20" ) );
        
        return suite;
    }
    
    public void testSingleVersionConstraint()
    {
        final VersionConstraint constraint = new VersionConstraint( "1.2.3" );
        
        assertTrue( constraint.check( new Version( "1.2.3" ) ) );
        assertTrue( constraint.check( new Version( "1.2.3.0.0" ) ) );
        assertFalse( constraint.check( new Version( "1.2.4" ) ) );
        assertFalse( constraint.check( new Version( "1.2.3.4" ) ) );
    }
    
    public void testClosedRangeVersionConstraint1()
    {
        final VersionConstraint constraint = new VersionConstraint( "[1.2.3-1.3)" );
        
        assertTrue( constraint.check( new Version( "1.2.3" ) ) );
        assertTrue( constraint.check( new Version( "1.2.3.0.0" ) ) );
        assertTrue( constraint.check( new Version( "1.2.5" ) ) );
        assertFalse( constraint.check( new Version( "1.3" ) ) );
        assertFalse( constraint.check( new Version( "1.3.0" ) ) );
    }
    
    public void testClosedRangeVersionConstraint2()
    {
        final VersionConstraint constraint = new VersionConstraint( "[1.2.3-1.3]" );
        
        assertTrue( constraint.check( new Version( "1.2.3" ) ) );
        assertTrue( constraint.check( new Version( "1.2.3.0.0" ) ) );
        assertTrue( constraint.check( new Version( "1.2.5" ) ) );
        assertTrue( constraint.check( new Version( "1.3" ) ) );
        assertTrue( constraint.check( new Version( "1.3.0" ) ) );
        assertFalse( constraint.check( new Version( "1.3.1" ) ) );
    }

    public void testClosedRangeVersionConstraint3()
    {
        final VersionConstraint constraint = new VersionConstraint( "(1.2.3-1.3]" );
        
        assertFalse( constraint.check( new Version( "1.2.3" ) ) );
        assertFalse( constraint.check( new Version( "1.2.3.0.0" ) ) );
        assertTrue( constraint.check( new Version( "1.2.5" ) ) );
        assertTrue( constraint.check( new Version( "1.3" ) ) );
        assertTrue( constraint.check( new Version( "1.3.0" ) ) );
        assertFalse( constraint.check( new Version( "1.3.1" ) ) );
    }

    public void testOpenRangeVersionConstraint1()
    {
        final VersionConstraint constraint = new VersionConstraint( "[1.2.3" );
        
        assertFalse( constraint.check( new Version( "0.5.3" ) ) );
        assertFalse( constraint.check( new Version( "1.0" ) ) );
        assertFalse( constraint.check( new Version( "1.2.2" ) ) );
        assertTrue( constraint.check( new Version( "1.2.3" ) ) );
        assertTrue( constraint.check( new Version( "1.2.3.0.0" ) ) );
        assertTrue( constraint.check( new Version( "1.2.5" ) ) );
        assertTrue( constraint.check( new Version( "1.3" ) ) );
        assertTrue( constraint.check( new Version( "2.0" ) ) );
        assertTrue( constraint.check( new Version( "55.0" ) ) );
    }

    public void testOpenRangeVersionConstraint2()
    {
        final VersionConstraint constraint = new VersionConstraint( "(1.2.3" );
        
        assertFalse( constraint.check( new Version( "0.5.3" ) ) );
        assertFalse( constraint.check( new Version( "1.0" ) ) );
        assertFalse( constraint.check( new Version( "1.2.2" ) ) );
        assertFalse( constraint.check( new Version( "1.2.3" ) ) );
        assertFalse( constraint.check( new Version( "1.2.3.0.0" ) ) );
        assertTrue( constraint.check( new Version( "1.2.5" ) ) );
        assertTrue( constraint.check( new Version( "1.3" ) ) );
        assertTrue( constraint.check( new Version( "2.0" ) ) );
        assertTrue( constraint.check( new Version( "55.0" ) ) );
    }
    
    public void testOpenRangeVersionConstraint3()
    {
        final VersionConstraint constraint = new VersionConstraint( "1.2.3]" );
        
        assertTrue( constraint.check( new Version( "0.5.3" ) ) );
        assertTrue( constraint.check( new Version( "1.0" ) ) );
        assertTrue( constraint.check( new Version( "1.2.2" ) ) );
        assertTrue( constraint.check( new Version( "1.2.3" ) ) );
        assertTrue( constraint.check( new Version( "1.2.3.0.0" ) ) );
        assertFalse( constraint.check( new Version( "1.2.5" ) ) );
        assertFalse( constraint.check( new Version( "1.3" ) ) );
        assertFalse( constraint.check( new Version( "2.0" ) ) );
        assertFalse( constraint.check( new Version( "55.0" ) ) );
    }
    
    public void testOpenRangeVersionConstraint4()
    {
        final VersionConstraint constraint = new VersionConstraint( "1.2.3)" );
        
        assertTrue( constraint.check( new Version( "0.5.3" ) ) );
        assertTrue( constraint.check( new Version( "1.0" ) ) );
        assertTrue( constraint.check( new Version( "1.2.2" ) ) );
        assertFalse( constraint.check( new Version( "1.2.3" ) ) );
        assertFalse( constraint.check( new Version( "1.2.3.0.0" ) ) );
        assertFalse( constraint.check( new Version( "1.2.5" ) ) );
        assertFalse( constraint.check( new Version( "1.3" ) ) );
        assertFalse( constraint.check( new Version( "2.0" ) ) );
        assertFalse( constraint.check( new Version( "55.0" ) ) );
    }

    public void testComplexVersionConstraint()
    {
        final VersionConstraint constraint = new VersionConstraint( "1.2.3,3.4,[5.2-6.0),[8.3" );
        
        assertFalse( constraint.check( new Version( "1.2" ) ) );
        assertFalse( constraint.check( new Version( "1.2.1" ) ) );
        assertTrue( constraint.check( new Version( "1.2.3" ) ) );
        assertFalse( constraint.check( new Version( "1.2.4" ) ) );
        assertFalse( constraint.check( new Version( "3.3" ) ) );
        assertTrue( constraint.check( new Version( "3.4" ) ) );
        assertFalse( constraint.check( new Version( "3.4.1" ) ) );
        assertFalse( constraint.check( new Version( "3.5" ) ) );
        assertFalse( constraint.check( new Version( "5.0" ) ) );
        assertFalse( constraint.check( new Version( "5.1" ) ) );
        assertTrue( constraint.check( new Version( "5.2" ) ) );
        assertTrue( constraint.check( new Version( "5.3" ) ) );
        assertTrue( constraint.check( new Version( "5.9.9.9" ) ) );
        assertFalse( constraint.check( new Version( "6.0" ) ) );
        assertFalse( constraint.check( new Version( "6.1" ) ) );
        assertFalse( constraint.check( new Version( "7.3" ) ) );
        assertFalse( constraint.check( new Version( "8.0" ) ) );
        assertFalse( constraint.check( new Version( "8.2" ) ) );
        assertTrue( constraint.check( new Version( "8.3" ) ) );
        assertTrue( constraint.check( new Version( "8.3.4.5" ) ) );
        assertTrue( constraint.check( new Version( "8.5" ) ) );
        assertTrue( constraint.check( new Version( "9.0" ) ) );
        assertTrue( constraint.check( new Version( "55.0" ) ) );
    }

    public void testVersionConstraintSpaceTolerance()
    {
        final VersionConstraint constraint = new VersionConstraint( "   1.2.3, 3.4  ,   [   5.2  -   6.0) ,    [  8.3    " );
        
        assertFalse( constraint.check( new Version( "1.2" ) ) );
        assertFalse( constraint.check( new Version( "1.2.1" ) ) );
        assertTrue( constraint.check( new Version( "1.2.3" ) ) );
        assertFalse( constraint.check( new Version( "1.2.4" ) ) );
        assertFalse( constraint.check( new Version( "3.3" ) ) );
        assertTrue( constraint.check( new Version( "3.4" ) ) );
        assertFalse( constraint.check( new Version( "3.4.1" ) ) );
        assertFalse( constraint.check( new Version( "3.5" ) ) );
        assertFalse( constraint.check( new Version( "5.0" ) ) );
        assertFalse( constraint.check( new Version( "5.1" ) ) );
        assertTrue( constraint.check( new Version( "5.2" ) ) );
        assertTrue( constraint.check( new Version( "5.3" ) ) );
        assertTrue( constraint.check( new Version( "5.9.9.9" ) ) );
        assertFalse( constraint.check( new Version( "6.0" ) ) );
        assertFalse( constraint.check( new Version( "6.1" ) ) );
        assertFalse( constraint.check( new Version( "7.3" ) ) );
        assertFalse( constraint.check( new Version( "8.0" ) ) );
        assertFalse( constraint.check( new Version( "8.2" ) ) );
        assertTrue( constraint.check( new Version( "8.3" ) ) );
        assertTrue( constraint.check( new Version( "8.3.4.5" ) ) );
        assertTrue( constraint.check( new Version( "8.5" ) ) );
        assertTrue( constraint.check( new Version( "9.0" ) ) );
        assertTrue( constraint.check( new Version( "55.0" ) ) );
    }

    public void testVersionConstraintErrorHandling01()
    {
        try
        {
            new VersionConstraint( null );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
    }

    public void testVersionConstraintErrorHandling02()
    {
        try
        {
            new VersionConstraint( "" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
    }

    public void testVersionConstraintErrorHandling03()
    {
        try
        {
            new VersionConstraint( "   " );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
    }

    public void testVersionConstraintErrorHandling04()
    {
        try
        {
            new VersionConstraint( "[" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
    }

    public void testVersionConstraintErrorHandling05()
    {
        try
        {
            new VersionConstraint( ")" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
    }

    public void testVersionConstraintErrorHandling06()
    {
        try
        {
            new VersionConstraint( "[[1.2.3" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
    }
    
    public void testVersionConstraintErrorHandling07()
    {
        try
        {
            new VersionConstraint( "1.2.3))" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
    }
    
    public void testVersionConstraintErrorHandling08()
    {
        try
        {
            new VersionConstraint( "[1.2.3-" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
    }

    public void testVersionConstraintErrorHandling09()
    {
        try
        {
            new VersionConstraint( "-1.2.3)" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
    }
    
    public void testVersionConstraintErrorHandling10()
    {
        try
        {
            new VersionConstraint( "1.2.3-" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
    }

    public void testVersionConstraintErrorHandling11()
    {
        try
        {
            new VersionConstraint( "-1.2.3" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
    }

    public void testVersionConstraintErrorHandling12()
    {
        try
        {
            new VersionConstraint( "1.2.3-4.5.6" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
    }
    
    public void testVersionConstraintErrorHandling13()
    {
        try
        {
            new VersionConstraint( ",1.2.3" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
    }

    public void testVersionConstraintErrorHandling14()
    {
        try
        {
            new VersionConstraint( "1.2.3," );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
    }
    
    public void testVersionConstraintErrorHandling15()
    {
        try
        {
            new VersionConstraint( "1.2.3,,4.5.6" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
    }
    
    public void testVersionConstraintErrorHandling16()
    {
        try
        {
            new VersionConstraint( "1.2.3." );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
    }

    public void testVersionConstraintErrorHandling17()
    {
        try
        {
            new VersionConstraint( ".1.2.3" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
    }

    public void testVersionConstraintErrorHandling18()
    {
        try
        {
            new VersionConstraint( "1..2.3" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
    }

    public void testVersionConstraintErrorHandling19()
    {
        try
        {
            new VersionConstraint( "1 2.3" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
    }

    public void testVersionConstraintErrorHandling20()
    {
        try
        {
            new VersionConstraint( "1.2.3.v20120702" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
    }

}
