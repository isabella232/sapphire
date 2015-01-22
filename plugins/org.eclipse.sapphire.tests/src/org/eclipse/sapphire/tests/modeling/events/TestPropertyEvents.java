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

package org.eclipse.sapphire.tests.modeling.events;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests delivery of property events.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestPropertyEvents extends SapphireTestCase
{
    private List<Event> monitor( final TestElement element )
    {
        return monitor( element, null );
    }
    
    private List<Event> monitor( final TestElement element, final String path )
    {
        final List<Event> events = new ArrayList<Event>();
        
        final Listener listener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                events.add( event );
            }
        };
        
        if( path == null )
        {
            element.attach( listener );
            
            for( Property property : element.properties() )
            {
                property.attach( listener );
            }
        }
        else
        {
            element.attach( listener, path );
        }
        
        return events;
    }
    
    @Test
    
    public void testEventsValuePropertyPlain() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            final List<Event> events = monitor( element );
            
            assertEquals( 0, events.size() );
            
            element.getValuePlain().text();
            element.setValuePlain( "a" );
            
            assertEquals( 1, events.size() );
            assertValuePropertyContentEvent( events.get( 0 ), element.getValuePlain(), null, "a" );
            
            events.clear();
            
            element.getValuePlain().validation();
            element.setValuePlain( "b" );
            
            assertEquals( 1, events.size() );
            assertValuePropertyContentEvent( events.get( 0 ), element.getValuePlain(), "a", "b" );
            
            events.clear();

            element.getValuePlain().enabled();
            element.setValuePlain( "c" );
            
            assertEquals( 1, events.size() );
            assertValuePropertyContentEvent( events.get( 0 ), element.getValuePlain(), "b", "c" );
        }
        finally
        {
            element.dispose();
        }
    }
    
    @Test

    public void testEventsValuePropertyConstrained() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            final List<Event> events = monitor( element );
            
            assertEquals( 0, events.size() );
            
            element.getValueConstrained().text();
            element.setValueConstrained( "a" );
            
            assertEquals( 1, events.size() );
            assertValuePropertyContentEvent( events.get( 0 ), element.getValueConstrained(), null, "a" );
            
            events.clear();
            
            element.getValueConstrained().validation();
            element.setValueConstrained( "b" );
            
            assertEquals( 1, events.size() );
            assertValuePropertyContentEvent( events.get( 0 ), element.getValueConstrained(), "a", "b" );
            
            events.clear();
            
            element.setValueConstrained( null );
            
            assertEquals( 2, events.size() );
            assertValuePropertyContentEvent( events.get( 0 ), element.getValueConstrained(), "b", null );
            assertPropertyValidationEvent( events.get( 1 ), element.getValueConstrained(), Status.createOkStatus(), Status.createErrorStatus( "Value constrained must be specified" ) );
            
            events.clear();

            element.getValueConstrained().enabled();
            element.setValueConstrained( "c" );
            
            assertEquals( 2, events.size() );
            assertValuePropertyContentEvent( events.get( 0 ), element.getValueConstrained(), null, "c" );
            assertPropertyValidationEvent( events.get( 1 ), element.getValueConstrained(), Status.createErrorStatus( "Value constrained must be specified" ), Status.createOkStatus() );
            
            events.clear();
            
            element.setEnablement( false );
            
            assertEquals( 2, events.size() );
            assertValuePropertyContentEvent( events.get( 0 ), element.getEnablement(), null, "false" );
            assertPropertyEnablementEvent( events.get( 1 ), element.getValueConstrained(), true, false );
        }
        finally
        {
            element.dispose();
        }
    }
    
    @Test
    
    public void testEventsListPropertyDescendents() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            final List<Event> events = monitor( element, "List/*" );
            
            assertEquals( 0, events.size() );

            final TestElement.ListEntry a = element.getList().insert();
            
            assertEquals( 1, events.size() );
            assertPropertyContentEvent( events.get( 0 ), element.getList() );
            
            events.clear();
            
            final TestElement.ListEntry b = element.getList().insert();

            assertEquals( 1, events.size() );
            assertPropertyContentEvent( events.get( 0 ), element.getList() );
            
            events.clear();
            
            a.setValue( "abc" );
            b.setValue( "def" );
            
            assertEquals( 2, events.size() );
            assertPropertyContentEvent( events.get( 0 ), a.getValue() );
            assertPropertyContentEvent( events.get( 1 ), b.getValue() );
            
            events.clear();
            
            element.getList().remove( a );
            
            assertEquals( 1, events.size() );
            assertPropertyContentEvent( events.get( 0 ), element.getList() );
            
            events.clear();
            
            final TestElement.ListEntry ba = b.getChildren().insert();
            final TestElement.ListEntry bb = b.getChildren().insert();

            assertEquals( 2, events.size() );
            assertPropertyContentEvent( events.get( 0 ), b.getChildren() );
            assertPropertyContentEvent( events.get( 1 ), b.getChildren() );
            
            events.clear();
            
            ba.setValue( "ghi" );
            bb.setValue( "jkl" );

            assertEquals( 2, events.size() );
            assertPropertyContentEvent( events.get( 0 ), ba.getValue() );
            assertPropertyContentEvent( events.get( 1 ), bb.getValue() );
        }
        finally
        {
            element.dispose();
        }
    }

    @Test
    
    public void testEventsListPropertyPath() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            final List<Event> events = monitor( element, "List/Children/Value" );
            
            assertEquals( 0, events.size() );

            final TestElement.ListEntry a = element.getList().insert();
            
            assertEquals( 1, events.size() );
            assertPropertyContentEvent( events.get( 0 ), element.getList() );
            
            events.clear();
            
            final TestElement.ListEntry b = element.getList().insert();

            assertEquals( 1, events.size() );
            assertPropertyContentEvent( events.get( 0 ), element.getList() );
            
            events.clear();
            
            a.setValue( "abc" );
            b.setValue( "def" );
            
            assertEquals( 0, events.size() );
            
            element.getList().remove( a );
            
            assertEquals( 1, events.size() );
            assertPropertyContentEvent( events.get( 0 ), element.getList() );
            
            events.clear();
            
            final TestElement.ListEntry ba = b.getChildren().insert();
            final TestElement.ListEntry bb = b.getChildren().insert();

            assertEquals( 2, events.size() );
            assertPropertyContentEvent( events.get( 0 ), b.getChildren() );
            assertPropertyContentEvent( events.get( 1 ), b.getChildren() );
            
            events.clear();
            
            ba.setValue( "ghi" );
            bb.setValue( "jkl" );

            assertEquals( 2, events.size() );
            assertPropertyContentEvent( events.get( 0 ), ba.getValue() );
            assertPropertyContentEvent( events.get( 1 ), bb.getValue() );
        }
        finally
        {
            element.dispose();
        }
    }

}
