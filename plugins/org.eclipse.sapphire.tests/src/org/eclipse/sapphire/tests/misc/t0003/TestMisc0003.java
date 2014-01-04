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

package org.eclipse.sapphire.tests.misc.t0003;

import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.VersionConstraint;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests VersionConstraint class.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestMisc0003 extends SapphireTestCase
{
    @Test
    
    public void testSingleVersionConstraint()
    {
        final VersionConstraint constraint = new VersionConstraint( "1.2.3" );
        
        assertTrue( constraint.check( new Version( "1.2.3" ) ) );
        assertTrue( constraint.check( new Version( "1.2.3.0.0" ) ) );
        assertFalse( constraint.check( new Version( "1.2.4" ) ) );
        assertFalse( constraint.check( new Version( "1.2.3.4" ) ) );
    }
    
    @Test
    
    public void testClosedRangeVersionConstraint1()
    {
        final VersionConstraint constraint = new VersionConstraint( "[1.2.3-1.3)" );
        
        assertTrue( constraint.check( new Version( "1.2.3" ) ) );
        assertTrue( constraint.check( new Version( "1.2.3.0.0" ) ) );
        assertTrue( constraint.check( new Version( "1.2.5" ) ) );
        assertFalse( constraint.check( new Version( "1.3" ) ) );
        assertFalse( constraint.check( new Version( "1.3.0" ) ) );
    }
    
    @Test
    
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
    
    @Test

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
    
    @Test

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
    
    @Test

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
    
    @Test
    
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
    
    @Test
    
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
    
    @Test

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
    
    @Test

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
    
    @Test( expected = IllegalArgumentException.class )

    public void testVersionConstraintErrorHandling01()
    {
        new VersionConstraint( null );
    }
    
    @Test( expected = IllegalArgumentException.class )

    public void testVersionConstraintErrorHandling02()
    {
        new VersionConstraint( "" );
    }
    
    @Test( expected = IllegalArgumentException.class )

    public void testVersionConstraintErrorHandling03()
    {
        new VersionConstraint( "   " );
    }
    
    @Test( expected = IllegalArgumentException.class )

    public void testVersionConstraintErrorHandling04()
    {
        new VersionConstraint( "[" );
    }
    
    @Test( expected = IllegalArgumentException.class )

    public void testVersionConstraintErrorHandling05()
    {
        new VersionConstraint( ")" );
    }
    
    @Test( expected = IllegalArgumentException.class )

    public void testVersionConstraintErrorHandling06()
    {
        new VersionConstraint( "[[1.2.3" );
    }
    
    @Test( expected = IllegalArgumentException.class )
    
    public void testVersionConstraintErrorHandling07()
    {
        new VersionConstraint( "1.2.3))" );
    }
    
    @Test( expected = IllegalArgumentException.class )
    
    public void testVersionConstraintErrorHandling08()
    {
        new VersionConstraint( "[1.2.3-" );
    }
    
    @Test( expected = IllegalArgumentException.class )

    public void testVersionConstraintErrorHandling09()
    {
        new VersionConstraint( "-1.2.3)" );
    }
    
    @Test( expected = IllegalArgumentException.class )
    
    public void testVersionConstraintErrorHandling10()
    {
        new VersionConstraint( "1.2.3-" );
    }
    
    @Test( expected = IllegalArgumentException.class )

    public void testVersionConstraintErrorHandling11()
    {
        new VersionConstraint( "-1.2.3" );
    }
    
    @Test( expected = IllegalArgumentException.class )

    public void testVersionConstraintErrorHandling12()
    {
        new VersionConstraint( "1.2.3-4.5.6" );
    }
    
    @Test( expected = IllegalArgumentException.class )
    
    public void testVersionConstraintErrorHandling13()
    {
        new VersionConstraint( ",1.2.3" );
    }
    
    @Test( expected = IllegalArgumentException.class )

    public void testVersionConstraintErrorHandling14()
    {
        new VersionConstraint( "1.2.3," );
    }
    
    @Test( expected = IllegalArgumentException.class )
    
    public void testVersionConstraintErrorHandling15()
    {
        new VersionConstraint( "1.2.3,,4.5.6" );
    }
    
    @Test( expected = IllegalArgumentException.class )
    
    public void testVersionConstraintErrorHandling16()
    {
        new VersionConstraint( "1.2.3." );
    }
    
    @Test( expected = IllegalArgumentException.class )

    public void testVersionConstraintErrorHandling17()
    {
        new VersionConstraint( ".1.2.3" );
    }
    
    @Test( expected = IllegalArgumentException.class )

    public void testVersionConstraintErrorHandling18()
    {
        new VersionConstraint( "1..2.3" );
    }
    
    @Test( expected = IllegalArgumentException.class )

    public void testVersionConstraintErrorHandling19()
    {
        new VersionConstraint( "1 2.3" );
    }
    
    @Test( expected = IllegalArgumentException.class )

    public void testVersionConstraintErrorHandling20()
    {
        new VersionConstraint( "1.2.3.v20120702" );
    }

}
