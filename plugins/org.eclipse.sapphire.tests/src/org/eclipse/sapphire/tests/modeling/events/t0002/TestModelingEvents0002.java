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

package org.eclipse.sapphire.tests.modeling.events.t0002;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests delivery of list property events.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestModelingEvents0002 extends SapphireTestCase
{
    private TestModelingEvents0002( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestModelingEvents0002" );

        suite.addTest( new TestModelingEvents0002( "testListPropertyEvents" ) );
        
        return suite;
    }

    public void testListPropertyEvents() throws Exception
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
        
        final Status initialPropertyValidation = root.getChildren().validation();
        final Status initialElementValidation = root.validation();
        
        assertValidationError( initialPropertyValidation, "At least one child element must be specified." );
        assertValidationError( initialElementValidation, "At least one child element must be specified." );
        
        assertEquals( 2, events.size() );
        assertPropertyInitializationEvent( events.get( 0 ), root, RootElement.PROP_ENABLEMENT );
        assertPropertyInitializationEvent( events.get( 1 ), root, RootElement.PROP_CHILDREN );
        
        events.clear();
        
        final ChildElement child = root.getChildren().insert();
        child.attach( listener );
        
        assertEquals( 3, events.size() );
        assertPropertyContentEvent( events.get( 0 ), root, RootElement.PROP_CHILDREN );
        assertPropertyValidationEvent( events.get( 1 ), root, RootElement.PROP_CHILDREN, initialPropertyValidation, Status.createOkStatus() );
        assertElementValidationEvent( events.get( 2 ), root, initialElementValidation, Status.createOkStatus() );
        
        events.clear();
        
        child.setIntegerValue( "abc" );
        
        assertEquals( 5, events.size() );
        assertPropertyContentEvent( events.get( 0 ), child, ChildElement.PROP_INTEGER_VALUE );
        assertPropertyValidationEvent( events.get( 1 ), child, ChildElement.PROP_INTEGER_VALUE, Status.createOkStatus(), Status.createErrorStatus( "\"abc\" is not a valid integer." ) );
        assertElementValidationEvent( events.get( 2 ), child, Status.createOkStatus(), Status.createErrorStatus( "\"abc\" is not a valid integer." ) );
        assertPropertyValidationEvent( events.get( 3 ), root, RootElement.PROP_CHILDREN, Status.createOkStatus(), Status.createErrorStatus( "\"abc\" is not a valid integer." ) );
        assertElementValidationEvent( events.get( 4 ), root, Status.createOkStatus(), Status.createErrorStatus( "\"abc\" is not a valid integer." ) );
        
        events.clear();
        
        final ChildElement child2 = root.getChildren().insert();
        child2.attach( listener );
        
        final ChildElement child3 = root.getChildren().insert();
        child3.attach( listener );
        
        assertEquals( 2, events.size() );
        assertPropertyContentEvent( events.get( 0 ), root, RootElement.PROP_CHILDREN );
        assertPropertyContentEvent( events.get( 1 ), root, RootElement.PROP_CHILDREN );
        
        events.clear();
        
        root.getChildren().move( child3, 0 );
        
        assertEquals( 1, events.size() );
        assertPropertyContentEvent( events.get( 0 ), root, RootElement.PROP_CHILDREN );
        
        events.clear();
        
        root.getChildren().remove( child2 );
        root.getChildren().remove( child3 );
        
        assertEquals( 4, events.size() );
        assertElementDisposeEvent( events.get( 0 ), child2 );
        assertPropertyContentEvent( events.get( 1 ), root, RootElement.PROP_CHILDREN );
        assertElementDisposeEvent( events.get( 2 ), child3 );
        assertPropertyContentEvent( events.get( 3 ), root, RootElement.PROP_CHILDREN );
        
        events.clear();
        
        root.setEnablement( false );
        root.setEnablement( true );
        
        assertEquals( 6, events.size() );
        assertPropertyContentEvent( events.get( 0 ), root, RootElement.PROP_ENABLEMENT );
        assertPropertyEnablementEvent( events.get( 1 ), root, RootElement.PROP_CHILDREN, true, false );
        assertPropertyEnablementEvent( events.get( 2 ), child, ChildElement.PROP_INTEGER_VALUE, true, false );
        assertPropertyContentEvent( events.get( 3 ), root, RootElement.PROP_ENABLEMENT );
        assertPropertyEnablementEvent( events.get( 4 ), root, RootElement.PROP_CHILDREN, false, true );
        assertPropertyEnablementEvent( events.get( 5 ), child, ChildElement.PROP_INTEGER_VALUE, false, true );
        
        events.clear();
        
        root.dispose();
        
        assertEquals( 2, events.size() );
        assertElementDisposeEvent( events.get( 0 ), root );
        assertElementDisposeEvent( events.get( 1 ), child );
    }

}
