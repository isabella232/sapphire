/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.misc.t0004;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests making an existing property read-only when extending from an another model element
 * interface.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class Misc0004Test

    extends SapphireTestCase
    
{
    private Misc0004Test( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "Misc0004" );

        suite.addTest( new Misc0004Test( "testWritable" ) );
        suite.addTest( new Misc0004Test( "testReadOnly" ) );
        
        return suite;
    }
    
    public void testWritable() throws Exception
    {
        final IMisc0004TestElementWritable element = IMisc0004TestElementWritable.TYPE.instantiate();
        
        element.setText( "abc" );
        assertEquals( "abc", element.getText().getContent() );
        
        element.setInteger( "1" );
        assertEquals( Integer.valueOf( 1 ), element.getInteger().getContent() );

        element.setInteger( 2 );
        assertEquals( Integer.valueOf( 2 ), element.getInteger().getContent() );
    }

    public void testReadOnly() throws Exception
    {
        final IMisc0004TestElementReadOnly element = IMisc0004TestElementReadOnly.TYPE.instantiate();

        boolean caughtExpectedException = false;
        
        try
        {
            element.setText( "abc" );
        }
        catch( UnsupportedOperationException e )
        {
            caughtExpectedException = true;
        }
        
        if( ! caughtExpectedException )
        {
            fail( "Did not catch expected UnsupportedOperationException." );
        }

        caughtExpectedException = false;
        
        try
        {
            element.setInteger( "1" );
        }
        catch( UnsupportedOperationException e )
        {
            caughtExpectedException = true;
        }
        
        if( ! caughtExpectedException )
        {
            fail( "Did not catch expected UnsupportedOperationException." );
        }

        caughtExpectedException = false;
        
        try
        {
            element.setInteger( 2 );
        }
        catch( UnsupportedOperationException e )
        {
            caughtExpectedException = true;
        }
        
        if( ! caughtExpectedException )
        {
            fail( "Did not catch expected UnsupportedOperationException." );
        }
    }

}
