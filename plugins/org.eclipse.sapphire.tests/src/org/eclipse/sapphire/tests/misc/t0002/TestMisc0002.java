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

package org.eclipse.sapphire.tests.misc.t0002;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests Version class.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestMisc0002 extends SapphireTestCase
{
    private TestMisc0002( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestMisc0002" );

        suite.addTest( new TestMisc0002( "testVersionFromString_Basic" ) );
        suite.addTest( new TestMisc0002( "testVersionFromString_LongSegment" ) );
        suite.addTest( new TestMisc0002( "testVersionFromString_InvalidFormat" ) );
        suite.addTest( new TestMisc0002( "testVersionFromLong" ) );
        suite.addTest( new TestMisc0002( "testVersionCanonicalization" ) );
        suite.addTest( new TestMisc0002( "testVersionComparison" ) );
        suite.addTest( new TestMisc0002( "testVersionEquals" ) );
        suite.addTest( new TestMisc0002( "testVersionToString" ) );
        
        return suite;
    }
    
    public void testVersionFromString_Basic()
    {
        Version version;
        
        version = new Version( "1" );
        
        assertEquals( 1, version.length() );
        assertEquals( 1l, version.segment( 0 ) );
        assertEquals( list( 1l ), version.segments() );
        
        version = new Version( "1.2" );

        assertEquals( 2, version.length() );
        assertEquals( 1l, version.segment( 0 ) );
        assertEquals( 2l, version.segment( 1 ) );
        assertEquals( list( 1l, 2l ), version.segments() );

        version = new Version( "1.2.3" );

        assertEquals( 3, version.length() );
        assertEquals( 1l, version.segment( 0 ) );
        assertEquals( 2l, version.segment( 1 ) );
        assertEquals( 3l, version.segment( 2 ) );
        assertEquals( list( 1l, 2l, 3l ), version.segments() );
    }
    
    public void testVersionFromString_LongSegment()
    {
        final Version version = new Version( "1.2.3.201206260957" );

        assertEquals( 4, version.length() );
        assertEquals( 1l, version.segment( 0 ) );
        assertEquals( 2l, version.segment( 1 ) );
        assertEquals( 3l, version.segment( 2 ) );
        assertEquals( 201206260957l, version.segment( 3 ) );
        assertEquals( list( 1l, 2l, 3l, 201206260957l ), version.segments() );
    }

    public void testVersionFromString_InvalidFormat()
    {
        try
        {
            new Version( null );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
        
        try
        {
            new Version( "" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}

        try
        {
            new Version( ".1.2.3" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}

        try
        {
            new Version( "1.2.3." );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}

        try
        {
            new Version( "1..2.3" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}

        try
        {
            new Version( "abc" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
        
        try
        {
            new Version( "1.2.abc" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}

        try
        {
            new Version( "1.2.v345" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}

        try
        {
            new Version( "1.2.345abc" );
            fail( "IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException e ) {}
    }
    
    public void testVersionFromLong()
    {
        Version version;
        
        version = new Version( 1 );

        assertEquals( 1, version.length() );
        assertEquals( 1l, version.segment( 0 ) );
        assertEquals( list( 1l ), version.segments() );

        version = new Version( 201206260957l );

        assertEquals( 1, version.length() );
        assertEquals( 201206260957l, version.segment( 0 ) );
        assertEquals( list( 201206260957l ), version.segments() );
    }
    
    public void testVersionCanonicalization()
    {
        Version version;
        
        version = new Version( "1.2.3.0" );

        assertEquals( 3, version.length() );
        assertEquals( 1l, version.segment( 0 ) );
        assertEquals( 2l, version.segment( 1 ) );
        assertEquals( 3l, version.segment( 2 ) );
        assertEquals( 0l, version.segment( 3 ) );
        assertEquals( 0l, version.segment( 1000 ) );
        assertEquals( list( 1l, 2l, 3l ), version.segments() );

        version = new Version( "1.2.3.0.0.0.0" );

        assertEquals( 3, version.length() );
        assertEquals( 1l, version.segment( 0 ) );
        assertEquals( 2l, version.segment( 1 ) );
        assertEquals( 3l, version.segment( 2 ) );
        assertEquals( 0l, version.segment( 3 ) );
        assertEquals( 0l, version.segment( 4 ) );
        assertEquals( 0l, version.segment( 5 ) );
        assertEquals( 0l, version.segment( 6 ) );
        assertEquals( 0l, version.segment( 1000 ) );
        assertEquals( list( 1l, 2l, 3l ), version.segments() );
    }
    
    public void testVersionComparison()
    {
        testVersionComparisonSame( new Version( 1 ), new Version( "1" ) );
        testVersionComparisonSame( new Version( "1.2.3" ), new Version( "1.2.3" ) );
        testVersionComparisonSame( new Version( "1.2.3" ), new Version( "1.2.3.0.0" ) );
        
        testVersionComparisonDifferent( new Version( "1.2.4" ), new Version( "1.2.3" ) );
        testVersionComparisonDifferent( new Version( "1.3.3" ), new Version( "1.2.3" ) );
        testVersionComparisonDifferent( new Version( "2.2.3" ), new Version( "1.2.3" ) );
        testVersionComparisonDifferent( new Version( "1.2.3" ), new Version( "1.2" ) );
        testVersionComparisonDifferent( new Version( "1.2.3" ), new Version( "1" ) );
        testVersionComparisonDifferent( new Version( "1.2.3" ), new Version( 1 ) );
    }
    
    private void testVersionComparisonSame( final Version x, final Version y )
    {
        assertTrue( x.compareTo( y ) == 0 );
        assertTrue( y.compareTo( x ) == 0 );
    }
    
    private void testVersionComparisonDifferent( final Version x, final Version y )
    {
        assertTrue( x.compareTo( y ) > 0 );
        assertTrue( y.compareTo( x ) < 0 );
    }
    
    public void testVersionEquals()
    {
        testVersionEquals( new Version( 1 ), new Version( "1" ) );
        testVersionEquals( new Version( "1.2.3" ), new Version( "1.2.3" ) );
        testVersionEquals( new Version( "1.2.3" ), new Version( "1.2.3.0.0" ) );
        
        testVersionNotEquals( new Version( "1.2.3" ), new Version( "1.2" ) );
        testVersionNotEquals( new Version( "1.2.3" ), new Version( "4.5.6" ) );
    }
    
    private void testVersionEquals( final Version x, final Version y )
    {
        assertTrue( x.equals( y ) );
        assertTrue( y.equals( x ) );
        assertTrue( x.hashCode() == y.hashCode() );
    }
    
    private void testVersionNotEquals( final Version x, final Version y )
    {
        assertFalse( x.equals( y ) );
        assertFalse( y.equals( x ) );
    }

    public void testVersionToString()
    {
        assertEquals( "1", ( new Version( 1 ) ).toString() );
        assertEquals( "1", ( new Version( "1" ) ).toString() );
        assertEquals( "1.2.3", ( new Version( "1.2.3" ) ).toString() );
        assertEquals( "1.2.3.201206260957", ( new Version( "1.2.3.201206260957" ) ).toString() );
        assertEquals( "1.2.3", ( new Version( "1.2.3.0" ) ).toString() );
        assertEquals( "1.2.3", ( new Version( "1.2.3.0.0.0.0" ) ).toString() );
    }

}
