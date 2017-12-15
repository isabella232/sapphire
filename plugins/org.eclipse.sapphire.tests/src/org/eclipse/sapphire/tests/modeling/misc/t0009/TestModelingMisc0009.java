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

package org.eclipse.sapphire.tests.modeling.misc.t0009;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests included ValueSerializationService implementations.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestModelingMisc0009 extends SapphireTestCase
{
    private TestModelingMisc0009( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestModelingMisc0009" );

        suite.addTest( new TestModelingMisc0009( "testDate1" ) );
        suite.addTest( new TestModelingMisc0009( "testDate2" ) );
        suite.addTest( new TestModelingMisc0009( "testDate3" ) );
        
        return suite;
    }
    
    @SuppressWarnings( "deprecation" )
    public void testDate1() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
        Date d;
        
        element.setDate1( "2011-08-26" );
        d = element.getDate1().getContent();
        
        assertNotNull( d );
        assertEquals( 2011, d.getYear() + 1900 );
        assertEquals( 8, d.getMonth() + 1 );
        assertEquals( 26, d.getDate() );
        
        element.setDate1( "2011-08-26T07:50" );
        d = element.getDate1().getContent();
        
        assertNotNull( d );
        assertEquals( 2011, d.getYear() + 1900 );
        assertEquals( 8, d.getMonth() + 1 );
        assertEquals( 26, d.getDate() );
        assertEquals( 7, d.getHours() );
        assertEquals( 50, d.getMinutes() );
        
        element.setDate1( "07:50" );
        d = element.getDate1().getContent();
        
        assertNotNull( d );
        assertEquals( 7, d.getHours() );
        assertEquals( 50, d.getMinutes() );
        
        element.setDate1( ( new SimpleDateFormat( "yyyy-MM-dd" ) ).parse( "2011-08-26" ) );
        
        assertTrue( element.getDate1().getText().startsWith( "2011-08-26T00:00:00.000-" ) );
    }

    @SuppressWarnings( "deprecation" )
    public void testDate2() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
        Date d;
        
        element.setDate2( "2011.08.26" );
        d = element.getDate2().getContent();
        
        assertNotNull( d );
        assertEquals( 2011, d.getYear() + 1900 );
        assertEquals( 8, d.getMonth() + 1 );
        assertEquals( 26, d.getDate() );

        element.setDate2( "08/26/2011" );
        d = element.getDate2().getContent();
        
        assertNotNull( d );
        assertEquals( 2011, d.getYear() + 1900 );
        assertEquals( 8, d.getMonth() + 1 );
        assertEquals( 26, d.getDate() );

        element.setDate2( "2011-08-26" );
        d = element.getDate2().getContent();
        
        assertNull( d );
        
        element.setDate2( ( new SimpleDateFormat( "yyyy-MM-ddZ" ) ).parse( "2011-08-26-0700" ) );
        
        assertEquals( "2011.08.26", element.getDate2().getText() );
    }

    @SuppressWarnings( "deprecation" )
    public void testDate3() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
        Date d;
        
        element.setDate3( "26.08.2011" );
        d = element.getDate3().getContent();
        
        assertNotNull( d );
        assertEquals( 2011, d.getYear() + 1900 );
        assertEquals( 8, d.getMonth() + 1 );
        assertEquals( 26, d.getDate() );

        element.setDate3( "2011/08/26" );
        d = element.getDate3().getContent();
        
        assertNotNull( d );
        assertEquals( 2011, d.getYear() + 1900 );
        assertEquals( 8, d.getMonth() + 1 );
        assertEquals( 26, d.getDate() );

        element.setDate3( "2011-08-26" );
        d = element.getDate3().getContent();
        
        assertNull( d );
        
        element.setDate3( ( new SimpleDateFormat( "yyyy-MM-ddZ" ) ).parse( "2011-08-26-0700" ) );
        
        assertEquals( "26.08.2011", element.getDate3().getText() );
    }

}
