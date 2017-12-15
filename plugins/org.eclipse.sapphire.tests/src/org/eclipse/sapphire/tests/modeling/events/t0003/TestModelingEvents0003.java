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

package org.eclipse.sapphire.tests.modeling.events.t0003;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests delivery of element property events.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestModelingEvents0003 extends SapphireTestCase
{
    private TestModelingEvents0003( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestModelingEvents0003" );

        suite.addTest( new TestModelingEvents0003( "testElementPropertyEvents" ) );
        
        return suite;
    }

    public void testElementPropertyEvents() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        
        final List<Event> events = new ArrayList<Event>();
        
        final Listener listener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                events.add( event );
            }
        };
        
        root.attach( listener );
        
        assertValidationError( root.getChild().validation(), "Child must be specified." );
        assertValidationError( root.validation(), "Child must be specified." );

        assertEquals( 2, events.size() );
        assertPropertyInitializationEvent( events.get( 0 ), root, RootElement.PROP_ENABLEMENT );
        assertPropertyInitializationEvent( events.get( 1 ), root, RootElement.PROP_CHILD );
        
        events.clear();
        
        final ChildElement child1 = root.getChild().element( true );
        child1.attach( listener );
        
        assertEquals( 3, events.size() );
        assertPropertyContentEvent( events.get( 0 ), root, RootElement.PROP_CHILD );
        assertPropertyValidationEvent( events.get( 1 ), root, RootElement.PROP_CHILD, Status.createErrorStatus( "Child must be specified." ), Status.createOkStatus() );
        assertElementValidationEvent( events.get( 2 ), root, Status.createErrorStatus( "Child must be specified." ), Status.createOkStatus() );
        
        events.clear();
        
        child1.setIntegerValue( "abc" );
        
        assertEquals( 5, events.size() );
        assertPropertyContentEvent( events.get( 0 ), child1, ChildElement.PROP_INTEGER_VALUE );
        assertPropertyValidationEvent( events.get( 1 ), child1, ChildElement.PROP_INTEGER_VALUE, Status.createOkStatus(), Status.createErrorStatus( "\"abc\" is not a valid integer." ) );
        assertElementValidationEvent( events.get( 2 ), child1, Status.createOkStatus(), Status.createErrorStatus( "\"abc\" is not a valid integer." ) );
        assertPropertyValidationEvent( events.get( 3 ), root, RootElement.PROP_CHILD, Status.createOkStatus(), Status.createErrorStatus( "\"abc\" is not a valid integer." ) );
        assertElementValidationEvent( events.get( 4 ), root, Status.createOkStatus(), Status.createErrorStatus( "\"abc\" is not a valid integer." ) );
        
        events.clear();
        
        root.getChild().remove();
        
        assertEquals( 4, events.size() );
        assertElementDisposeEvent( events.get( 0 ), child1 );
        assertPropertyContentEvent( events.get( 1 ), root, RootElement.PROP_CHILD );
        assertPropertyValidationEvent( events.get( 2 ), root, RootElement.PROP_CHILD, Status.createErrorStatus( "\"abc\" is not a valid integer." ), Status.createErrorStatus( "Child must be specified." ) );
        assertElementValidationEvent( events.get( 3 ), root, Status.createErrorStatus( "\"abc\" is not a valid integer." ), Status.createErrorStatus( "Child must be specified." ) );
        
        events.clear();
        
        final ChildElement child2 = root.getChild().element( true );
        child2.attach( listener );

        root.setEnablement( false );
        root.setEnablement( true );
        
        assertEquals( 9, events.size() );
        assertPropertyContentEvent( events.get( 0 ), root, RootElement.PROP_CHILD );
        assertPropertyValidationEvent( events.get( 1 ), root, RootElement.PROP_CHILD, Status.createErrorStatus( "Child must be specified." ), Status.createOkStatus() );
        assertElementValidationEvent( events.get( 2 ), root, Status.createErrorStatus( "Child must be specified." ), Status.createOkStatus() );
        assertPropertyContentEvent( events.get( 3 ), root, RootElement.PROP_ENABLEMENT );
        assertPropertyEnablementEvent( events.get( 4 ), root, RootElement.PROP_CHILD, true, false );
        assertPropertyEnablementEvent( events.get( 5 ), child2, ChildElement.PROP_INTEGER_VALUE, true, false );
        assertPropertyContentEvent( events.get( 6 ), root, RootElement.PROP_ENABLEMENT );
        assertPropertyEnablementEvent( events.get( 7 ), root, RootElement.PROP_CHILD, false, true );
        assertPropertyEnablementEvent( events.get( 8 ), child2, ChildElement.PROP_INTEGER_VALUE, false, true );
        
        events.clear();
        
        root.dispose();
        
        assertEquals( 2, events.size() );
        assertElementDisposeEvent( events.get( 0 ), root );
        assertElementDisposeEvent( events.get( 1 ), child2 );
    }

}
