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

package org.eclipse.sapphire.tests.modeling.events.t0005;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.internal.MemoryResource;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.eclipse.sapphire.tests.modeling.events.EventLog;

/**
 * Tests delivery of PropertyInitializationEvent when property enablement is accessed first.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestModelingEvents0005 extends SapphireTestCase
{
    private TestModelingEvents0005( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestModelingEvents0005" );

        suite.addTest( new TestModelingEvents0005( "testInitializationEventDuringEnablementCheck_ValueProperty" ) );
        suite.addTest( new TestModelingEvents0005( "testInitializationEventDuringEnablementCheck_ElementProperty" ) );
        suite.addTest( new TestModelingEvents0005( "testInitializationEventDuringEnablementCheck_ImpliedElementProperty" ) );
        suite.addTest( new TestModelingEvents0005( "testInitializationEventDuringEnablementCheck_ListProperty" ) );
        
        return suite;
    }
    
    public void testInitializationEventDuringEnablementCheck_ValueProperty() throws Exception
    {
        test( RootElement.PROP_VALUE );
    }

    public void testInitializationEventDuringEnablementCheck_ElementProperty() throws Exception
    {
        test( RootElement.PROP_ELEMENT );
    }

    public void testInitializationEventDuringEnablementCheck_ImpliedElementProperty() throws Exception
    {
        test( RootElement.PROP_IMPLIED_ELEMENT );
    }

    public void testInitializationEventDuringEnablementCheck_ListProperty() throws Exception
    {
        test( RootElement.PROP_LIST );
    }

    private void test( final ModelProperty property ) throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate( new MemoryResource( RootElement.TYPE ) ); // Note explicit avoidance of instantiate with initialization.
        final EventLog log = new EventLog();
        
        root.attach( log );
        
        root.enabled( property );
        
        assertPropertyInitializationEvent( log.event( 0 ), root, RootElement.PROP_ENABLED );
        assertPropertyInitializationEvent( log.event( 1 ), root, property );
        
        log.clear();
        
        root.setEnabled( false );
        
        assertPropertyContentEvent( log.event( 0 ), root, RootElement.PROP_ENABLED );
        assertPropertyEnablementEvent( log.event( log.size() - 1 ), root, property, true, false );
        
        // TODO: Figure out why an extra PropertyEnablementEvent is generated.
        //       When Element property isn't being tested, the second event is the PropertyEnablementEvent 
        //       for Element property. Not expected.
    }

}
