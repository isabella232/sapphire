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

package org.eclipse.sapphire.tests.modeling.events.t0001;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.eclipse.sapphire.tests.modeling.events.EventLog;

/**
 * Tests delivery of value property events.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestModelingEvents0001 extends SapphireTestCase
{
    private TestModelingEvents0001( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestModelingEvents0001" );

        suite.addTest( new TestModelingEvents0001( "testElementListenerInstance" ) );
        suite.addTest( new TestModelingEvents0001( "testElementListenerGlobal" ) );
        suite.addTest( new TestModelingEvents0001( "testPropertyListenerInstance" ) );
        suite.addTest( new TestModelingEvents0001( "testPropertyListenerGlobal" ) );
        
        return suite;
    }
    
    public void testElementListenerInstance() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        final EventLog log = new EventLog();
        
        root.attach( log );
        
        testElementListener( root, log );
    }

    public void testElementListenerGlobal() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        
        testElementListener( root, GlobalRootElementEventLog.INSTANCE );
    }

    public void testPropertyListenerInstance() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        final EventLog log = new EventLog();
        
        root.attach( log, RootElement.PROP_REQUIRED_STRING_VALUE.getName() );
        
        testPropertyListener( root, log );
    }

    public void testPropertyListenerGlobal() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        
        testPropertyListener( root, GlobalRequiredStringValueEventLog.INSTANCE );
    }

    private void testElementListener( final RootElement root,
                                      final EventLog log ) 
                                                  
        throws Exception
        
    {
        log.clear();
        
        final Status initialPropertyValidation = root.getRequiredStringValue().validation();
        final Status initialElementValidation = root.validation();
        
        assertValidationError( initialPropertyValidation, "Required string value must be specified." );
        assertValidationError( initialElementValidation, "Required string value must be specified." );
        
        assertEquals( 2, log.size() );
        assertPropertyInitializationEvent( log.event( 0 ), root, RootElement.PROP_ENABLEMENT );
        assertPropertyInitializationEvent( log.event( 1 ), root, RootElement.PROP_REQUIRED_STRING_VALUE );
        
        log.clear();
        
        root.setRequiredStringValue( "abc" );
        
        assertEquals( 3, log.size() );
        assertPropertyContentEvent( log.event( 0 ), root, RootElement.PROP_REQUIRED_STRING_VALUE );
        assertPropertyValidationEvent( log.event( 1 ), root, RootElement.PROP_REQUIRED_STRING_VALUE, initialPropertyValidation, Status.createOkStatus() );
        assertElementValidationEvent( log.event( 2 ), root, initialPropertyValidation, Status.createOkStatus() );
        
        root.setRequiredStringValue( null );
        
        log.clear();
        
        root.setRequiredStringValue( "abc" );
        root.setRequiredStringValue( "xyz" );
        
        assertEquals( 4, log.size() );
        assertPropertyContentEvent( log.event( 0 ), root, RootElement.PROP_REQUIRED_STRING_VALUE );
        assertPropertyValidationEvent( log.event( 1 ), root, RootElement.PROP_REQUIRED_STRING_VALUE, initialPropertyValidation, Status.createOkStatus() );
        assertElementValidationEvent( log.event( 2 ), root, initialPropertyValidation, Status.createOkStatus() );
        assertPropertyContentEvent( log.event( 3 ), root, RootElement.PROP_REQUIRED_STRING_VALUE );
        
        log.clear();
        
        root.setEnablement( false );
        root.setEnablement( true );
        
        assertEquals( 4, log.size() );
        assertPropertyContentEvent( log.event( 0 ), root, RootElement.PROP_ENABLEMENT );
        assertPropertyEnablementEvent( log.event( 1 ), root, RootElement.PROP_REQUIRED_STRING_VALUE, true, false );
        assertPropertyContentEvent( log.event( 2 ), root, RootElement.PROP_ENABLEMENT );
        assertPropertyEnablementEvent( log.event( 3 ), root, RootElement.PROP_REQUIRED_STRING_VALUE, false, true );
        
        log.clear();
        
        root.dispose();
        
        assertEquals( 1, log.size() );
        assertElementDisposeEvent( log.event( 0 ), root );
    }
    
    /**
     * Identical scenario to testElementListener() method, but only expect to see events related to RequireStringValue
     * property.
     */

    private void testPropertyListener( final RootElement root,
                                       final EventLog log ) 
                                                  
        throws Exception
        
    {
        log.clear();
        
        final Status initialPropertyValidation = root.getRequiredStringValue().validation();
        final Status initialElementValidation = root.validation();
        
        assertValidationError( initialPropertyValidation, "Required string value must be specified." );
        assertValidationError( initialElementValidation, "Required string value must be specified." );
        
        assertEquals( 1, log.size() );
        assertPropertyInitializationEvent( log.event( 0 ), root, RootElement.PROP_REQUIRED_STRING_VALUE );
        
        log.clear();
        
        root.setRequiredStringValue( "abc" );
        
        assertEquals( 2, log.size() );
        assertPropertyContentEvent( log.event( 0 ), root, RootElement.PROP_REQUIRED_STRING_VALUE );
        assertPropertyValidationEvent( log.event( 1 ), root, RootElement.PROP_REQUIRED_STRING_VALUE, initialPropertyValidation, Status.createOkStatus() );
        
        root.setRequiredStringValue( null );
        
        log.clear();
        
        root.setRequiredStringValue( "abc" );
        root.setRequiredStringValue( "xyz" );
        
        assertEquals( 3, log.size() );
        assertPropertyContentEvent( log.event( 0 ), root, RootElement.PROP_REQUIRED_STRING_VALUE );
        assertPropertyValidationEvent( log.event( 1 ), root, RootElement.PROP_REQUIRED_STRING_VALUE, initialPropertyValidation, Status.createOkStatus() );
        assertPropertyContentEvent( log.event( 2 ), root, RootElement.PROP_REQUIRED_STRING_VALUE );
        
        log.clear();
        
        root.setEnablement( false );
        root.setEnablement( true );
        
        assertEquals( 2, log.size() );
        assertPropertyEnablementEvent( log.event( 0 ), root, RootElement.PROP_REQUIRED_STRING_VALUE, true, false );
        assertPropertyEnablementEvent( log.event( 1 ), root, RootElement.PROP_REQUIRED_STRING_VALUE, false, true );
        
        log.clear();
        
        root.dispose();
        
        assertEquals( 0, log.size() );
    }

}
