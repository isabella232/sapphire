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

package org.eclipse.sapphire.tests.element;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.Disposable;
import org.eclipse.sapphire.ElementData;
import org.eclipse.sapphire.tests.EventLog;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests the Element class.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
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

    @Test
    
    public void CopyElement() throws Exception
    {
        final CopyTestElementRoot a = CopyTestElementRoot.TYPE.instantiate();
        CopyTestElementChild ac1;
        CopyTestElementChildEx ac2;
        
        a.setValueProperty1( "abc" );
        a.setValueProperty2( 5 );
        a.setValueProperty3( (String) null );
        
        ac1 = a.getImpliedElementProperty1();
        ac1.setValueProperty1( "implied-1" );
        
        ac2 = a.getImpliedElementProperty2();
        ac2.setValueProperty1( "implied-2-a" );
        ac2.setValueProperty2( "implied-2-b" );
        
        ac1 = a.getElementProperty1().content( true );
        ac1.setValueProperty1( "element-1" );
        
        ac2 = (CopyTestElementChildEx) a.getElementProperty2().content( true, CopyTestElementChildEx.TYPE );
        ac2.setValueProperty1( "element-2-a" );
        ac2.setValueProperty2( "element-2-b" );
        
        ac1 = a.getListProperty1().insert();
        ac1.setValueProperty1( "list-1-a" );
        
        ac1 = a.getListProperty1().insert();
        ac1.setValueProperty1( "list-1-b" );
        
        ac1 = a.getListProperty1().insert();
        ac1.setValueProperty1( "list-1-c" );
        
        ac1 = a.getListProperty2().insert( CopyTestElementChild.TYPE );
        ac1.setValueProperty1( "list-2-a" );

        ac2 = (CopyTestElementChildEx) a.getListProperty2().insert( CopyTestElementChildEx.TYPE );
        ac2.setValueProperty1( "list-2-b-a" );
        ac2.setValueProperty2( "list-2-b-b" );
        
        ac2 = (CopyTestElementChildEx) a.getListProperty2().insert( CopyTestElementChildEx.TYPE );
        ac2.setValueProperty1( "list-2-c-a" );
        ac2.setValueProperty2( "list-2-c-b" );
        
        final Object t = new Object();
        a.setTransientProperty( t );
        
        final CopyTestElementRoot b = CopyTestElementRoot.TYPE.instantiate();
        b.copy( a );
        
        assertEquals( b.getValueProperty1().text( false ), "abc" );
        assertEquals( b.getValueProperty2().text( false ), "5" );
        assertEquals( b.getValueProperty3().text( false ), null );
        
        assertEquals( b.getImpliedElementProperty1().getValueProperty1().text( false ), "implied-1" );
        assertEquals( b.getImpliedElementProperty2().getValueProperty1().text( false ), "implied-2-a" );
        assertEquals( b.getImpliedElementProperty2().getValueProperty2().text( false ), "implied-2-b" );
        
        assertEquals( b.getElementProperty1().content().getValueProperty1().text( false ), "element-1" );
        assertEquals( ( (CopyTestElementChildEx) b.getElementProperty2().content() ).getValueProperty1().text( false ), "element-2-a" );
        assertEquals( ( (CopyTestElementChildEx) b.getElementProperty2().content() ).getValueProperty2().text( false ), "element-2-b" );
        
        assertEquals( b.getListProperty1().size(), 3 );
        assertEquals( b.getListProperty1().get( 0 ).getValueProperty1().text( false ), "list-1-a" );
        assertEquals( b.getListProperty1().get( 1 ).getValueProperty1().text( false ), "list-1-b" );
        assertEquals( b.getListProperty1().get( 2 ).getValueProperty1().text( false ), "list-1-c" );
        
        assertEquals( b.getListProperty2().size(), 3 );
        assertEquals( b.getListProperty2().get( 0 ).getValueProperty1().text( false ), "list-2-a" );
        assertEquals( ( (CopyTestElementChildEx) b.getListProperty2().get( 1 ) ).getValueProperty1().text( false ), "list-2-b-a" );
        assertEquals( ( (CopyTestElementChildEx) b.getListProperty2().get( 1 ) ).getValueProperty2().text( false ), "list-2-b-b" );
        assertEquals( ( (CopyTestElementChildEx) b.getListProperty2().get( 2 ) ).getValueProperty1().text( false ), "list-2-c-a" );
        assertEquals( ( (CopyTestElementChildEx) b.getListProperty2().get( 2 ) ).getValueProperty2().text( false ), "list-2-c-b" );
        
        assertEquals( b.getTransientProperty().content(), t );
    }

    @Test
    
    public void CopyElementData() throws Exception
    {
        final ElementData a = new ElementData( CopyTestElementRoot.TYPE );
        ElementData ac1;
        ElementData ac2;
        List<ElementData> list;
        
        a.write( "ValueProperty1", "abc" );
        a.write( "ValueProperty2", 5 );
        a.write( "ValueProperty3", null );
        
        ac1 = new ElementData( CopyTestElementChild.TYPE );
        a.write( "ImpliedElementProperty1", ac1 );
        ac1.write( "ValueProperty1", "implied-1" );
        
        ac2 = new ElementData( CopyTestElementChildEx.TYPE );
        a.write( "ImpliedElementProperty2", ac2 );
        ac2.write( "ValueProperty1", "implied-2-a" );
        ac2.write( "ValueProperty2", "implied-2-b" );
        
        ac1 = new ElementData( CopyTestElementChild.TYPE );
        a.write( "ElementProperty1", ac1 );
        ac1.write( "ValueProperty1", "element-1" );
        
        ac2 = new ElementData( CopyTestElementChildEx.TYPE );
        a.write( "ElementProperty2", ac2 );
        ac2.write( "ValueProperty1", "element-2-a" );
        ac2.write( "ValueProperty2", "element-2-b" );
        
        list = new ArrayList<ElementData>();
        a.write( "ListProperty1", list );
        
        ac1 = new ElementData( CopyTestElementChild.TYPE );
        list.add( ac1 );
        ac1.write( "ValueProperty1", "list-1-a" );
        
        ac1 = new ElementData( CopyTestElementChild.TYPE );
        list.add( ac1 );
        ac1.write( "ValueProperty1", "list-1-b" );
        
        ac1 = new ElementData( CopyTestElementChild.TYPE );
        list.add( ac1 );
        ac1.write( "ValueProperty1", "list-1-c" );
        
        list = new ArrayList<ElementData>();
        a.write( "ListProperty2", list );
        
        ac1 = new ElementData( CopyTestElementChild.TYPE );
        list.add( ac1 );
        ac1.write( "ValueProperty1", "list-2-a" );

        ac2 = new ElementData( CopyTestElementChildEx.TYPE );
        list.add( ac2 );
        ac2.write( "ValueProperty1", "list-2-b-a" );
        ac2.write( "ValueProperty2", "list-2-b-b" );
        
        ac2 = new ElementData( CopyTestElementChildEx.TYPE );
        list.add( ac2 );
        ac2.write( "ValueProperty1", "list-2-c-a" );
        ac2.write( "ValueProperty2", "list-2-c-b" );
        
        final Object t = new Object();
        a.write( "TransientProperty", t );
        
        final CopyTestElementRoot b = CopyTestElementRoot.TYPE.instantiate();
        b.copy( a );
        
        assertEquals( b.getValueProperty1().text( false ), "abc" );
        assertEquals( b.getValueProperty2().text( false ), "5" );
        assertEquals( b.getValueProperty3().text( false ), null );
        
        assertEquals( b.getImpliedElementProperty1().getValueProperty1().text( false ), "implied-1" );
        assertEquals( b.getImpliedElementProperty2().getValueProperty1().text( false ), "implied-2-a" );
        assertEquals( b.getImpliedElementProperty2().getValueProperty2().text( false ), "implied-2-b" );
        
        assertEquals( b.getElementProperty1().content().getValueProperty1().text( false ), "element-1" );
        assertEquals( ( (CopyTestElementChildEx) b.getElementProperty2().content() ).getValueProperty1().text( false ), "element-2-a" );
        assertEquals( ( (CopyTestElementChildEx) b.getElementProperty2().content() ).getValueProperty2().text( false ), "element-2-b" );
        
        assertEquals( b.getListProperty1().size(), 3 );
        assertEquals( b.getListProperty1().get( 0 ).getValueProperty1().text( false ), "list-1-a" );
        assertEquals( b.getListProperty1().get( 1 ).getValueProperty1().text( false ), "list-1-b" );
        assertEquals( b.getListProperty1().get( 2 ).getValueProperty1().text( false ), "list-1-c" );
        
        assertEquals( b.getListProperty2().size(), 3 );
        assertEquals( b.getListProperty2().get( 0 ).getValueProperty1().text( false ), "list-2-a" );
        assertEquals( ( (CopyTestElementChildEx) b.getListProperty2().get( 1 ) ).getValueProperty1().text( false ), "list-2-b-a" );
        assertEquals( ( (CopyTestElementChildEx) b.getListProperty2().get( 1 ) ).getValueProperty2().text( false ), "list-2-b-b" );
        assertEquals( ( (CopyTestElementChildEx) b.getListProperty2().get( 2 ) ).getValueProperty1().text( false ), "list-2-c-a" );
        assertEquals( ( (CopyTestElementChildEx) b.getListProperty2().get( 2 ) ).getValueProperty2().text( false ), "list-2-c-b" );
        
        assertEquals( b.getTransientProperty().content(), t );
    }

}
