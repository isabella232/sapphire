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

package org.eclipse.sapphire.tests.modeling.events;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.sapphire.tests.modeling.events.t0001.TestModelingEvents0001;
import org.eclipse.sapphire.tests.modeling.events.t0002.TestModelingEvents0002;
import org.eclipse.sapphire.tests.modeling.events.t0003.TestModelingEvents0003;
import org.eclipse.sapphire.tests.modeling.events.t0004.TestModelingEvents0004;
import org.eclipse.sapphire.tests.modeling.events.t0005.TestModelingEvents0005;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class EventSystemTests extends TestCase
{
    private EventSystemTests( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "EventSystemTests" );

        suite.addTest( TestModelingEvents0001.suite() );
        suite.addTest( TestModelingEvents0002.suite() );
        suite.addTest( TestModelingEvents0003.suite() );
        suite.addTest( TestModelingEvents0004.suite() );
        suite.addTest( TestModelingEvents0005.suite() );
        
        return suite;
    }
    
}
