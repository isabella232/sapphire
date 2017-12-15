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

package org.eclipse.sapphire.tests.element;

import org.eclipse.sapphire.Disposable;
import org.eclipse.sapphire.tests.EventLog;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests the Element class.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ElementTests extends SapphireTestCase
{
    @Test
    
    public void HoldsElement() throws Exception
    {
        final TestElement a = TestElement.TYPE.instantiate();
        
        try
        {
            final TestElement aa = a.getElement().content( true );
            
            assertTrue( a.holds( aa ) );
            assertFalse( aa.holds( a ) );
            
            final TestElement aaa = aa.getList().insert();
            
            assertTrue( a.holds( aaa ) );
            assertTrue( aa.holds( aaa ) );
            assertFalse( aaa.holds( a ) );
            assertFalse( aaa.holds( aa ) );
            
            final TestElement ab = a.getList().insert();
            
            assertTrue( a.holds( ab ) );
            assertFalse( aa.holds( ab ) );
            assertFalse( ab.holds( aa ) );
            
            final TestElement b = TestElement.TYPE.instantiate();
            
            try
            {
                assertFalse( a.holds( b ) );
                assertFalse( b.holds( a ) );
            }
            finally
            {
                b.dispose();
            }
        }
        finally
        {
            a.dispose();
        }
    }

    @Test
    
    public void HoldsProperty() throws Exception
    {
        final TestElement a = TestElement.TYPE.instantiate();
        
        try
        {
            assertTrue( a.holds( a.getValue() ) );
            assertTrue( a.holds( a.getTransient() ) );
            assertTrue( a.holds( a.getElement() ) );
            assertTrue( a.holds( a.getList() ) );
            
            final TestElement aa = a.getElement().content( true );
            
            assertTrue( a.holds( aa.getValue() ) );
            assertTrue( a.holds( aa.getTransient() ) );
            assertTrue( a.holds( aa.getElement() ) );
            assertTrue( a.holds( aa.getList() ) );
            
            final TestElement aaa = aa.getList().insert();
            
            assertTrue( a.holds( aaa.getValue() ) );
            assertTrue( a.holds( aaa.getTransient() ) );
            assertTrue( a.holds( aaa.getElement() ) );
            assertTrue( a.holds( aaa.getList() ) );
    
            assertTrue( aa.holds( aaa.getValue() ) );
            assertTrue( aa.holds( aaa.getTransient() ) );
            assertTrue( aa.holds( aaa.getElement() ) );
            assertTrue( aa.holds( aaa.getList() ) );
            
            final TestElement ab = a.getList().insert();
            
            assertTrue( a.holds( ab.getValue() ) );
            assertTrue( a.holds( ab.getTransient() ) );
            assertTrue( a.holds( ab.getElement() ) );
            assertTrue( a.holds( ab.getList() ) );
            
            assertFalse( aa.holds( ab.getValue() ) );
            assertFalse( aa.holds( ab.getTransient() ) );
            assertFalse( aa.holds( ab.getElement() ) );
            assertFalse( aa.holds( ab.getList() ) );
    
            final TestElement b = TestElement.TYPE.instantiate();
            
            try
            {
                assertFalse( a.holds( b.getValue() ) );
                assertFalse( b.holds( a.getValue() ) );
            }
            finally
            {
                b.dispose();
            }
        }
        finally
        {
            a.dispose();
        }
    }
    
    @Test
    
    public void Suspend()
    {
        final EventLog log = new EventLog();
        final TestElement a = TestElement.TYPE.instantiate();
        
        try
        {
            a.attach( log, "*" );
            
            final TestElement aa = a.getElement().content( true );
            
            aa.setValue( "abc" );
            aa.getList().insert();
            
            assertEquals( 3, log.size() );
            assertPropertyContentEvent( log.event( 0 ), a.getElement() );
            assertPropertyContentEvent( log.event( 1 ), aa.getValue() );
            assertPropertyContentEvent( log.event( 2 ), aa.getList() );
            
            log.clear();
            
            final Disposable suspension = aa.suspend();
            
            try
            {
                aa.setValue( "def" );
                aa.getList().insert().setValue( "ghi" );
                aa.getElement().content( true ).setValue( "klm" );
                
                assertEquals( 0, log.size() );
                
                a.setValue( "nop" );
                
                assertEquals( 1, log.size() );
                assertPropertyContentEvent( log.event( 0 ), a.getValue() );
                
                log.clear();
            }
            finally
            {
                suspension.dispose();
            }
            
            assertEquals( 5, log.size() );
            assertPropertyContentEvent( log.event( 0 ), aa.getValue() );
            assertPropertyContentEvent( log.event( 1 ), aa.getList() );
            assertPropertyContentEvent( log.event( 2 ), aa.getList().get( 1 ).getValue() );
            assertPropertyContentEvent( log.event( 3 ), aa.getElement() );
            assertPropertyContentEvent( log.event( 4 ), aa.getElement().content().getValue() );
        }
        finally
        {
            a.dispose();
        }
    }

}
