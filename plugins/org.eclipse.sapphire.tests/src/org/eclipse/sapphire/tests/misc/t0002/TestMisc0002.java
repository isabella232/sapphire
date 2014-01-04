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

package org.eclipse.sapphire.tests.misc.t0002;

import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests Version class.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestMisc0002 extends SapphireTestCase
{
    @Test
    
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
    
    @Test
    
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
    
    @Test( expected = IllegalArgumentException.class )

    public void testVersionFromString_InvalidFormat_1()
    {
        new Version( null );
    }

    @Test( expected = IllegalArgumentException.class )
    
    public void testVersionFromString_InvalidFormat_2()
    {
        new Version( "" );
    }

    @Test( expected = IllegalArgumentException.class )
    
    public void testVersionFromString_InvalidFormat_3()
    {
        new Version( ".1.2.3" );
    }

    @Test( expected = IllegalArgumentException.class )
    
    public void testVersionFromString_InvalidFormat_4()
    {
        new Version( "1.2.3." );
    }

    @Test( expected = IllegalArgumentException.class )
    
    public void testVersionFromString_InvalidFormat_5()
    {
        new Version( "1..2.3" );
    }

    @Test( expected = IllegalArgumentException.class )
    
    public void testVersionFromString_InvalidFormat_6()
    {
        new Version( "abc" );
    }

    @Test( expected = IllegalArgumentException.class )
    
    public void testVersionFromString_InvalidFormat_7()
    {
        new Version( "1.2.abc" );
    }

    @Test( expected = IllegalArgumentException.class )
    
    public void testVersionFromString_InvalidFormat_8()
    {
        new Version( "1.2.v345" );
    }

    @Test( expected = IllegalArgumentException.class )
    
    public void testVersionFromString_InvalidFormat_9()
    {
        new Version( "1.2.345abc" );
    }
    
    @Test
    
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
    
    @Test
    
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
    
    @Test
    
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
    
    @Test
    
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
    
    @Test

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
